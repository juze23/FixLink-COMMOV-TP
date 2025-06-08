package com.example.fixlink

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.logIn(email, password)
                result.fold(
                    onSuccess = { user ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to the appropriate activity based on user type
                            val intent = when (user.typeId) {
                                1 -> Intent(this@LoginActivity, IssuesUserActivity::class.java) // Regular user
                                2 -> Intent(this@LoginActivity, MyTasksActivity::class.java) // Technician
                                3 -> Intent(this@LoginActivity, DashboardActivity::class.java) // Admin
                                else -> Intent(this@LoginActivity, ProfileActivity::class.java)
                            }
                            // Add flag to clear the back stack
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
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
}