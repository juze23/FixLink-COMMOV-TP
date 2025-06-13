package com.example.fixlink.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.fixlink.R
import com.example.fixlink.data.repository.MaintenanceTypeRepository
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import com.example.fixlink.fragments.TopAppBarFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterMaintenanceTypeActivity : AppCompatActivity() {
    private lateinit var maintenanceTypeNameInput: EditText
    private lateinit var registerMaintenanceTypeButton: Button
    private val maintenanceTypeRepository = MaintenanceTypeRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_maintenance_type)

        // Configure system bars
        window.statusBarColor = getColor(R.color.purple_secondary)
        window.navigationBarColor = getColor(R.color.purple_secondary)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        // Initialize UI components
        maintenanceTypeNameInput = findViewById(R.id.maintenance_type_name_input)
        registerMaintenanceTypeButton = findViewById(R.id.register_maintenance_type_button)

        // Load fragments
        val topAppBarFragment = TopAppBarFragment()
        loadFragment(topAppBarFragment, R.id.topAppBarFragmentContainer)
        loadFragment(BottomNavigationAdminFragment().apply {
            arguments = Bundle().apply {
                putInt("selected_item", R.id.nav_admin)
            }
        }, R.id.bottomNavigationContainer)

        // Set title after fragment is loaded
        topAppBarFragment.arguments = Bundle().apply {
            putBoolean("show_back_button", true)
            putString("title", getString(R.string.title_register_maintenance_type))
        }

        setupRegisterButton()
    }

    private fun setupRegisterButton() {
        registerMaintenanceTypeButton.setOnClickListener {
            if (validateMaintenanceTypeInputs()) {
                registerMaintenanceType()
            }
        }
    }

    private fun validateMaintenanceTypeInputs(): Boolean {
        val maintenanceTypeName = maintenanceTypeNameInput.text.toString().trim()

        if (maintenanceTypeName.isEmpty()) {
            maintenanceTypeNameInput.error = getString(R.string.error_empty_maintenance_type_name)
            return false
        }

        return true
    }

    private fun registerMaintenanceType() {
        val maintenanceTypeName = maintenanceTypeNameInput.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = maintenanceTypeRepository.addMaintenanceType(maintenanceTypeName)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@RegisterMaintenanceTypeActivity,
                                getString(R.string.success_maintenance_type_registered),
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@RegisterMaintenanceTypeActivity,
                                getString(R.string.error_maintenance_type_registration),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RegisterMaintenanceTypeActivity,
                        getString(R.string.error_maintenance_type_registration),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment, containerId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }
} 