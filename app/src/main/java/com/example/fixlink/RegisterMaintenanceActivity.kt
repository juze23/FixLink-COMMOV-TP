package com.example.fixlink

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.TopAppBarFragment
import com.example.fixlink.BottomNavigationFragment
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Type_maintenance
import com.example.fixlink.data.repository.EquipmentRepository
import com.example.fixlink.data.repository.LocationRepository
import com.example.fixlink.data.repository.MaintenanceRepository
import com.example.fixlink.data.repository.PriorityRepository
import com.example.fixlink.data.repository.MaintenanceTypeRepository
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import android.annotation.SuppressLint

class RegisterMaintenanceActivity : AppCompatActivity() {

    private lateinit var equipmentSpinner: Spinner
    private lateinit var prioritySpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button

    private val equipmentRepository = EquipmentRepository()
    private val priorityRepository = PriorityRepository()
    private val locationRepository = LocationRepository()
    private val maintenanceTypeRepository = MaintenanceTypeRepository()
    private val maintenanceRepository = MaintenanceRepository()

    private var equipmentList: List<Equipment> = emptyList()
    private var priorityList: List<Priority> = emptyList()
    private var locationList: List<Location> = emptyList()
    private var typeList: List<Type_maintenance> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_maintenance)
        // Further setup like finding views and setting listeners will go here

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationFragment())
            }
        }

        initializeViews()
        setupSpinners()
        loadAuxiliaryData()
        setupSubmitButton()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        equipmentSpinner = findViewById(R.id.equipment_spinner)
        prioritySpinner = findViewById(R.id.priority_spinner)
        locationSpinner = findViewById(R.id.location_spinner)
        typeSpinner = findViewById(R.id.type_spinner)
        titleEditText = findViewById(R.id.title_edit_text)
        descriptionEditText = findViewById(R.id.description_edit_text)
        submitButton = findViewById(R.id.submit_button)
    }

    private fun setupSpinners() {
        // Setup equipment spinner
        equipmentSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Choose equipment")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Setup priority spinner
        prioritySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Choose priority")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Setup location spinner
        locationSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Choose location")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Setup type spinner
        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Choose maintenance type")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun loadAuxiliaryData() {
        lifecycleScope.launch {
            try {
                // Load equipment
                equipmentRepository.getEquipmentList().onSuccess { equipment ->
                    equipmentList = equipment
                    val equipmentNames = listOf("Choose equipment") + equipment.map { it.name }
                    (equipmentSpinner.adapter as ArrayAdapter<String>).apply {
                        clear()
                        addAll(equipmentNames)
                        notifyDataSetChanged()
                    }
                }

                // Load priorities
                priorityRepository.getPriorityList().onSuccess { priorities ->
                    priorityList = priorities
                    val priorityNames = listOf("Choose priority") + priorities.map { it.priority }
                    (prioritySpinner.adapter as ArrayAdapter<String>).apply {
                        clear()
                        addAll(priorityNames)
                        notifyDataSetChanged()
                    }
                }

                // Load locations
                locationRepository.getLocationList().onSuccess { locations ->
                    locationList = locations
                    val locationNames = listOf("Choose location") + locations.map { it.name }
                    (locationSpinner.adapter as ArrayAdapter<String>).apply {
                        clear()
                        addAll(locationNames)
                        notifyDataSetChanged()
                    }
                }

                // Load maintenance types
                maintenanceTypeRepository.getMaintenanceTypes().onSuccess { types ->
                    typeList = types
                    val typeNames = listOf("Choose maintenance type") + types.map { it.type }
                    (typeSpinner.adapter as ArrayAdapter<String>).apply {
                        clear()
                        addAll(typeNames)
                        notifyDataSetChanged()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterMaintenanceActivity, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            if (validateForm()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    submitMaintenance()
                } else {
                    Toast.makeText(this, "This app requires Android 8.0 or higher", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (titleEditText.text.toString().trim().isEmpty()) {
            titleEditText.error = "Title is required"
            isValid = false
        }

        if (descriptionEditText.text.toString().trim().isEmpty()) {
            descriptionEditText.error = "Description is required"
            isValid = false
        }

        if (equipmentSpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select equipment", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (prioritySpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select priority", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (locationSpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select location", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (typeSpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select maintenance type", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    @SuppressLint("NewApi")
    private fun submitMaintenance() {
        val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.id
        val selectedEquipment = equipmentList.getOrNull(equipmentSpinner.selectedItemPosition - 1)
        val selectedPriority = priorityList.getOrNull(prioritySpinner.selectedItemPosition - 1)
        val selectedLocation = locationList.getOrNull(locationSpinner.selectedItemPosition - 1)
        val selectedType = typeList.getOrNull(typeSpinner.selectedItemPosition - 1)

        if (selectedEquipment == null || selectedPriority == null || selectedLocation == null || selectedType == null) {
            Toast.makeText(this, "Please select all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val equipmentId = selectedEquipment.equipment_id ?: run {
            Toast.makeText(this, "Invalid equipment selected", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val result = maintenanceRepository.createMaintenance(
                    userId = userId,
                    equipmentId = equipmentId,
                    title = title,
                    description = description,
                    locationId = selectedLocation.location_id,
                    priorityId = selectedPriority.priority_id,
                    typeId = selectedType.type_id
                )

                result.onSuccess {
                    Toast.makeText(this@RegisterMaintenanceActivity, "Maintenance scheduled successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { error ->
                    Toast.makeText(this@RegisterMaintenanceActivity, "Error scheduling maintenance: ${error.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterMaintenanceActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setLabelColors() {
        val purpleColor = ContextCompat.getColor(this, R.color.purple_primary)

        val titleLabel = findViewById<TextView>(R.id.title_label)
        val descriptionLabel = findViewById<TextView>(R.id.description_label)
        val locationLabel = findViewById<TextView>(R.id.location_label)
        val equipmentLabel = findViewById<TextView>(R.id.equipment_label)
        val priorityLabel = findViewById<TextView>(R.id.priority_label)

        titleLabel.setTextColor(purpleColor)
        descriptionLabel.setTextColor(purpleColor)
        locationLabel.setTextColor(purpleColor)
        equipmentLabel.setTextColor(purpleColor)
        priorityLabel.setTextColor(purpleColor)
    }
}