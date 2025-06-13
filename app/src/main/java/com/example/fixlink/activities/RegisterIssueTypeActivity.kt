package com.example.fixlink.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fixlink.R
import com.example.fixlink.data.repository.IssueTypeRepository
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import com.example.fixlink.fragments.TopAppBarFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterIssueTypeActivity : AppCompatActivity() {
    private lateinit var issueTypeNameInput: EditText
    private lateinit var registerIssueTypeButton: Button
    private val issueTypeRepository = IssueTypeRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_issue_type)

        // Configure system bars
        window.statusBarColor = getColor(R.color.purple_secondary)
        window.navigationBarColor = getColor(R.color.purple_secondary)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        // Initialize UI components
        issueTypeNameInput = findViewById(R.id.issue_type_name_input)
        registerIssueTypeButton = findViewById(R.id.register_issue_type_button)

        // Load fragments
        val topAppBarFragment = TopAppBarFragment()
        loadFragment(topAppBarFragment, R.id.topAppBarFragmentContainer)
        loadFragment(BottomNavigationAdminFragment().apply {
            arguments = Bundle().apply {
                putInt("selected_item", R.id.nav_admin)
            }
        }, R.id.bottomNavigationContainer)

        // Set title after fragment is loaded
        topAppBarFragment.arguments = Bundle().apply {
            putBoolean("show_back_button", true)
            putString("title", getString(R.string.title_register_issue_type))
        }

        setupRegisterButton()
    }

    private fun setupRegisterButton() {
        registerIssueTypeButton.setOnClickListener {
            if (validateIssueTypeInputs()) {
                registerIssueType()
            }
        }
    }

    private fun validateIssueTypeInputs(): Boolean {
        val issueTypeName = issueTypeNameInput.text.toString().trim()

        if (issueTypeName.isEmpty()) {
            issueTypeNameInput.error = getString(R.string.error_empty_issue_type_name)
            return false
        }

        return true
    }

    private fun registerIssueType() {
        val issueTypeName = issueTypeNameInput.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = issueTypeRepository.addIssueType(issueTypeName)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@RegisterIssueTypeActivity,
                                getString(R.string.success_issue_type_registered),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@RegisterIssueTypeActivity,
                                getString(R.string.error_issue_type_registration),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterIssueTypeActivity,
                        getString(R.string.error_issue_type_registration),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment, containerId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }
} 