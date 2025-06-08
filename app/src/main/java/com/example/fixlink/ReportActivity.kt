package com.example.fixlink

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.repository.StateIssueRepository
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportActivity : AppCompatActivity() {
    private lateinit var reportEditText: EditText
    private lateinit var sendReportButton: Button
    private val issueRepository = IssueRepository()
    private val stateIssueRepository = StateIssueRepository()
    private val userRepository = UserRepository()
    private var issueId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // Add fragments
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                .commit()

            supportFragmentManager.beginTransaction()
                .replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
                .commit()
        }

        // Initialize views and load data
        initializeViews()
        loadData()
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

    private fun loadData() {
        issueId = intent.getStringExtra("ISSUE_ID")
        if (issueId == null) {
            Toast.makeText(this, "Error: Issue ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
    }

    private fun sendReport(report: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withContext(Dispatchers.IO) {
                    issueId?.let { id ->
                        issueRepository.updateIssueReport(id, report)
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