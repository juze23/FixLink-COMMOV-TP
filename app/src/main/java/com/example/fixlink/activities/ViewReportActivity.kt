package com.example.fixlink.activities

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fixlink.R
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.repository.MaintenanceRepository
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import com.example.fixlink.fragments.TopAppBarFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewReportActivity : AppCompatActivity() {
    private val issueRepository = IssueRepository()
    private val maintenanceRepository = MaintenanceRepository()
    private val userRepository = UserRepository()
    private var issueId: String? = null
    private var maintenanceId: String? = null
    private var isMaintenance: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_report)

        // Check user role before proceeding
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userResult = userRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    val isAdmin = user?.typeId == 3
                    val isTechnician = user?.typeId == 2
                    
                    if (!isAdmin && !isTechnician) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ViewReportActivity, "Access denied. Only administrators and technicians can view reports.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        return@launch
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ViewReportActivity, "Error: Could not verify user permissions", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return@launch
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ViewReportActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                return@launch
            }
        }

        // Get IDs from intent
        issueId = intent.getStringExtra("ISSUE_ID")
        maintenanceId = intent.getStringExtra("MAINTENANCE_ID")
        isMaintenance = maintenanceId != null

        if (issueId == null && maintenanceId == null) {
            Toast.makeText(this, "Error: No task ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Add fragments
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                .commit()

            // Add bottom navigation with the correct selected item
            val bottomNavFragment = BottomNavigationAdminFragment().apply {
                arguments = Bundle().apply {
                    // Set the selected item based on whether this is for maintenance or issue
                    putInt("selected_item", if (isMaintenance) R.id.nav_maintenance else R.id.nav_issues)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                .commit()
        }

        val reportTextView = findViewById<TextView>(R.id.viewReportText)

        CoroutineScope(Dispatchers.Main).launch {
            val report = withContext(Dispatchers.IO) {
                if (isMaintenance) {
                    maintenanceRepository.getMaintenanceById(maintenanceId!!).getOrNull()?.report
                } else {
                    issueRepository.getIssueById(issueId!!).getOrNull()?.report
                }
            }
            
            if (!report.isNullOrEmpty()) {
                reportTextView.text = report
            } else {
                reportTextView.text = "No report available."
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Show back button in top app bar after fragment is initialized
        val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.showBackButton()
    }
} 