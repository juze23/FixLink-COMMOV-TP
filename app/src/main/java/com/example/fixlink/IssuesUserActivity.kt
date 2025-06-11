package com.example.fixlink

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.NavigationUtils
import com.example.fixlink.data.preferences.LoginPreferences
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import android.widget.Toast

class IssuesUserActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private lateinit var loginPreferences: LoginPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_issues_user)

        loginPreferences = LoginPreferences(this)

        // Check user role before loading content
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = userRepository.getCurrentUser().getOrNull()
                if (currentUser == null) {
                    // If we can't get the current user, try to use stored type
                    val storedUserType = loginPreferences.getUserType()
                    if (storedUserType == -1) {
                        // If no stored type, clear preferences and go to login
                        loginPreferences.clearLoginState()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@IssuesUserActivity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@IssuesUserActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        return@launch
                    }
                }

                // If we have a user, verify their type
                val userType = currentUser?.typeId ?: loginPreferences.getUserType()
                if (userType != 1 && userType != 2 && userType != 3) { // 1 is regular user, 2 is technician, 3 is admin
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@IssuesUserActivity, "Access denied. Invalid user type.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@IssuesUserActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    return@launch
                }

                // If we get here, user is valid and has correct type, load the content
                withContext(Dispatchers.Main) {
                    loadContent()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@IssuesUserActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@IssuesUserActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_issues_host)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadContent() {
        // Add TopAppBarFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            .commit()

        // Add IssuesContentFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.issuesContentFragmentContainer, IssuesContentFragment())
            .commit()

        // Add appropriate bottom navigation based on user type
        CoroutineScope(Dispatchers.Main).launch {
            val bottomNavFragment = withContext(Dispatchers.IO) {
                NavigationUtils.getBottomNavigationFragment()
            }
            // Set the selected item to issues for both admin and technician navigation
            when (bottomNavFragment) {
                is BottomNavigationAdminFragment -> {
                    bottomNavFragment.arguments = Bundle().apply {
                        putInt("selected_item", R.id.nav_issues)
                    }
                }
                is BottomNavigationFragment -> {
                    bottomNavFragment.arguments = Bundle().apply {
                        putInt("selected_item", R.id.nav_issues)
                    }
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                .commit()
        }
    }
} 