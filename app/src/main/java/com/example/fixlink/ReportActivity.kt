package com.example.fixlink

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.repository.MaintenanceRepository
import com.example.fixlink.data.repository.StateIssueRepository
import com.example.fixlink.data.repository.StateMaintenanceRepository
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportActivity : AppCompatActivity() {
    private lateinit var reportEditText: EditText
    private lateinit var sendReportButton: Button
    private val issueRepository = IssueRepository()
    private val maintenanceRepository = MaintenanceRepository()
    private val stateIssueRepository = StateIssueRepository()
    private val stateMaintenanceRepository = StateMaintenanceRepository()
    private val userRepository = UserRepository()
    private var issueId: String? = null
    private var maintenanceId: String? = null
    private var isMaintenance: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Get IDs from intent first to determine if it's maintenance or issue
        issueId = intent.getStringExtra("ISSUE_ID")
        maintenanceId = intent.getStringExtra("MAINTENANCE_ID")
        isMaintenance = maintenanceId != null

        if (issueId == null && maintenanceId == null) {
            Toast.makeText(this, "Error: No task ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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
                            Toast.makeText(this@ReportActivity, "Access denied. Only administrators and technicians can send reports.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        return@launch
                    }

                    // Add fragments after user role check
                    withContext(Dispatchers.Main) {
                        if (savedInstanceState == null) {
                            // Add top app bar
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                                .commit()

                            // Add bottom navigation with the correct fragment based on user role
                            val selectedItem = if (isMaintenance) R.id.nav_maintenance else R.id.nav_issues
                            val bottomNavFragment = if (isAdmin) {
                                BottomNavigationAdminFragment().apply {
                                    arguments = Bundle().apply {
                                        putInt("selected_item", selectedItem)
                                    }
                                }
                            } else {
                                BottomNavigationFragment().apply {
                                    arguments = Bundle().apply {
                                        putInt("selected_item", selectedItem)
                                    }
                                }
                            }
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                                .commit()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ReportActivity, "Error: Could not verify user permissions", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // Initialize views
        initializeViews()
    }

    override fun onResume() {
        super.onResume()
        // Show back button in top app bar
        val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.showBackButton()
    }

    private fun initializeViews() {
        reportEditText = findViewById(R.id.reportEditText)
        sendReportButton = findViewById(R.id.sendReportButton)

        sendReportButton.setOnClickListener {
            val report = reportEditText.text.toString()
            if (report.isNotEmpty()) {
                sendReport(report)
            } else {
                Toast.makeText(this, "Please enter a report", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendReport(report: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    if (isMaintenance) {
                        maintenanceId?.let { id ->
                            maintenanceRepository.updateMaintenanceReport(id, report)
                            // No need to call changeMaintenanceStatus since updateMaintenanceReport already sets status to completed
                        }
                    } else {
                        issueId?.let { id ->
                            issueRepository.updateIssueReport(id, report)
                            // Update status to resolved
                            issueRepository.changeIssueStatus(id, "resolved")
                        }
                    }
                }
                Toast.makeText(this@ReportActivity, "Report sent successfully", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ReportActivity, "Error sending report: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
} 