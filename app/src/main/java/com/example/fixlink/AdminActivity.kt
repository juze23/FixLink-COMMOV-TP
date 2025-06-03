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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.topAppBarFragmentContainer, TopAppBarFragment())
                replace(R.id.bottomNavigationContainer, BottomNavigationAdminFragment())
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

        // Populate lists with temporary data
        populateTechniciansList()
        populateEquipmentsList()

        // Set click listeners
        viewAllTechnicians.setOnClickListener { showViewAllListFragment("technicians") }
        viewAllEquipments.setOnClickListener { showViewAllListFragment("equipments") }
        addFab.setOnClickListener { navigateToAddItem() }
        icon_profile.setOnClickListener { navigateToProfile() }
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

    private fun populateTechniciansList() {
        // Temporary data - replace with actual data fetching
        val technicians = listOf("Luís Fernandes", "Luís Santos", "João Silva", "Tiago Gomes")
        val inflater = layoutInflater

        techniciansListContainer.removeAllViews() // Clear previous views
        for (technician in technicians) {
            val technicianItemView = inflater.inflate(R.layout.list_item_admin, techniciansListContainer, false) // Assuming you have a list_item_admin.xml layout
            val nameTextView = technicianItemView.findViewById<TextView>(R.id.itemNameTextView) // Assuming IDs in list_item_admin.xml
            // Add edit and delete icon handling here

            nameTextView.text = technician
            techniciansListContainer.addView(technicianItemView)
        }
    }

    private fun populateEquipmentsList() {
        // Temporary data - replace with actual data fetching
        val equipments = listOf("Fire Extinguisher", "Server Room UPS System", "Air Conditioning System", "Printer Paper")
        val inflater = layoutInflater

        equipmentsListContainer.removeAllViews() // Clear previous views
        for (equipment in equipments) {
            val equipmentItemView = inflater.inflate(R.layout.list_item_admin, equipmentsListContainer, false) // Assuming list_item_admin.xml also works for equipments or create a separate one
            val nameTextView = equipmentItemView.findViewById<TextView>(R.id.itemNameTextView) // Assuming IDs in list_item_admin.xml
            val editIcon = equipmentItemView.findViewById<ImageView>(R.id.editIcon)
            val deleteIcon = equipmentItemView.findViewById<ImageView>(R.id.deleteIcon)

            nameTextView.text = equipment

            editIcon.setOnClickListener { 
                showEditFragment(equipment)
            }

            equipmentsListContainer.addView(equipmentItemView)
        }
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

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
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