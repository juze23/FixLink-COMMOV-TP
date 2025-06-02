package com.example.fixlink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.content.Intent

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize fragments
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment()) // Assuming admin bottom nav
            }
        }

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)

        // Set click listeners
        changePasswordButton.setOnClickListener { handleChangePassword() }
        cancelButton.setOnClickListener { finish() } // Basic cancel: just close activity
        saveButton.setOnClickListener { saveProfile() }

        // TODO: Load current profile data into EditTexts
    }

    private fun handleChangePassword() {
        // TODO: Implement navigation to Change Password screen
        // val intent = Intent(this, ChangePasswordActivity::class.java)
        // startActivity(intent)
    }

    private fun saveProfile() {
        // TODO: Implement logic to save profile changes
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()

        // TODO: Validate input and save data (e.g., update database, send to API)

        // TODO: Show success message and potentially close activity
    }
} 