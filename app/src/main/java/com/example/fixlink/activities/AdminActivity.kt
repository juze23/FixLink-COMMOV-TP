package com.example.fixlink.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.commit
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ImageView
import android.view.View
import android.content.Intent
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.activity.addCallback
import android.widget.Toast
import com.example.fixlink.data.entities.User
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.repository.EquipmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.enableEdgeToEdge
import com.example.fixlink.fragments.AdminFragment
import android.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.fragments.TopAppBarFragment
import com.example.fixlink.adapters.LocationAdapter
import com.example.fixlink.data.entities.Issue_type
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.Type_maintenance
import com.example.fixlink.data.repository.MaintenanceTypeRepository
import com.example.fixlink.data.repository.IssueTypeRepository
import com.example.fixlink.data.repository.LocationRepository
import com.example.fixlink.fragments.EditTypeFragment
import com.example.fixlink.fragments.EditLocationFragment
import com.example.fixlink.fragments.ViewAllListFragment
import com.example.fixlink.adapters.MaintenanceTypeAdapter
import com.example.fixlink.adapters.IssueTypeAdapter
import com.example.fixlink.fragments.BottomNavigationAdminFragment
import android.widget.Button
import com.example.fixlink.adapters.TechnicianAdapter
import com.example.fixlink.fragments.EditEquipmentFragment



class AdminActivity : AppCompatActivity() {

    private lateinit var techniciansListContainer: LinearLayout
    private lateinit var equipmentsListContainer: LinearLayout
    private lateinit var maintenanceTypesListContainer: LinearLayout
    private lateinit var issueTypesListContainer: LinearLayout
    private lateinit var locationsListContainer: LinearLayout
    private lateinit var viewAllTechnicians: TextView
    private lateinit var viewAllEquipments: TextView
    private lateinit var viewAllMaintenanceTypes: TextView
    private lateinit var viewAllIssueTypes: TextView
    private lateinit var viewAllLocations: TextView
    private lateinit var addFab: FloatingActionButton
    private lateinit var editFragmentContainer: FrameLayout
    private lateinit var contentScrollView: ScrollView
    private lateinit var viewAllListFragmentContainer: FrameLayout
    private lateinit var fragmentContainer: FrameLayout

    private val userRepository = UserRepository()
    private val equipmentRepository = EquipmentRepository()
    private lateinit var maintenanceTypeRepository: MaintenanceTypeRepository
    private lateinit var issueTypeRepository: IssueTypeRepository
    private lateinit var locationRepository: LocationRepository

    // Add state tracking for expanded cards
    private var isTechniciansExpanded = false
    private var isEquipmentsExpanded = false
    private var isMaintenanceTypesExpanded = false
    private var isIssueTypesExpanded = false
    private var isLocationsExpanded = false

