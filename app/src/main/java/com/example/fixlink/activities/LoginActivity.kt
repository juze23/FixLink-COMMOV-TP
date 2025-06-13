package com.example.fixlink.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.R
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.preferences.LoginPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var rememberMeCheckbox: CheckBox
    private val userRepository = UserRepository()
    private lateinit var loginPreferences: LoginPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginPreferences = LoginPreferences(this)
        
        // Check if user is already logged in and remember me is enabled
        if (loginPreferences.isLoggedIn() && loginPreferences.shouldRememberMe()) {
            val storedUserType = loginPreferences.getUserType()
            if (storedUserType != -1) {
                // Try to get current user from Supabase
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val currentUser = userRepository.getCurrentUser().getOrNull()
                        if (currentUser != null) {
                            // If we can get the current user, use their actual type
                            withContext(Dispatchers.Main) {
                                navigateToAppropriateActivity(currentUser.typeId)
                            }
                        } else {
                            // If we can't get current user but have stored type, use stored type
                            withContext(Dispatchers.Main) {
                                navigateToAppropriateActivity(storedUserType)
                            }
                        }
                    } catch (e: Exception) {
                        // If any error, use stored type
                        withContext(Dispatchers.Main) {
                            navigateToAppropriateActivity(storedUserType)
                        }
                    }
                }
                return
            }
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        rememberMeCheckbox = findViewById(R.id.remember_checkbox)
        rememberMeCheckbox.isChecked = true
    }

    private fun setupClickListeners() {
        val signUpLink = findViewById<TextView>(R.id.sign_up_link)
        val forgotPassword = findViewById<TextView>(R.id.forgot_password)

        signUpLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            return false
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            return false
        }

        return true
    }

    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val rememberMe = rememberMeCheckbox.isChecked

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.logIn(email, password)
                result.fold(
                    onSuccess = { user ->
                        // Save login state only if remember me is checked
                        if (rememberMe) {
                            loginPreferences.saveLoginState(user.user_id, user.email, user.typeId, true)
                        } else {
                            // Clear any existing login state if remember me is not checked
                            loginPreferences.clearLoginState()
                        }
                        
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                            navigateToAppropriateActivity(user.typeId)
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToAppropriateActivity(userType: Int) {
        val intent = when (userType) {
            1 -> Intent(this, IssuesUserActivity::class.java) // Regular user
            2 -> Intent(this, MyTasksActivity::class.java) // Technician
            3 -> Intent(this, DashboardActivity::class.java) // Admin
            else -> {
                // If user type is invalid, clear preferences and show login
                loginPreferences.clearLoginState()
                Toast.makeText(this, "Invalid user type. Please login again.", Toast.LENGTH_SHORT).show()
                return
            }
        }
        // Add flag to clear the back stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}