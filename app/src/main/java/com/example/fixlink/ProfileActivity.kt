package com.example.fixlink

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.content.Intent
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.entities.User
import com.example.fixlink.NavigationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.fragment.app.commit

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnEditProfile: Button
    private lateinit var btnLogout: Button
    private lateinit var nameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private var isFromAdmin: Boolean = false

    private val userRepository = UserRepository()
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Check if coming from admin
        isFromAdmin = intent.getBooleanExtra("FROM_ADMIN", false)

        // Add fragments
        if (savedInstanceState == null) {
            val topAppBarFragment = TopAppBarFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.topAppBarFragmentContainer, topAppBarFragment)
                .commit()

            // Add appropriate bottom navigation based on user type
            CoroutineScope(Dispatchers.Main).launch {
                val bottomNavFragment = withContext(Dispatchers.IO) {
                    NavigationUtils.getBottomNavigationFragment()
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                    .commit()
            }
        }

        initializeViews()
        
        loadUserData()

        btnEditProfile.setOnClickListener { navigateToEditProfile() }
        btnLogout.setOnClickListener { handleLogout() }
    }

    private fun initializeViews() {
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        btnLogout = findViewById(R.id.btn_logout)
        nameTextView = findViewById(R.id.edit_name)
        emailTextView = findViewById(R.id.edit_email)
        phoneTextView = findViewById(R.id.edit_phone)
    }

    private fun loadUserData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.getCurrentUser()
                result.fold(
                    onSuccess = { user ->
                        currentUser = user
                        withContext(Dispatchers.Main) {
                            updateUI(user)
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(user: User) {
        nameTextView.text = if (user.lastname.isNullOrEmpty()) user.firstname else "${user.firstname} ${user.lastname}"
        emailTextView.text = user.email
        phoneTextView.text = user.phoneNumber ?: "Not set"
        // TODO: Load profile image if available
    }

    private fun navigateToEditProfile() {
        val intent = Intent(this, EditProfileActivity::class.java).apply {
            putExtra("USER_ID", currentUser?.user_id)
            putExtra("USER_NAME", if (currentUser?.lastname.isNullOrEmpty()) currentUser?.firstname else "${currentUser?.firstname} ${currentUser?.lastname}")
            putExtra("USER_EMAIL", currentUser?.email)
            putExtra("USER_PHONE", currentUser?.phoneNumber)
        }
        startActivity(intent)
    }

    private fun handleLogout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.logout()
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ProfileActivity, LoginActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            startActivity(intent)
                            finish()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ProfileActivity, "Error logging out: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        
        // Show back button only if coming from admin
        if (isFromAdmin) {
            val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
            topAppBarFragment?.showBackButton()
        }
    }
}