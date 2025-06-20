package com.example.fixlink.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.data.preferences.LoginPreferences
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.enableEdgeToEdge
import com.example.fixlink.R
import com.example.fixlink.fragments.AdminDashboardFragment
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import com.example.fixlink.fragments.TopAppBarFragment
import com.example.fixlink.utils.NavigationUtils


class DashboardActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private lateinit var loginPreferences: LoginPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
                            Toast.makeText(this@DashboardActivity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        return@launch
                    }
                }

                // If we have a user, verify their type
                val userType = currentUser?.typeId ?: loginPreferences.getUserType()
                if (userType != 3) { // 3 is admin type
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DashboardActivity, "Access denied. Invalid user type.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
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
                    Toast.makeText(this@DashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@DashboardActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun loadContent() {
        // Add TopAppBarFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            .commit()

        // Add AdminDashboardFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.dashboardContainer, AdminDashboardFragment())
            .commit()

        // Add appropriate bottom navigation based on user type
        CoroutineScope(Dispatchers.Main).launch {
            val bottomNavFragment = withContext(Dispatchers.IO) {
                NavigationUtils.getBottomNavigationFragment()
            }
            // Set the selected item to dashboard
            if (bottomNavFragment is BottomNavigationAdminFragment) {
                bottomNavFragment.arguments = Bundle().apply {
                    putInt("selected_item", R.id.nav_dashboard)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                .commit()
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
} 