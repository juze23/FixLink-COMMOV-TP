package com.example.fixlink

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.repository.MaintenanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewReportActivity : AppCompatActivity() {
    private val issueRepository = IssueRepository()
    private val maintenanceRepository = MaintenanceRepository()
    private var issueId: String? = null
    private var maintenanceId: String? = null
    private var isMaintenance: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_report)

        // Get IDs from intent first to determine the type
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