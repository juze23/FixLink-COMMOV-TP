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

class AdminActivity : AppCompatActivity() {

    private lateinit var techniciansListContainer: LinearLayout
    private lateinit var equipmentsListContainer: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var filterIcon: ImageView
    private lateinit var viewAllTechnicians: TextView
    private lateinit var viewAllEquipments: TextView
    private lateinit var addFab: FloatingActionButton
    private lateinit var icon_profile: ImageView

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

        // Populate lists with temporary data
        populateTechniciansList()
        populateEquipmentsList()

        // Set click listeners
        viewAllTechnicians.setOnClickListener { navigateToViewAll("technicians") }
        viewAllEquipments.setOnClickListener { navigateToViewAll("equipments") }
        addFab.setOnClickListener { navigateToAddItem() }
        icon_profile.setOnClickListener { navigateToProfile() }
        // Add listeners for search and filter if needed

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
            // Add edit and delete icon handling here

            nameTextView.text = equipment
            equipmentsListContainer.addView(equipmentItemView)
        }
    }

    private fun navigateToViewAll(listType: String) {
        // Implement navigation to a separate "View All" screen for technicians or equipments
        // Pass listType to indicate which list to display
    }

    private fun navigateToAddItem() {
        // Implement navigation to the "Register User" or "Register Equipment" page based on the FAB's context or a choice mechanism
        val intent = Intent(this, RegisterUserActivity::class.java) // Example: navigate to RegisterUserActivity
        startActivity(intent)
    }

    private fun navigateToProfile() {
        // Implement navigation to the Profile page
         val intent = Intent(this, ProfileActivity::class.java) // Assuming you have a ProfileActivity
         startActivity(intent)
    }
}