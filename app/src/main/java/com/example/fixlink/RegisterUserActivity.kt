package com.example.fixlink

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.TopAppBarFragment
import com.example.fixlink.BottomNavigationAdminFragment
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.commit
import android.widget.LinearLayout
import android.widget.Button
import android.widget.EditText
import android.view.View
import android.widget.Toast
import com.google.android.material.button.MaterialButtonToggleGroup
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.repository.EquipmentRepository
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.os.Build
import androidx.annotation.RequiresApi

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var userTypeSpinner: Spinner
    private lateinit var toggleButtonLayout: MaterialButtonToggleGroup
    private lateinit var userFormLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var equipmentFormLayout: androidx.constraintlayout.widget.ConstraintLayout

    // User form views
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var registerUserButton: Button

    // Equipment form views
    private lateinit var equipmentNameInput: EditText
    private lateinit var equipmentLocationSpinner: Spinner
    private lateinit var registerEquipmentButton: Button

    private val userRepository = UserRepository()
    private val equipmentRepository = EquipmentRepository()
    private val locationRepository = LocationRepository()
    private var locationList: List<Location> = emptyList()

    companion object {
        private const val TAG = "RegisterUserActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
            }
        }

        // Initialize views
        toggleButtonLayout = findViewById(R.id.toggleButtonLayout)
        userFormLayout = findViewById(R.id.userFormLayout)
        equipmentFormLayout = findViewById(R.id.equipmentFormLayout)
        userTypeSpinner = findViewById(R.id.user_type_spinner)
        emailInput = findViewById(R.id.email_input)
        phoneInput = findViewById(R.id.phone_input)
        passwordInput = findViewById(R.id.password_input)
        registerUserButton = findViewById(R.id.register_user_button)
        equipmentNameInput = findViewById(R.id.equipment_name_input)
        equipmentLocationSpinner = findViewById(R.id.equipmentLocationSpinner)
        registerEquipmentButton = findViewById(R.id.register_equipment_button)

        setupUserTypeSpinner()
        setupToggleButtonListener()
        setupRegisterButtons()
        // Initial form visibility based on checked button in layout
        updateFormVisibility(toggleButtonLayout.checkedButtonId)
        loadLocations()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

    private fun loadLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                locationRepository.getLocationList().fold(
                    onSuccess = { locations ->
                        locationList = locations
                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(
                                this@RegisterUserActivity,
                                android.R.layout.simple_spinner_item,
                                listOf("Select location") + locations.map { it.name }
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            equipmentLocationSpinner.adapter = adapter
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error loading locations: ${error.message}", error)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterUserActivity, "Error loading locations", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadLocations: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterUserActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupToggleButtonListener() {
        toggleButtonLayout.addOnButtonCheckedListener { toggleGroup, checkedId, isChecked ->
            if (isChecked) {
                updateFormVisibility(checkedId)
            }
        }
    }

    private fun setupRegisterButtons() {
        registerUserButton.setOnClickListener {
            if (validateUserInputs()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    registerUser()
                } else {
                    Toast.makeText(this, "This app requires Android 8.0 or higher", Toast.LENGTH_LONG).show()
                }
            }
        }

        registerEquipmentButton.setOnClickListener {
            if (validateEquipmentInputs()) {
                registerEquipment()
            }
        }
    }

    private fun validateUserInputs(): Boolean {
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val userTypePosition = userTypeSpinner.selectedItemPosition

        if (userTypePosition == 0) {
            Toast.makeText(this, "Please select a user type", Toast.LENGTH_SHORT).show()
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

    private fun validateEquipmentInputs(): Boolean {
        val name = equipmentNameInput.text.toString().trim()
        if (equipmentLocationSpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun registerUser() {
        val email = emailInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val userTypePosition = userTypeSpinner.selectedItemPosition
        val typeId = when (userTypePosition) {
            1 -> 3 // Admin
            2 -> 2 // Technician
            else -> return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.signUp(email, password, phone, typeId)
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

    private fun registerEquipment() {
        val name = equipmentNameInput.text.toString().trim()
        if (name.isEmpty()) {
            equipmentNameInput.error = "Equipment name is required"
            return
        }

        val locationId = locationList[equipmentLocationSpinner.selectedItemPosition - 1].location_id
        val description = "Location ID: $locationId"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = equipmentRepository.registerEquipment(name, description)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterUserActivity, "Equipment registered successfully!", Toast.LENGTH_SHORT).show()
                            // Clear inputs
                            equipmentNameInput.text.clear()
                            equipmentLocationSpinner.setSelection(0)
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

    private fun updateFormVisibility(checkedId: Int) {
        when (checkedId) {
            R.id.button_user -> {
                userFormLayout.visibility = View.VISIBLE
                equipmentFormLayout.visibility = View.GONE
            }
            R.id.button_equipment -> {
                userFormLayout.visibility = View.GONE
                equipmentFormLayout.visibility = View.VISIBLE
            }
        }
    }
}