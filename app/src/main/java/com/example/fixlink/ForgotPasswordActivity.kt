package com.example.fixlink

import android.os.Bundle
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

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var resetPasswordButton: Button
    private lateinit var statusMessage: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)
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
            val email = emailInput.text.toString().trim()
            if (email.isEmpty()) {
                emailInput.error = "Please enter your email"
                return@setOnClickListener
            }
            sendResetEmail(email)
        }
    }

    private fun sendResetEmail(email: String) {
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseClient.supabase.auth.resetPasswordForEmail(email, "fixlink://reset-password")
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    statusMessage.text = "Password reset email sent. Please check your inbox."
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Password reset email sent",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    statusMessage.text = "Error: ${e.message}"
                    Toast.makeText(
                        this@ForgotPasswordActivity,
                        "Error sending reset email: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        resetPasswordButton.isEnabled = !show
        emailInput.isEnabled = !show
    }
}