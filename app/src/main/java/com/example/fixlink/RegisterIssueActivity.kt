package com.example.fixlink

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.Issue_type
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.repository.EquipmentRepository
import com.example.fixlink.data.repository.PriorityRepository
import com.example.fixlink.data.repository.LocationRepository
import com.example.fixlink.data.repository.IssueTypeRepository
import com.example.fixlink.data.repository.StateIssueRepository
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class RegisterIssueActivity : AppCompatActivity() {

    private lateinit var equipmentSpinner: Spinner
    private lateinit var prioritySpinner: Spinner
    private lateinit var locationSpinner: Spinner
    private lateinit var issueTypeSpinner: Spinner
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var imageView: ImageView
    private lateinit var submitButton: Button
    private lateinit var issueRepository: IssueRepository
    private lateinit var equipmentRepository: EquipmentRepository
    private lateinit var priorityRepository: PriorityRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var issueTypeRepository: IssueTypeRepository
    private lateinit var stateIssueRepository: StateIssueRepository
    
    private var currentPhotoPath: String? = null
    private var selectedImageUri: Uri? = null
    private var equipmentList: List<Equipment> = emptyList()
    private var priorityList: List<Priority> = emptyList()
    private var locationList: List<Location> = emptyList()
    private var issueTypeList: List<Issue_type> = emptyList()

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri?.let { uri ->
                Log.d("RegisterIssueActivity", "Loading image from camera: $uri")
                try {
                    Glide.with(this)
                        .load(uri)
                        .apply(RequestOptions()
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.placeholder_printer_image)
                            .error(R.drawable.placeholder_printer_image))
                        .into(imageView)
                    
                    imageView.foreground = null
                } catch (e: Exception) {
                    Log.e("RegisterIssueActivity", "Error in image loading: ${e.message}", e)
                    Toast.makeText(this@RegisterIssueActivity, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    imageView.setImageResource(R.drawable.placeholder_printer_image)
                }
            }
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Log.d("RegisterIssueActivity", "Loading image from gallery: $uri")
                selectedImageUri = uri
                try {
                    Glide.with(this)
                        .load(uri)
                        .apply(RequestOptions()
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.drawable.placeholder_printer_image)
                            .error(R.drawable.placeholder_printer_image))
                        .into(imageView)
                    
                    imageView.foreground = null
                } catch (e: Exception) {
                    Log.e("RegisterIssueActivity", "Error in image loading: ${e.message}", e)
                    Toast.makeText(this@RegisterIssueActivity, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
                    imageView.setImageResource(R.drawable.placeholder_printer_image)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_issue)

        // Add fragments
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
            }

            // Add appropriate bottom navigation based on user type
            CoroutineScope(Dispatchers.Main).launch {
                val bottomNavFragment = withContext(Dispatchers.IO) {
                    NavigationUtils.getBottomNavigationFragment()
                }
                // Set the selected item to issues
                if (bottomNavFragment is BottomNavigationAdminFragment) {
                    bottomNavFragment.arguments = Bundle().apply {
                        putInt("selected_item", R.id.nav_issues)
                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.bottomNavigationContainer, bottomNavFragment)
                    .commit()
            }
        }

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        issueRepository = IssueRepository()
        equipmentRepository = EquipmentRepository()
        priorityRepository = PriorityRepository()
        locationRepository = LocationRepository()
        issueTypeRepository = IssueTypeRepository()
        stateIssueRepository = StateIssueRepository()
        initializeViews()
        loadData()
        setLabelColors()
        setupImageButton()
        setupSubmitButton()
    }

    private fun initializeViews() {
        Log.d("RegisterIssueActivity", "Initializing views")
        equipmentSpinner = findViewById(R.id.equipment_spinner)
        prioritySpinner = findViewById(R.id.priority_spinner)
        locationSpinner = findViewById(R.id.location_spinner)
        issueTypeSpinner = findViewById(R.id.issue_type_spinner)
        titleEditText = findViewById(R.id.title_input)
        descriptionEditText = findViewById(R.id.description_input)
        imageView = findViewById(R.id.add_image_placeholder)
        submitButton = findViewById(R.id.register_issue_button)
        Log.d("RegisterIssueActivity", "Views initialized")
    }

    private fun loadData() {
        val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()
        
        if (currentUser == null) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load equipment list
                val equipmentResult = equipmentRepository.getEquipmentList()
                
                equipmentResult.fold(
                    onSuccess = { equipment ->
                        equipmentList = equipment
                        withContext(Dispatchers.Main) {
                            try {
                                val adapter = ArrayAdapter(
                                    this@RegisterIssueActivity,
                                    android.R.layout.simple_spinner_item,
                                    listOf("Select equipment") + equipment.map { it.name }
                                ).apply {
                                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                }
                                equipmentSpinner.adapter = adapter
                            } catch (e: Exception) {
                                Log.e("RegisterIssueActivity", "Error setting adapter: ${e.message}", e)
                            }
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterIssueActivity", "Error loading equipment: ${error.message}", error)
                    }
                )

                // Load priority list
                priorityRepository.getPriorityList().fold(
                    onSuccess = { priorities ->
                        priorityList = priorities
                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(
                                this@RegisterIssueActivity,
                                android.R.layout.simple_spinner_item,
                                listOf("Select priority") + priorities.map { it.priority }
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            prioritySpinner.adapter = adapter
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterIssueActivity", "Error loading priorities: ${error.message}", error)
                    }
                )

                // Load location list
                locationRepository.getLocationList().fold(
                    onSuccess = { locations ->
                        locationList = locations
                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(
                                this@RegisterIssueActivity,
                                android.R.layout.simple_spinner_item,
                                listOf("Select location") + locations.map { it.name }
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            locationSpinner.adapter = adapter
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterIssueActivity", "Error loading locations: ${error.message}", error)
                    }
                )

                // Load issue types
                issueTypeRepository.getIssueTypes().fold(
                    onSuccess = { types ->
                        issueTypeList = types
                        withContext(Dispatchers.Main) {
                            val adapter = ArrayAdapter(
                                this@RegisterIssueActivity,
                                android.R.layout.simple_spinner_item,
                                listOf("Select issue type") + types.map { it.type }
                            ).apply {
                                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            }
                            issueTypeSpinner.adapter = adapter
                        }
                    },
                    onFailure = { error ->
                        Log.e("RegisterIssueActivity", "Error loading issue types: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e("RegisterIssueActivity", "Error loading data: ${e.message}", e)
            }
        }
    }

    private fun setupImageButton() {
        imageView.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
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
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
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

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            if (validateForm()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    submitIssue()
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

        if (issueTypeSpinner.selectedItemPosition == 0) {
            Toast.makeText(this, "Please select issue type", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun submitIssue() {
        val currentUser = SupabaseClient.supabase.auth.currentUserOrNull()
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUser.id
        val selectedEquipment = equipmentList.getOrNull(equipmentSpinner.selectedItemPosition - 1)
        val selectedPriority = priorityList.getOrNull(prioritySpinner.selectedItemPosition - 1)
        val selectedLocation = locationList.getOrNull(locationSpinner.selectedItemPosition - 1)
        val selectedIssueType = issueTypeList.getOrNull(issueTypeSpinner.selectedItemPosition - 1)

        if (selectedEquipment == null || selectedPriority == null || selectedLocation == null || selectedIssueType == null) {
            Toast.makeText(this, "Please select all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val title = titleEditText.text.toString()
        val description = descriptionEditText.text.toString()
        val equipmentId = selectedEquipment.equipment_id ?: run {
            Toast.makeText(this, "Invalid equipment selected", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = issueRepository.createIssue(
                    userId = userId,
                    equipmentId = equipmentId,
                    title = title,
                    description = description,
                    locationId = selectedLocation.location_id,
                    priorityId = selectedPriority.priority_id,
                    typeId = selectedIssueType.type_id,
                    imageUri = selectedImageUri,
                    context = this@RegisterIssueActivity
                )

                withContext(Dispatchers.Main) {
                    result.fold(
                        onSuccess = {
                            Toast.makeText(this@RegisterIssueActivity, "Issue registered successfully!", Toast.LENGTH_SHORT).show()
                            // Navigate to IssuesUserActivity after successful registration
                            val intent = Intent(this@RegisterIssueActivity, IssuesUserActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onFailure = { error ->
                            Toast.makeText(this@RegisterIssueActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterIssueActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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
        val issueTypeLabel = findViewById<TextView>(R.id.issue_type_label)

        titleLabel.setTextColor(purpleColor)
        descriptionLabel.setTextColor(purpleColor)
        locationLabel.setTextColor(purpleColor)
        equipmentLabel.setTextColor(purpleColor)
        priorityLabel.setTextColor(purpleColor)
        issueTypeLabel.setTextColor(purpleColor)
    }
} 