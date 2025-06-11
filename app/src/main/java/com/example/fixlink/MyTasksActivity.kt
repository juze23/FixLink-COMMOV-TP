package com.example.fixlink

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fixlink.NavigationUtils
import com.example.fixlink.data.preferences.LoginPreferences
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import android.widget.Toast

class MyTasksActivity : AppCompatActivity() {
    private val userRepository = UserRepository()
    private lateinit var loginPreferences: LoginPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_my_tasks)

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
                            Toast.makeText(this@MyTasksActivity, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@MyTasksActivity, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                        return@launch
                    }
                }

                // If we have a user, verify their type
                val userType = currentUser?.typeId ?: loginPreferences.getUserType()
                if (userType != 2) { // 2 is technician type
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MyTasksActivity, "Access denied. Invalid user type.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MyTasksActivity, LoginActivity::class.java)
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
                    Toast.makeText(this@MyTasksActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@MyTasksActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }

        // Handle system insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Apply padding to the top app bar container
            findViewById<View>(R.id.topAppBarFragmentContainer).setPadding(
                insets.left,
                insets.top,
                insets.right,
                0
            )
            
            // Apply padding to the bottom navigation container
            findViewById<View>(R.id.bottomNavigationContainer).setPadding(
                insets.left,
                0,
                insets.right,
                insets.bottom
            )
            
            windowInsets
        }
    }

    private fun loadContent() {
        // Add TopAppBarFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            .commit()

        // Add MyTasksFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.myTasksContentFragmentContainer, MyTasksFragment())
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
} 