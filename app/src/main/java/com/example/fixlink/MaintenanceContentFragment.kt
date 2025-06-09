package com.example.fixlink

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.example.fixlink.ui.filters.MaintenanceFilterDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.data.repository.MaintenanceRepository
import com.example.fixlink.data.entities.Maintenance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.fixlink.data.repository.PriorityRepository
import com.example.fixlink.data.repository.EquipmentRepository
import com.example.fixlink.data.repository.LocationRepository
import com.example.fixlink.data.repository.StateMaintenanceRepository
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.State_maintenance
import com.example.fixlink.data.entities.User
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class MaintenanceContentFragment : Fragment() {
    companion object {
        private const val TAG = "MaintenanceContentFragment"
    }

    private lateinit var maintenanceRecyclerView: RecyclerView
    private lateinit var maintenanceAdapter: MaintenanceAdapter
    private lateinit var searchEditText: EditText
    private val maintenanceRepository = MaintenanceRepository()
    private val maintenanceList = mutableListOf<Maintenance>()
    private val filteredMaintenanceList = mutableListOf<Maintenance>()
    private val priorityRepository = PriorityRepository()
    private val equipmentRepository = EquipmentRepository()
    private val locationRepository = LocationRepository()
    private val stateMaintenanceRepository = StateMaintenanceRepository()
    private val userRepository = UserRepository()

    private var priorities: List<Priority> = emptyList()
    private var equipments: List<Equipment> = emptyList()
    private var locations: List<Location> = emptyList()
    private var states: List<State_maintenance> = emptyList()
    private var users: List<User> = emptyList()

    private lateinit var loadingProgressBar: View
    private lateinit var maintenanceContent: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maintenance_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val filterIcon: ImageView = view.findViewById(R.id.filterIcon)
        maintenanceRecyclerView = view.findViewById(R.id.maintenanceRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        maintenanceContent = view.findViewById(R.id.maintenanceContent)

        filterIcon.setOnClickListener {
            MaintenanceFilterDialogFragment().show(childFragmentManager, MaintenanceFilterDialogFragment.TAG)
        }

        // Setup RecyclerView
        maintenanceAdapter = MaintenanceAdapter(filteredMaintenanceList)
        maintenanceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        maintenanceRecyclerView.adapter = maintenanceAdapter

        // Setup search functionality
        setupSearch()

        // Show loading state
        showLoading(true)

        // Load auxiliary data and maintenance tasks
        loadAuxiliaryDataAndMaintenance()
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchMaintenances(s.toString())
            }
        })
    }

    private fun searchMaintenances(query: String) {
        filteredMaintenanceList.clear()
        if (query.isEmpty()) {
            filteredMaintenanceList.addAll(maintenanceList)
        } else {
            val searchQuery = query.lowercase()
            filteredMaintenanceList.addAll(maintenanceList.filter { maintenance ->
                // Search in title and description
                (maintenance.title?.lowercase()?.contains(searchQuery) == true) ||
                (maintenance.description?.lowercase()?.contains(searchQuery) == true) ||
                // Search in equipment name
                equipments.find { it.equipment_id == maintenance.id_equipment }?.name?.lowercase()?.contains(searchQuery) == true ||
                // Search in location name
                locations.find { it.location_id == maintenance.localization_id }?.name?.lowercase()?.contains(searchQuery) == true ||
                // Search in user name
                users.find { it.user_id == maintenance.id_user }?.let { "${it.firstname} ${it.lastname}" }?.lowercase()?.contains(searchQuery) == true ||
                // Search in state
                states.find { it.state_id == maintenance.state_id }?.state?.lowercase()?.contains(searchQuery) == true ||
                // Search in priority
                priorities.find { it.priority_id == maintenance.priority_id }?.priority?.lowercase()?.contains(searchQuery) == true
            })
        }
        maintenanceAdapter.notifyDataSetChanged()
    }

    private fun showLoading(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        maintenanceContent.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun loadAuxiliaryDataAndMaintenance() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting to load auxiliary data")
                val prioritiesResult = priorityRepository.getPriorityList()
                val equipmentsResult = equipmentRepository.getEquipmentList()
                val locationsResult = locationRepository.getLocationList()
                val statesResult = stateMaintenanceRepository.getMaintenanceStates()
                val usersResult = userRepository.getAllUsers()

                if (prioritiesResult.isFailure || equipmentsResult.isFailure || 
                    locationsResult.isFailure || statesResult.isFailure || usersResult.isFailure) {
                    Log.e(TAG, "Failed to load auxiliary data: " +
                        "Priorities: ${prioritiesResult.exceptionOrNull()?.message}, " +
                        "Equipments: ${equipmentsResult.exceptionOrNull()?.message}, " +
                        "Locations: ${locationsResult.exceptionOrNull()?.message}, " +
                        "States: ${statesResult.exceptionOrNull()?.message}, " +
                        "Users: ${usersResult.exceptionOrNull()?.message}")
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Error loading auxiliary data", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                priorities = prioritiesResult.getOrNull() ?: emptyList()
                equipments = equipmentsResult.getOrNull() ?: emptyList()
                locations = locationsResult.getOrNull() ?: emptyList()
                states = statesResult.getOrNull() ?: emptyList()
                users = usersResult.getOrNull() ?: emptyList()

                Log.d(TAG, "Successfully loaded auxiliary data: " +
                    "Priorities: ${priorities.size}, " +
                    "Equipments: ${equipments.size}, " +
                    "Locations: ${locations.size}, " +
                    "States: ${states.size}, " +
                    "Users: ${users.size}")

                withContext(Dispatchers.Main) {
                    loadMaintenance()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading auxiliary data", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadMaintenance() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting to load maintenance tasks")
                val result = maintenanceRepository.getAllMaintenance()
                withContext(Dispatchers.Main) {
                    result.onSuccess { maintenance ->
                        Log.d(TAG, "Successfully loaded ${maintenance.size} maintenance tasks")
                        maintenanceList.clear()
                        maintenanceList.addAll(maintenance)
                        filteredMaintenanceList.clear()
                        filteredMaintenanceList.addAll(maintenance)
                        maintenanceAdapter.setAuxiliaryData(priorities, equipments, locations, states, users)
                        maintenanceAdapter.notifyDataSetChanged()
                        showLoading(false)
                    }.onFailure { error ->
                        Log.e(TAG, "Failed to load maintenance tasks", error)
                        showLoading(false)
                        Toast.makeText(requireContext(), "Error loading maintenance tasks: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while loading maintenance tasks", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 