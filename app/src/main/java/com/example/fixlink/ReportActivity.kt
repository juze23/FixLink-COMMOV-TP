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
        // Adicionar fragments de top app bar e bottom navigation
        supportFragmentManager.beginTransaction()
            .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            .commit()

        CoroutineScope(Dispatchers.IO).launch {
            val userResult = userRepository.getCurrentUser()
            withContext(Dispatchers.Main) {
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    val navFragment = if (user?.typeId == 2) BottomNavigationFragment() else BottomNavigationUserFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.bottomNavigationContainer, navFragment)
                        .commit()
                } else {
                    // fallback para usuário comum
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.bottomNavigationContainer, BottomNavigationUserFragment())
                        .commit()
                }
            }
        }

        reportEditText = findViewById(R.id.reportEditText)
        sendReportButton = findViewById(R.id.sendReportButton)
        issueId = intent.getStringExtra("ISSUE_ID")

        sendReportButton.setOnClickListener {
            val reportText = reportEditText.text.toString().trim()
            if (reportText.isEmpty()) {
                Toast.makeText(this, "Please enter your report.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (issueId == null) {
                Toast.makeText(this, "Invalid issue.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            android.app.AlertDialog.Builder(this)
                .setTitle("Finish Task")
                .setMessage("Are you sure you want to send this report and finish this task?")
                .setPositiveButton("Send Report") { _, _ ->
                    sendReport(reportText)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun sendReport(report: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val issueResult = issueRepository.getIssueById(issueId!!)
            val statesResult = stateIssueRepository.getIssueStates()
            if (issueResult.isFailure || statesResult.isFailure) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportActivity, "Error loading data.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val issue = issueResult.getOrNull()!!
            val states = statesResult.getOrNull()!!
            val resolvedState = states.find { it.state.equals("resolved", true) }
            if (resolvedState == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportActivity, "Resolved state not found.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val updatedIssue = issue.copy(state_id = resolvedState.state_id, report = report)
            val updateResult = issueRepository.updateIssue(updatedIssue)
            withContext(Dispatchers.Main) {
                if (updateResult.isSuccess) {
                    Toast.makeText(this@ReportActivity, "Report sent and task resolved!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@ReportActivity, "Failed to send report.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 