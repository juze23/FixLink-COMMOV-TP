package com.example.fixlink

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fixlink.data.preferences.LoginPreferences
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.ui.filters.IssuesFilterDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private lateinit var loginPreferences: LoginPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

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