package com.example.fixlink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.content.Intent
import android.widget.Toast
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.LinearLayout

class EditProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    private val userRepository = UserRepository()
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize fragments
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationUserFragment())
            }
        }

        // Initialize views
        initializeViews()

        // Load user data from intent
        loadUserData()

        // Set click listeners
        changePasswordButton.setOnClickListener { showChangePasswordDialog() }
        cancelButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { saveProfile() }
    }

    private fun initializeViews() {
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)
    }

    private fun loadUserData() {
        userId = intent.getStringExtra("USER_ID")
        nameEditText.setText(intent.getStringExtra("USER_NAME"))
        emailEditText.setText(intent.getStringExtra("USER_EMAIL"))
        phoneEditText.setText(intent.getStringExtra("USER_PHONE"))
    }

    private fun showChangePasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null)
        val currentPasswordEditText = dialogView.findViewById<EditText>(R.id.currentPasswordEditText)
        val newPasswordEditText = dialogView.findViewById<EditText>(R.id.newPasswordEditText)
        val confirmPasswordEditText = dialogView.findViewById<EditText>(R.id.confirmPasswordEditText)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updatePassword(currentPassword, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePassword(currentPassword: String, newPassword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.updatePassword(currentPassword, newPassword)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditProfileActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditProfileActivity, "Error updating password: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveProfile() {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()

        if (name.isBlank() || email.isBlank()) {
            Toast.makeText(this, "Name and email are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        userId?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val result = userRepository.updateUserProfile(id, name, email, phone)
                    result.fold(
                        onSuccess = {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        },
                        onFailure = { error ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@EditProfileActivity, "Error updating profile: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show()
        }
    }
} 