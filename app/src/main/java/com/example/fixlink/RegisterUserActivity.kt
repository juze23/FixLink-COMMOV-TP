package com.example.fixlink

import android.os.Bundle
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
import com.google.android.material.button.MaterialButtonToggleGroup

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var technicianSpinner: Spinner
    private lateinit var toggleButtonLayout: MaterialButtonToggleGroup
    private lateinit var userFormLayout: androidx.constraintlayout.widget.ConstraintLayout
    private lateinit var equipmentFormLayout: androidx.constraintlayout.widget.ConstraintLayout

    // User form views (already have spinner, add others if needed)
    private lateinit var emailInput: EditText
    private lateinit var phoneNumberInput: EditText
    private lateinit var registerUserButton: Button

    // Equipment form views
    private lateinit var equipmentNameInput: EditText
    private lateinit var equipmentLocationInput: EditText
    private lateinit var registerEquipmentButton: Button

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
        technicianSpinner = findViewById(R.id.technician_spinner)
        emailInput = findViewById(R.id.email_input)
        phoneNumberInput = findViewById(R.id.phone_number_input)
        registerUserButton = findViewById(R.id.register_user_button)
        equipmentNameInput = findViewById(R.id.equipment_name_input)
        equipmentLocationInput = findViewById(R.id.equipment_location_input)
        registerEquipmentButton = findViewById(R.id.register_equipment_button)

        setupTechnicianSpinner()
        setupToggleButtonListener()
        // Initial form visibility based on checked button in layout
        updateFormVisibility(toggleButtonLayout.checkedButtonId)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupTechnicianSpinner() {
        // Create an ArrayAdapter using a sample string array and a default spinner layout
        val technicianAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.technician_options,
            android.R.layout.simple_spinner_item
        )

        // Specify the layout to use when the list of choices appears
        technicianAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        technicianSpinner.adapter = technicianAdapter
    }

    private fun setupToggleButtonListener() {
        toggleButtonLayout.addOnButtonCheckedListener { toggleGroup, checkedId, isChecked ->
            if (isChecked) {
                updateFormVisibility(checkedId)
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