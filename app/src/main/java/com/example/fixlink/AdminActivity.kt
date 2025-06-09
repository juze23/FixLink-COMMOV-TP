package com.example.fixlink

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fixlink.TopAppBarFragment
import com.example.fixlink.BottomNavigationAdminFragment
import androidx.fragment.app.commit
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.EditText
import android.widget.ImageView
import android.view.View
import android.content.Intent
import android.widget.FrameLayout
import androidx.fragment.app.replace
import androidx.fragment.app.add
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

class AdminActivity : AppCompatActivity() {

    private lateinit var techniciansListContainer: LinearLayout
    private lateinit var equipmentsListContainer: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var filterIcon: ImageView
    private lateinit var viewAllTechnicians: TextView
    private lateinit var viewAllEquipments: TextView
    private lateinit var addFab: FloatingActionButton
    private lateinit var icon_profile: ImageView
    private lateinit var editFragmentContainer: FrameLayout
    private lateinit var contentScrollView: ScrollView
    private lateinit var viewAllListFragmentContainer: FrameLayout

    private val userRepository = UserRepository()
    private val equipmentRepository = EquipmentRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment().apply {
                    arguments = Bundle().apply {
                        putInt("selected_item", R.id.nav_admin)
                    }
                })
                replace(R.id.viewAllListFragmentContainer, AdminFragment())
            }
        }

        // Initialize views
        techniciansListContainer = findViewById(R.id.techniciansListContainer)
        equipmentsListContainer = findViewById(R.id.equipmentsListContainer)
        searchEditText = findViewById(R.id.searchEditText)
        filterIcon = findViewById(R.id.filterIcon)
        viewAllTechnicians = findViewById(R.id.viewAllTechnicians)
        viewAllEquipments = findViewById(R.id.viewAllEquipments)
        addFab = findViewById(R.id.addFab)
        icon_profile = findViewById(R.id.icon_profile)
        editFragmentContainer = findViewById(R.id.editFragmentContainer)
        contentScrollView = findViewById(R.id.contentScrollView)
        viewAllListFragmentContainer = findViewById(R.id.viewAllListFragmentContainer)

        // Load real data
        loadTechnicians()
        loadEquipments()

        // Set click listeners
        viewAllTechnicians.setOnClickListener { showViewAllListFragment("technicians") }
        viewAllEquipments.setOnClickListener { showViewAllListFragment("equipments") }
        addFab.setOnClickListener { navigateToAddItem() }
        icon_profile.setOnClickListener { 
            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("FROM_ADMIN", true)
            }
            startActivity(intent)
        }
        // Add listeners for search and filter if needed

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle back button press
        onBackPressedDispatcher.addCallback(this) {
            if (viewAllListFragmentContainer.visibility == View.VISIBLE) {
                hideViewAllListFragment()
            } else {
                // If viewAllListFragment is not visible, allow default back behavior (e.g., exit activity)
                isEnabled = false // Disable this callback temporarily
                onBackPressedDispatcher.onBackPressed() // Call default back behavior
                isEnabled = true // Re-enable the callback
            }
        }
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

    private fun populateTechniciansList(technicians: List<User>) {
        val inflater = layoutInflater
        techniciansListContainer.removeAllViews()

        // Show only first 4 technicians in the main view
        val techniciansToShow = technicians.take(4)
        
        for (technician in techniciansToShow) {
            val technicianItemView = inflater.inflate(R.layout.list_item_admin, techniciansListContainer, false)
            val nameTextView = technicianItemView.findViewById<TextView>(R.id.itemNameTextView)
            val editIcon = technicianItemView.findViewById<ImageView>(R.id.editIcon)
            val deleteIcon = technicianItemView.findViewById<ImageView>(R.id.deleteIcon)

            nameTextView.text = "${technician.firstname} ${technician.lastname}"

            // TODO: Implement edit and delete functionality
            editIcon.setOnClickListener {
                // Handle edit technician
            }

            deleteIcon.setOnClickListener {
                // Handle delete technician
            }

            techniciansListContainer.addView(technicianItemView)
        }

        // Update "View All" text to show total count
        viewAllTechnicians.text = "View All (${technicians.size})"
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
            val deleteIcon = equipmentItemView.findViewById<ImageView>(R.id.deleteIcon)

            nameTextView.text = equipment.name

            editIcon.setOnClickListener { 
                showEditFragment(equipment.name)
            }

            deleteIcon.setOnClickListener {
                // Handle delete equipment
            }

            equipmentsListContainer.addView(equipmentItemView)
        }

        // Update "View All" text to show total count
        viewAllEquipments.text = "View All (${equipments.size})"
    }

    private fun showViewAllListFragment(listType: String) {
        contentScrollView.visibility = View.GONE
        viewAllListFragmentContainer.visibility = View.VISIBLE

        supportFragmentManager.commit {
            replace(R.id.viewAllListFragmentContainer, ViewAllListFragment.newInstance(listType))
        }
    }

    private fun hideViewAllListFragment() {
        val existingFragment = supportFragmentManager.findFragmentById(R.id.viewAllListFragmentContainer)
        if (existingFragment != null) {
            supportFragmentManager.commit {
                remove(existingFragment)
            }
        }
        viewAllListFragmentContainer.visibility = View.GONE
        contentScrollView.visibility = View.VISIBLE
    }

    private fun navigateToAddItem() {
        val intent = Intent(this, RegisterUserActivity::class.java)
        startActivity(intent)
    }

    private fun showEditFragment(equipmentName: String) {
        editFragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.commit {
            replace(R.id.editFragmentContainer, EditEquipmentFragment.newInstance())
            addToBackStack(null)
        }
    }

    fun hideEditFragment() {
        editFragmentContainer.visibility = View.GONE
        val existingFragment = supportFragmentManager.findFragmentById(R.id.editFragmentContainer)
        if (existingFragment != null) {
            supportFragmentManager.commit {
                remove(existingFragment)
            }
        }
    }
}