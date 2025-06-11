package com.example.fixlink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var statusMessage: TextView
    private lateinit var progressBar: ProgressBar
    private var resetToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupClickListeners()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun extractTokenFromUri(uri: Uri): String? {
        // Get the full URI fragment (content after '#')
        val fragment = uri.fragment
        Log.d("ResetPassword", "Full URI: $uri")
        Log.d("ResetPassword", "Full fragment: $fragment")
        
        if (fragment == null) {
            Log.d("ResetPassword", "No fragment found in URI")
            return null
        }

        // Parse fragment parameters
        val params = fragment.split("&")
        Log.d("ResetPassword", "Found ${params.size} parameters in fragment")
        
        for (param in params) {
            val pair = param.split("=")
            if (pair.size < 2) {
                Log.d("ResetPassword", "Invalid parameter format: $param")
                continue
            }
            
            val key = pair[0]
            val value = pair[1]
            Log.d("ResetPassword", "Parameter - Key: $key, Value: ${value.take(10)}...")
            
            // Look for Supabase's access_token parameter
            if ("access_token" == key) {
                Log.d("ResetPassword", "Found access_token in fragment")
                return value
            }
        }
        Log.d("ResetPassword", "No access_token found in fragment")
        return null
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            Log.d("ResetPassword", "Received URI: $uri")
            
            if (uri?.scheme == "fixlink" && uri.host == "reset-password") {
                // Extract token from URI fragment
                resetToken = extractTokenFromUri(uri)
                Log.d("ResetPassword", "Extracted token: $resetToken")
                
                if (resetToken == null) {
                    statusMessage.text = "Invalid reset link. Please request a new password reset."
                    resetPasswordButton.isEnabled = false
                } else {
                    // Enable the reset button since we have a valid token
                    resetPasswordButton.isEnabled = true
                }
            } else {
                Log.e("ResetPassword", "Invalid URI scheme or host: ${uri?.scheme}://${uri?.host}")
                statusMessage.text = "Invalid reset link. Please request a new password reset."
                resetPasswordButton.isEnabled = false
            }
        } else {
            Log.e("ResetPassword", "No ACTION_VIEW intent received")
            statusMessage.text = "Invalid reset link. Please request a new password reset."
            resetPasswordButton.isEnabled = false
        }
    }

    private fun initializeViews() {
        newPasswordInput = findViewById(R.id.new_password_input)
        confirmPasswordInput = findViewById(R.id.confirm_password_input)
        resetPasswordButton = findViewById(R.id.reset_password_button)
        statusMessage = findViewById(R.id.status_message)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupClickListeners() {
        val backArrow = findViewById<ImageView>(R.id.back_arrow)
        backArrow.setOnClickListener {
            finish()
        }

        resetPasswordButton.setOnClickListener {
            if (validateInputs()) {
                updatePassword(newPasswordInput.text.toString())
            }
        }
    }

    private fun validateInputs(): Boolean {
        val newPassword = newPasswordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        if (newPassword.isEmpty()) {
            newPasswordInput.error = "New password is required"
            return false
        }

        if (newPassword.length < 6) {
            newPasswordInput.error = "Password must be at least 6 characters"
            return false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.error = "Please confirm your password"
            return false
        }

        if (newPassword != confirmPassword) {
            confirmPasswordInput.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun updatePassword(newPassword: String) {
        if (resetToken == null) {
            statusMessage.text = "Invalid reset link. Please request a new password reset."
            return
        }

        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use the recovery token to update the password
                SupabaseClient.supabase.auth.updateUser {
                    password = newPassword
                }
                
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Password updated successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    // Navigate to login screen
                    startActivity(Intent(this@ResetPasswordActivity, LoginActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                Log.e("ResetPassword", "Password reset failed", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    statusMessage.text = "Error: ${e.message}"
                    Toast.makeText(
                        this@ResetPasswordActivity,
                        "Error updating password: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        resetPasswordButton.isEnabled = !show
        newPasswordInput.isEnabled = !show
        confirmPasswordInput.isEnabled = !show
    }
} 