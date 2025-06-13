package com.example.fixlink.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.fixlink.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Build
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.fixlink.R
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import com.example.fixlink.fragments.TopAppBarFragment

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var userTypeSpinner: Spinner
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var firstnameInput: EditText
    private lateinit var lastnameInput: EditText
    private lateinit var registerUserButton: Button

    private val userRepository = UserRepository()

    companion object {
        private const val TAG = "RegisterUserActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_user)

        // Configure system bars
        window.statusBarColor = getColor(R.color.purple_secondary)
        window.navigationBarColor = getColor(R.color.purple_secondary)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and 
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() and 
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment().apply {
                    arguments = Bundle().apply {
                        putBoolean("show_back_button", true)
                    }
                })
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment().apply {
                    arguments = Bundle().apply {
                        putInt("selected_item", R.id.nav_admin)
                    }
                })
            }
        }

        // Initialize views
        userTypeSpinner = findViewById(R.id.user_type_spinner)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        passwordInput = findViewById(R.id.password_input)
        firstnameInput = findViewById(R.id.firstname_input)
        lastnameInput = findViewById(R.id.lastname_input)
        registerUserButton = findViewById(R.id.register_user_button)

        setupUserTypeSpinner()
        setupRegisterButton()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // Show back button in top app bar
        val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.showBackButton()
    }

    private fun setupUserTypeSpinner() {
        val userTypeAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.user_type_options,
            android.R.layout.simple_spinner_item
        )
        userTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = userTypeAdapter
    }

    private fun setupRegisterButton() {
        registerUserButton.setOnClickListener {
            if (validateUserInputs()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerUser()
                } else {
                    Toast.makeText(this, "This app requires Android 8.0 or higher", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateUserInputs(): Boolean {
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val firstname = firstnameInput.text.toString().trim()
        val lastname = lastnameInput.text.toString().trim()
        val userTypePosition = userTypeSpinner.selectedItemPosition

        if (userTypePosition == 0) {
            Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
            return false
        }

        if (firstname.isEmpty()) {
            firstnameInput.error = "First name is required"
            return false
        }

        if (lastname.isEmpty()) {
            lastnameInput.error = "Last name is required"
            return false
        }

        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.error = "Please enter a valid email address"
            return false
        }

        if (phone.isEmpty()) {
            phoneInput.error = "Phone number is required"
            return false
        }

        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            return false
        }

        if (password.length < 6) {
            passwordInput.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerUser() {
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val firstname = firstnameInput.text.toString().trim()
        val lastname = lastnameInput.text.toString().trim()
        val userTypePosition = userTypeSpinner.selectedItemPosition
        val typeId = when (userTypePosition) {
            1 -> 3 // Admin
            2 -> 2 // Technician
            else -> return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.signUp(email, password, phone, typeId, firstname, lastname)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterUserActivity, "User registered successfully!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterUserActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterUserActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}