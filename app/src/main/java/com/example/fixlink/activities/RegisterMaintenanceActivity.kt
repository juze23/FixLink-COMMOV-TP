package com.example.fixlink

import android.Manifest
import androidx.appcompat.app.AlertDialog
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.example.fixlink.TopAppBarFragment
import com.example.fixlink.BottomNavigationFragment
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import android.util.Log
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.withContext
import com.example.fixlink.utils.NavigationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

class RegisterMaintenanceActivity : AppCompatActivity() {

    private lateinit var equipmentSpinner: Spinner
    private lateinit var prioritySpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var addImagePlaceholder: ImageView
    private var currentPhotoPath: String? = null
    private var selectedImageUri: Uri? = null

    private val equipmentRepository = EquipmentRepository()
    private val priorityRepository = PriorityRepository()
    private val locationRepository = LocationRepository()
    private val maintenanceTypeRepository = MaintenanceTypeRepository()
    private val maintenanceRepository = MaintenanceRepository()

    private var equipmentList: List<Equipment> = emptyList()
    private var priorityList: List<Priority> = emptyList()
    private var locationList: List<Location> = emptyList()
    private var typeList: List<Type_maintenance> = emptyList()

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, getString(R.string.error_camera_permission), Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri?.let { uri ->
                Log.d("RegisterMaintenanceActivity", "Loading image from camera: $uri")
                try {
                    loadImage(uri)
                } catch (e: Exception) {
                    Log.e("RegisterMaintenanceActivity", "Error loading image from camera: ${e.message}", e)
                    Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    addImagePlaceholder.setImageResource(R.drawable.ic_add)
                }
            }
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("RegisterMaintenanceActivity", "Loading image from gallery: $uri")
                selectedImageUri = uri
                try {
                    loadImage(uri)
                } catch (e: Exception) {
                    Log.e("RegisterMaintenanceActivity", "Error loading image from gallery: ${e.message}", e)
                    Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    addImagePlaceholder.setImageResource(R.drawable.ic_add)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_maintenance)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            }

            // Add appropriate bottom navigation based on user type
            CoroutineScope(Dispatchers.Main).launch {
                val bottomNavFragment = withContext(Dispatchers.IO) {
                    NavigationUtils.getBottomNavigationFragment()
                }
                // Set the selected item to maintenance
                if (bottomNavFragment is BottomNavigationAdminFragment) {
                    bottomNavFragment.arguments = Bundle().apply {
                        putInt("selected_item", R.id.nav_maintenance)
                    }
                } else if (bottomNavFragment is BottomNavigationFragment) {
                    bottomNavFragment.arguments = Bundle().apply {
                        putInt("selected_item", R.id.nav_maintenance)
                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                    .commit()
            }
        }

        initializeViews()
        setupSpinners()
        loadAuxiliaryData()
        setupSubmitButton()
        setupImageButton()

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
        submitButton = findViewById(R.id.register_maintenance_button)
        addImagePlaceholder = findViewById(R.id.add_image_placeholder)
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
                // Load equipment list
                equipmentRepository.getEquipmentList().fold(
                    onSuccess = { equipment ->
                        equipmentList = equipment
                        withContext(Dispatchers.Main) {
                            try {
                                val adapter = ArrayAdapter(
                                    this@RegisterMaintenanceActivity,
                                    android.R.layout.simple_spinner_item,
                                    listOf(getString(R.string.text_select_equipment)) + equipment.map { it.name }
                                ).apply {
                                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                }
                                equipmentSpinner.adapter = adapter
                            } catch (e: Exception) {
                                Log.e("RegisterMaintenanceActivity", "Error setting adapter: ${e.message}", e)
                            }
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterMaintenanceActivity", "Error loading equipment: ${error.message}", error)
                    }
                )

                // Load priority list
                priorityRepository.getPriorityList().fold(
                    onSuccess = { priorities ->
                        priorityList = priorities
                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(
                                this@RegisterMaintenanceActivity,
                                android.R.layout.simple_spinner_item,
                                listOf(getString(R.string.text_select_priority)) + priorities.map { it.priority }
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            prioritySpinner.adapter = adapter
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterMaintenanceActivity", "Error loading priorities: ${error.message}", error)
                    }
                )

                // Load location list
                locationRepository.getLocationList().fold(
                    onSuccess = { locations ->
                        locationList = locations
                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(
                                this@RegisterMaintenanceActivity,
                                android.R.layout.simple_spinner_item,
                                listOf(getString(R.string.text_select_location)) + locations.map { it.name }
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            locationSpinner.adapter = adapter
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterMaintenanceActivity", "Error loading locations: ${error.message}", error)
                    }
                )

                // Load maintenance types
                maintenanceTypeRepository.getMaintenanceTypes().fold(
                    onSuccess = { types ->
                        typeList = types
                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(
                                this@RegisterMaintenanceActivity,
                                android.R.layout.simple_spinner_item,
                                listOf(getString(R.string.text_select_maintenance_type)) + types.map { it.type }
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            typeSpinner.adapter = adapter
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterMaintenanceActivity", "Error loading maintenance types: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("RegisterMaintenanceActivity", "Error loading data: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterMaintenanceActivity, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
                }
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

    private fun setupImageButton() {
        addImagePlaceholder.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf(
            getString(R.string.dialog_take_photo),
            getString(R.string.dialog_choose_gallery),
            getString(R.string.dialog_cancel)
        )
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_add_photo_title))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndTakePicture()
                    1 -> openGallery()
                    2 -> return@setItems
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndTakePicture() {
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                dispatchTakePictureIntent()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            Toast.makeText(this, getString(R.string.error_creating_image), Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                it
            )
            selectedImageUri = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getContent.launch(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                    typeId = selectedType.type_id,
                    imageUri = selectedImageUri,
                    context = this@RegisterMaintenanceActivity
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

    private fun loadImage(uri: Uri) {
        try {
            Glide.with(this)
                .load(uri)
                .apply(RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.placeholder_printer_image)
                    .error(R.drawable.placeholder_printer_image))
                .into(addImagePlaceholder)
            
            addImagePlaceholder.foreground = null
        } catch (e: Exception) {
            Log.e("RegisterMaintenanceActivity", "Error loading image: ${e.message}", e)
            Toast.makeText(this, getString(R.string.error_loading_image, e.message), Toast.LENGTH_SHORT).show()
            addImagePlaceholder.setImageResource(R.drawable.ic_add)
        }
    }

    private fun loadImageFromFirebase(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .apply(RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .placeholder(R.drawable.placeholder_printer_image)
                .error(R.drawable.placeholder_printer_image))
            .into(addImagePlaceholder)
        
        addImagePlaceholder.foreground = null
    }

    override fun onResume() {
        super.onResume()
        // Show back button in top app bar
        val topAppBarFragment = supportFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.showBackButton()
    }
}