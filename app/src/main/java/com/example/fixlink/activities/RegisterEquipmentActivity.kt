package com.example.fixlink.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import com.example.fixlink.data.repository.EquipmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.enableEdgeToEdge
import com.example.fixlink.R
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import com.example.fixlink.fragments.TopAppBarFragment

class RegisterEquipmentActivity : AppCompatActivity() {

    private lateinit var equipmentNameInput: EditText
    private lateinit var equipmentDescriptionInput: EditText
    private lateinit var registerEquipmentButton: Button

    private val equipmentRepository = EquipmentRepository()

    companion object {
        private const val TAG = "RegisterEquipmentActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_equipment)

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
        equipmentNameInput = findViewById(R.id.equipment_name_input)
        equipmentDescriptionInput = findViewById(R.id.equipment_description_input)
        registerEquipmentButton = findViewById(R.id.register_equipment_button)

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

    private fun setupRegisterButton() {
        registerEquipmentButton.setOnClickListener {
            if (validateEquipmentInputs()) {
                registerEquipment()
            }
        }
    }

    private fun validateEquipmentInputs(): Boolean {
        val name = equipmentNameInput.text.toString().trim()
        if (name.isEmpty()) {
            equipmentNameInput.error = getString(R.string.error_empty_equipment_name)
            return false
        }

        return true
    }

    private fun registerEquipment() {
        val name = equipmentNameInput.text.toString().trim()
        val description = equipmentDescriptionInput.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = equipmentRepository.registerEquipment(name, description)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterEquipmentActivity, getString(R.string.success_equipment_registered), Toast.LENGTH_SHORT).show()
                            // Clear inputs
                            equipmentNameInput.text.clear()
                            equipmentDescriptionInput.text.clear()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterEquipmentActivity, getString(R.string.error_equipment_registration), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterEquipmentActivity, getString(R.string.error_equipment_registration), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 