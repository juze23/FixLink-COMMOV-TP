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
import androidx.lifecycle.lifecycleScope

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firstnameEditText: EditText
    private lateinit var lastnameEditText: EditText
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
            }

            // Add appropriate bottom navigation based on user type
            CoroutineScope(Dispatchers.Main).launch {
                val bottomNavFragment = withContext(Dispatchers.IO) {
                    NavigationUtils.getBottomNavigationFragment()
                }
                // Set the selected item to profile
                when (bottomNavFragment) {
                    is BottomNavigationUserFragment,
                    is BottomNavigationFragment,
                    is BottomNavigationAdminFragment -> {
                        bottomNavFragment.arguments = Bundle().apply {
                            putInt("selected_item", R.id.nav_profile)
                        }
                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                    .commit()
            }
        }

        // Initialize views
        initializeViews()

        // Get user data from intent
        val userId = intent.getStringExtra("USER_ID")
        val fullName = intent.getStringExtra("USER_NAME") ?: ""
        val email = intent.getStringExtra("USER_EMAIL") ?: ""
        val phone = intent.getStringExtra("USER_PHONE") ?: ""

        // Split full name into first and last name
        val nameParts = fullName.split(" ", limit = 2)
        val firstname = nameParts.getOrNull(0) ?: ""
        val lastname = nameParts.getOrNull(1) ?: ""

        // Set initial values
        firstnameEditText.setText(firstname)
        lastnameEditText.setText(lastname)
        emailEditText.setText(email)
        phoneEditText.setText(phone)

        // Set click listeners
        changePasswordButton.setOnClickListener { showChangePasswordDialog() }
        cancelButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { updateProfile() }
    }

    private fun initializeViews() {
        firstnameEditText = findViewById(R.id.firstnameEditText)
        lastnameEditText = findViewById(R.id.lastnameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        cancelButton = findViewById(R.id.cancelButton)
        saveButton = findViewById(R.id.saveButton)
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

    private fun updateProfile() {
        val userId = intent.getStringExtra("USER_ID") ?: return
        val firstname = firstnameEditText.text.toString().trim()
        val lastname = lastnameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        if (firstname.isEmpty()) {
            firstnameEditText.error = "First name is required"
            return
        }

        if (lastname.isEmpty()) {
            lastnameEditText.error = "Last name is required"
            return
        }

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            return
        }

        lifecycleScope.launch {
            val result = userRepository.updateUserProfile(
                userId = userId,
                firstname = firstname,
                lastname = lastname,
                email = email,
                phone = phone
            )

            if (result.isSuccess) {
                Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@EditProfileActivity, "Failed to update profile: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Show back button in top app bar
        val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.showBackButton()
    }
} 