    // Add properties to store full lists
    private var allTechnicians: List<User> = emptyList()
    private var allEquipments: List<Equipment> = emptyList()
    private var allMaintenanceTypes: List<Type_maintenance> = emptyList()
    private var allIssueTypes: List<Issue_type> = emptyList()
    private var allLocations: List<Location> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        // Configure window to prevent premature destruction
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setCustomAnimations(0, 0, 0, 0)
                add(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment().apply {
                    arguments = Bundle().apply {
                        putInt("selected_item", R.id.nav_admin)
                    }
                })
                replace(R.id.viewAllListFragmentContainer, AdminFragment())
            }
        }

        // Initialize repositories
        maintenanceTypeRepository = MaintenanceTypeRepository()
        issueTypeRepository = IssueTypeRepository()
        locationRepository = LocationRepository()

        // Initialize views
        techniciansListContainer = findViewById(R.id.techniciansListContainer)
        equipmentsListContainer = findViewById(R.id.equipmentsListContainer)
        maintenanceTypesListContainer = findViewById(R.id.maintenanceTypesListContainer)
        issueTypesListContainer = findViewById(R.id.issueTypesListContainer)
        locationsListContainer = findViewById(R.id.locationsListContainer)
        viewAllTechnicians = findViewById(R.id.viewAllTechnicians)
        viewAllEquipments = findViewById(R.id.viewAllEquipments)
        viewAllMaintenanceTypes = findViewById(R.id.viewAllMaintenanceTypes)
        viewAllIssueTypes = findViewById(R.id.viewAllIssueTypes)
        viewAllLocations = findViewById(R.id.viewAllLocations)
        addFab = findViewById(R.id.addFab)
        editFragmentContainer = findViewById(R.id.editFragmentContainer)
        contentScrollView = findViewById(R.id.contentScrollView)
        viewAllListFragmentContainer = findViewById(R.id.viewAllListFragmentContainer)
        fragmentContainer = findViewById(R.id.fragmentContainer)

        // Set up click listeners
        viewAllMaintenanceTypes.setOnClickListener { showViewAllListFragment("maintenance_types") }
        viewAllIssueTypes.setOnClickListener { showViewAllListFragment("issue_types") }
        viewAllLocations.setOnClickListener { showViewAllListFragment("locations") }
        addFab.setOnClickListener { navigateToAddItem() }

        // Load data
        loadTechnicians()
        loadEquipments()
        loadMaintenanceTypes()
        loadIssueTypes()
        loadLocations()

        // Handle back button press
        onBackPressedDispatcher.addCallback(this) {
            if (viewAllListFragmentContainer.visibility == View.VISIBLE) {
                hideViewAllListFragment()
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
                isEnabled = true
            }
        }

        // Update click listeners for view all buttons
        viewAllTechnicians.setOnClickListener { showViewAllListFragment("technicians") }
        viewAllEquipments.setOnClickListener { showViewAllListFragment("equipments") }
        viewAllMaintenanceTypes.setOnClickListener { showViewAllListFragment("maintenance_types") }
        viewAllIssueTypes.setOnClickListener { showViewAllListFragment("issue_types") }
        viewAllLocations.setOnClickListener { showViewAllListFragment("locations") }
    }

    private fun loadTechnicians() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usersResult = userRepository.getAllUsers()
                if (usersResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminActivity, "Error loading technicians", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val technicians = usersResult.getOrNull()?.filter { it.typeId == 2 } ?: emptyList()
                withContext(Dispatchers.Main) {
                    populateTechniciansList(technicians)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminActivity, "Error loading technicians: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadEquipments() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val equipmentsResult = equipmentRepository.getEquipmentList()
                if (equipmentsResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminActivity, "Error loading equipments", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val equipments = equipmentsResult.getOrNull() ?: emptyList()
                withContext(Dispatchers.Main) {
                    populateEquipmentsList(equipments)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminActivity, "Error loading equipments: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMaintenanceTypes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = maintenanceTypeRepository.getMaintenanceTypes()
                if (result.isSuccess) {
                    val types = result.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        populateMaintenanceTypesList(types)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminActivity, "Error loading maintenance types", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadIssueTypes() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = issueTypeRepository.getIssueTypes()
                if (result.isSuccess) {
                    val types = result.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        populateIssueTypesList(types)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminActivity, "Error loading issue types", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = locationRepository.getLocationList()
                if (result.isSuccess) {
                    val locations = result.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        populateLocationsList(locations)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AdminActivity, "Error loading locations", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateTechniciansList(technicians: List<User>) {
        techniciansListContainer.removeAllViews()
        val recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = TechnicianAdapter { technician ->
                // Handle technician click if needed
            }.apply {
                submitList(technicians.take(4))
            }
        }
        techniciansListContainer.addView(recyclerView)
        viewAllTechnicians.text = getString(R.string.text_view_all_count, technicians.size)
    }

    private fun populateEquipmentsList(equipments: List<Equipment>) {
        val inflater = layoutInflater
        equipmentsListContainer.removeAllViews()

        // Show only first 4 equipments in the main view
        val equipmentsToShow = equipments.take(4)
        
        for (equipment in equipmentsToShow) {
            val equipmentItemView = inflater.inflate(R.layout.list_item_admin, equipmentsListContainer, false)
            val nameTextView = equipmentItemView.findViewById<TextView>(R.id.itemNameTextView)
            val editIcon = equipmentItemView.findViewById<ImageView>(R.id.editIcon)

            nameTextView.text = equipment.name

            // Keep edit icon for equipment
            editIcon.setOnClickListener { 
                showEditFragment(equipment.name, "equipment")
            }

            equipmentsListContainer.addView(equipmentItemView)
        }

        // Update "View All" text to show total count
        viewAllEquipments.text = getString(R.string.text_view_all_count, equipments.size)
    }

    private fun populateMaintenanceTypesList(types: List<Type_maintenance>) {
        maintenanceTypesListContainer.removeAllViews()
        val inflater = layoutInflater
        
        // Show only first 4 types in the main view
        val typesToShow = types.take(4)
        
        for (type in typesToShow) {
            val typeItemView = inflater.inflate(R.layout.list_item_admin, maintenanceTypesListContainer, false)
            val nameTextView = typeItemView.findViewById<TextView>(R.id.itemNameTextView)
            val editIcon = typeItemView.findViewById<ImageView>(R.id.editIcon)

            nameTextView.text = type.type

            // Keep edit icon for type
            editIcon.setOnClickListener { 
                showEditFragment(type.type, "maintenance_type", type.type_id)
            }

            maintenanceTypesListContainer.addView(typeItemView)
        }

        // Update "View All" text to show total count
        viewAllMaintenanceTypes.text = getString(R.string.text_view_all_count, types.size)
    }

    private fun populateIssueTypesList(types: List<Issue_type>) {
        issueTypesListContainer.removeAllViews()
        val inflater = layoutInflater
        
        // Show only first 4 types in the main view
        val typesToShow = types.take(4)
        
        for (type in typesToShow) {
            val typeItemView = inflater.inflate(R.layout.list_item_admin, issueTypesListContainer, false)
            val nameTextView = typeItemView.findViewById<TextView>(R.id.itemNameTextView)
            val editIcon = typeItemView.findViewById<ImageView>(R.id.editIcon)

            nameTextView.text = type.type

            // Keep edit icon for type
            editIcon.setOnClickListener { 
                showEditFragment(type.type, "issue_type", type.type_id)
            }

            issueTypesListContainer.addView(typeItemView)
        }

        // Update "View All" text to show total count
        viewAllIssueTypes.text = getString(R.string.text_view_all_count, types.size)
    }

    private fun populateLocationsList(locations: List<Location>) {
        locationsListContainer.removeAllViews()
        val inflater = layoutInflater
        
        // Show only first 4 locations in the main view
        val locationsToShow = locations.take(4)
        
        for (location in locationsToShow) {
            val locationItemView = inflater.inflate(R.layout.list_item_admin, locationsListContainer, false)
            val nameTextView = locationItemView.findViewById<TextView>(R.id.itemNameTextView)
            val editIcon = locationItemView.findViewById<ImageView>(R.id.editIcon)

            nameTextView.text = location.name

            // Keep edit icon for location
            editIcon.setOnClickListener { 
                showEditFragment(location.name, "location", location.location_id)
            }

            locationsListContainer.addView(locationItemView)
        }

        // Update "View All" text to show total count
        viewAllLocations.text = getString(R.string.text_view_all_count, locations.size)
    }

    companion object {
        // Remove unused constants
    }

    private fun showViewAllListFragment(listType: String) {
        contentScrollView.visibility = View.GONE
        viewAllListFragmentContainer.visibility = View.VISIBLE

        val title = when (listType) {
            "technicians" -> resources.getString(R.string.text_technicians)
            "equipments" -> resources.getString(R.string.text_equipments)
            "maintenance_types" -> resources.getString(R.string.text_maintenance_types)
            "issue_types" -> resources.getString(R.string.text_issue_types)
            "locations" -> resources.getString(R.string.text_locations)
            else -> ""
        }

        val fragment = ViewAllListFragment.newInstance(listType, title)

        supportFragmentManager.commit {
            setCustomAnimations(0, 0, 0, 0)
            replace(R.id.viewAllListFragmentContainer, fragment)
            addToBackStack(null)
        }
    }

    fun hideViewAllListFragment() {
        supportFragmentManager.popBackStack()
        viewAllListFragmentContainer.visibility = View.GONE
        contentScrollView.visibility = View.VISIBLE
    }

    private fun navigateToAddItem() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btnAddUser).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, RegisterUserActivity::class.java)
            startActivity(intent)
        }

        dialogView.findViewById<Button>(R.id.btnAddEquipment).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, RegisterEquipmentActivity::class.java)
            startActivity(intent)
        }

        dialogView.findViewById<Button>(R.id.btnAddLocation).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, RegisterLocationActivity::class.java)
            startActivity(intent)
        }

        dialogView.findViewById<Button>(R.id.btnAddMaintenanceType).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, RegisterMaintenanceTypeActivity::class.java)
            startActivity(intent)
        }

        dialogView.findViewById<Button>(R.id.btnAddIssueType).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, RegisterIssueTypeActivity::class.java)
            startActivity(intent)
        }

        dialog.show()
    }

    fun showEditFragment(itemName: String, itemType: String, itemId: Int? = null) {
        val fragment = when (itemType) {
            "equipment" -> {
                EditEquipmentFragment().apply {
                    arguments = Bundle().apply {
                        putString("equipmentName", itemName)
                    }
                }
            }
            "maintenance_type" -> {
                if (itemId == null) {
                    Toast.makeText(this, getString(R.string.error_type_not_found), Toast.LENGTH_SHORT).show()
                    return
                }
                EditTypeFragment.newInstance(itemId, itemName, "maintenance")
            }
            "issue_type" -> {
                if (itemId == null) {
                    Toast.makeText(this, getString(R.string.error_type_not_found), Toast.LENGTH_SHORT).show()
                    return
                }
                EditTypeFragment.newInstance(itemId, itemName, "issue")
            }
            "location" -> {
                if (itemId == null) {
                    Toast.makeText(this, getString(R.string.error_location_not_found), Toast.LENGTH_SHORT).show()
                    return
                }
                EditLocationFragment.newInstance(itemId, itemName)
            }
            else -> return
        }

        // Show the fragment in the editFragmentContainer
        editFragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.editFragmentContainer, fragment)
            addToBackStack("edit_fragment")
        }
    }

    fun hideEditFragment() {
        try {
            val existingFragment = supportFragmentManager.findFragmentById(R.id.editFragmentContainer)
            if (existingFragment != null) {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    remove(existingFragment)
                }
            }
            editFragmentContainer.visibility = View.GONE
            editFragmentContainer.removeAllViews()
            // Refresh all lists after hiding edit fragment
            refreshAllLists()
        } catch (e: Exception) {
            // Log the error but don't crash
            android.util.Log.e("AdminActivity", "Error hiding edit fragment", e)
        }
    }

    fun refreshAllLists() {
        loadTechnicians()
        loadEquipments()
        loadMaintenanceTypes()
        loadIssueTypes()
        loadLocations()
    }

    override fun onResume() {
        super.onResume()
        // Refresh all lists when activity resumes (e.g., after returning from register activities)
        refreshAllLists()
    }

    override fun onBackPressed() {
        if (editFragmentContainer.visibility == View.VISIBLE) {
            hideEditFragment()
        } else {
            super.onBackPressed()
        }
    }
}