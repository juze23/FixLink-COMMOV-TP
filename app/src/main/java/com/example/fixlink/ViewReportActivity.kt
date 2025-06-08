package com.example.fixlink

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fixlink.data.repository.IssueRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewReportActivity : AppCompatActivity() {
    private val issueRepository = IssueRepository()
    private var issueId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_report)

        // Adiciona fragments de top app bar e bottom navigation
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                .commit()
            supportFragmentManager.beginTransaction()
                .replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
                .commit()
        }

        val reportTextView = findViewById<TextView>(R.id.viewReportText)

        issueId = intent.getStringExtra("ISSUE_ID")
        if (issueId == null) {
            Toast.makeText(this, "Error: Issue ID not provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            val issue = withContext(Dispatchers.IO) {
                issueRepository.getIssueById(issueId!!).getOrNull()
            }
            if (issue != null && !issue.report.isNullOrEmpty()) {
                reportTextView.text = issue.report
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