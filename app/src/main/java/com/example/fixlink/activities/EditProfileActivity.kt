package com.example.fixlink.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.AlertDialog
import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import android.net.ConnectivityManager
import android.content.Context
import android.net.NetworkCapabilities
import com.example.fixlink.data.preferences.ProfilePreferences
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.R
import com.example.fixlink.fragments.TopAppBarFragment
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import com.example.fixlink.fragments.BottomNavigationFragment
import com.example.fixlink.fragments.BottomNavigationUserFragment
import com.example.fixlink.utils.NavigationUtils

class EditProfileActivity : AppCompatActivity() {

    private lateinit var firstnameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var changePasswordButton: Button
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    private val userRepository = UserRepository()
    private lateinit var profilePreferences: ProfilePreferences
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        profilePreferences = ProfilePreferences(this)

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
        userId = intent.getStringExtra("USER_ID")
        val firstname = intent.getStringExtra("USER_FIRSTNAME") ?: ""
        val lastname = intent.getStringExtra("USER_LASTNAME") ?: ""
        val email = intent.getStringExtra("USER_EMAIL") ?: ""
        val phone = intent.getStringExtra("USER_PHONE") ?: ""

        // Set initial values
        firstnameEditText.setText(firstname)
        lastnameEditText.setText(lastname)
        emailEditText.setText(email)
        phoneEditText.setText(phone)

        // Set click listeners
        changePasswordButton.setOnClickListener { showChangePasswordDialog() }
        cancelButton.setOnClickListener { finish() }
        saveButton.setOnClickListener { updateProfile() }

        // Check for pending updates when activity starts
        checkPendingUpdates()
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
            .setTitle(getString(R.string.dialog_change_password_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.dialog_change_password_button)) { _, _ ->
                val currentPassword = currentPasswordEditText.text.toString()
                val newPassword = newPasswordEditText.text.toString()
                val confirmPassword = confirmPasswordEditText.text.toString()

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, getString(R.string.text_passwords_dont_match), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, getString(R.string.text_password_length_error), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                updatePassword(currentPassword, newPassword)
            }
            .setNegativeButton(getString(R.string.dialog_change_password_cancel), null)
            .show()
    }

    private fun updatePassword(currentPassword: String, newPassword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.updatePassword(currentPassword, newPassword)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditProfileActivity, getString(R.string.text_password_updated), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditProfileActivity, getString(R.string.text_error_updating_password, error.message), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProfileActivity, getString(R.string.text_error_updating_password, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkPendingUpdates() {
        if (profilePreferences.hasPendingUpdates()) {
            val pendingUpdate = profilePreferences.getPendingProfileUpdate()
            if (pendingUpdate != null && isNetworkAvailable()) {
                syncPendingUpdate(pendingUpdate)
            }
        }
    }

    private fun syncPendingUpdate(update: ProfilePreferences.ProfileUpdate) {
        lifecycleScope.launch {
            try {
                val result = userRepository.updateUserProfile(
                    userId = update.userId,
                    firstname = update.firstname,
                    lastname = update.lastname,
                    email = update.email,
                    phone = update.phone
                )

                if (result.isSuccess) {
                    profilePreferences.clearPendingUpdates()
                    Toast.makeText(this@EditProfileActivity, getString(R.string.text_pending_updates_synced), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@EditProfileActivity, getString(R.string.text_error_syncing_updates, result.exceptionOrNull()?.message), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditProfileActivity, getString(R.string.text_error_syncing, e.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProfile() {
        val userId = userId ?: return
        val firstname = firstnameEditText.text.toString().trim()
        val lastname = lastnameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        if (firstname.isEmpty()) {
            firstnameEditText.error = getString(R.string.text_error_firstname_required)
            return
        }

        if (email.isEmpty()) {
            emailEditText.error = getString(R.string.text_error_email_required)
            return
        }

        if (isNetworkAvailable()) {
            // Online update
            lifecycleScope.launch {
                val result = userRepository.updateUserProfile(
                    userId = userId,
                    firstname = firstname,
                    lastname = lastname,
                    email = email,
                    phone = phone
                )

                if (result.isSuccess) {
                    Toast.makeText(this@EditProfileActivity, getString(R.string.text_profile_update_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditProfileActivity, getString(R.string.text_error_updating_profile_generic, result.exceptionOrNull()?.message), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Offline update
            profilePreferences.savePendingProfileUpdate(
                userId = userId,
                firstname = firstname,
                lastname = lastname,
                email = email,
                phone = phone
            )
            Toast.makeText(this, getString(R.string.text_profile_offline_save), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Show back button in top app bar
        val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.showBackButton()
    }
} 