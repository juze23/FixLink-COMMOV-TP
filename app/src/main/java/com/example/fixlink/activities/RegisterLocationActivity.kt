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
import com.example.fixlink.data.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.enableEdgeToEdge
import com.example.fixlink.R
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import com.example.fixlink.fragments.TopAppBarFragment

class RegisterLocationActivity : AppCompatActivity() {

    private lateinit var locationNameInput: EditText
    private lateinit var registerLocationButton: Button

    private val locationRepository = LocationRepository()

    companion object {
        private const val TAG = "RegisterLocationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_location)

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
        locationNameInput = findViewById(R.id.location_name_input)
        registerLocationButton = findViewById(R.id.register_location_button)

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
        registerLocationButton.setOnClickListener {
            if (validateLocationInputs()) {
                registerLocation()
            }
        }
    }

    private fun validateLocationInputs(): Boolean {
        val name = locationNameInput.text.toString().trim()
        if (name.isEmpty()) {
            locationNameInput.error = "Location name is required"
            return false
        }
        return true
    }

    private fun registerLocation() {
        val name = locationNameInput.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = locationRepository.registerLocation(name)
                result.fold(
                    onSuccess = {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterLocationActivity, "Location registered successfully!", Toast.LENGTH_SHORT).show()
                            // Clear inputs
                            locationNameInput.text.clear()
                        }
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@RegisterLocationActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterLocationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 