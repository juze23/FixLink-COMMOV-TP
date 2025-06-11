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
import java.text.SimpleDateFormat
import java.util.*
import com.example.fixlink.ui.filters.FilterConstants
import com.example.fixlink.ui.filters.MaintenanceFilterDialogFragment
import kotlinx.coroutines.Job

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
    private lateinit var fabAddMaintenance: FloatingActionButton
    private var isAdmin: Boolean = false
    private var isTechnician: Boolean = false

    private var currentPriority: String? = null
    private var currentState: String? = null
    private var currentDate: String? = null
    private var currentEquipmentStatus: Boolean? = null

    private var filterJob: Job? = null

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
        fabAddMaintenance = view.findViewById(R.id.fabAddMaintenance)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        maintenanceContent = view.findViewById(R.id.maintenanceContent)

        // Get current user role
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userResult = userRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    isAdmin = user?.typeId == 3  // 3 is the typeId for admin
                    isTechnician = user?.typeId == 2  // 2 is the typeId for technician
                    withContext(Dispatchers.Main) {
                        // Show FAB only for admin and technician users
                        fabAddMaintenance.visibility = if (isAdmin || isTechnician) View.VISIBLE else View.GONE
                    }
                    Log.d(TAG, "User typeId: ${user?.typeId}, isAdmin: $isAdmin, isTechnician: $isTechnician")
                } else {
                    Log.e(TAG, "Failed to get current user: ${userResult.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user role", e)
            }
        }

        filterIcon.setOnClickListener {
            showFilterDialog()
        }

        // Setup FAB click listener
        fabAddMaintenance.setOnClickListener {
            if (isAdmin || isTechnician) {
                val intent = Intent(requireContext(), RegisterMaintenanceActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "You don't have permission to register maintenance", Toast.LENGTH_SHORT).show()
            }
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
                applyFilters()
            }
        })
    }

    private fun applyFilters() {
        filterJob?.cancel()
        filterJob = CoroutineScope(Dispatchers.Main).launch {
            var filteredMaintenance = maintenanceList.toMutableList()

            //priority filter
            if (currentPriority != null) {
                Log.d(TAG, "Filtering by priority: $currentPriority")
                Log.d(TAG, "Available priorities: ${priorities.map { it.priority }}")
                val priorityId = priorities.find { it.priority == currentPriority }?.priority_id
                Log.d(TAG, "Found priority ID: $priorityId")
                filteredMaintenance = filteredMaintenance.filter { it.priority_id == priorityId }.toMutableList()
                Log.d(TAG, "Filtered maintenance count after priority filter: ${filteredMaintenance.size}")
            }

            //date filter
            if (currentDate != null) {
                try {
                    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = inputFormat.parse(currentDate)
                    val selectedDateStr = selectedDate?.let { outputFormat.format(it) }

                    filteredMaintenance = filteredMaintenance.filter { maintenance ->
                        val maintenanceDate = maintenance.publicationDate.split("T")[0] // Get just the date part
                        maintenanceDate == selectedDateStr
                    }.toMutableList()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing date: ${e.message}")
                }
            }

            //state filter
            if (currentState != null) {
                Log.d(TAG, "Filtering by state: $currentState")
                Log.d(TAG, "Available states: ${states.map { it.state }}")
                val stateId = states.find { it.state == currentState }?.state_id
                Log.d(TAG, "Found state ID: $stateId")
                filteredMaintenance = filteredMaintenance.filter { it.state_id == stateId }.toMutableList()
                Log.d(TAG, "Filtered maintenance count after state filter: ${filteredMaintenance.size}")
            }

            //equipment status filter
            if (currentEquipmentStatus != null) {
                filteredMaintenance = filteredMaintenance.filter { maintenance ->
                    val equipment = equipments.find { it.equipment_id == maintenance.id_equipment }
                    equipment?.active == currentEquipmentStatus
                }.toMutableList()
                Log.d(TAG, "Filtered maintenance count after equipment status filter: ${filteredMaintenance.size}")
            }

            //search filter
            val searchQuery = searchEditText.text.toString()
            if (searchQuery.isNotEmpty()) {
                filteredMaintenance = filteredMaintenance.filter { maintenance ->
                    (maintenance.description?.contains(searchQuery, ignoreCase = true) == true) ||
                    equipments.find { it.equipment_id == maintenance.id_equipment }?.name?.contains(searchQuery, ignoreCase = true) == true ||
                    locations.find { it.location_id == maintenance.localization_id }?.name?.contains(searchQuery, ignoreCase = true) == true ||
                    users.find { it.user_id == maintenance.id_user }?.let { user ->
                        if (user.lastname.isNullOrEmpty()) user.firstname else "${user.firstname} ${user.lastname}"
                    }?.contains(searchQuery, ignoreCase = true) == true ||
                    states.find { it.state_id == maintenance.state_id }?.state?.contains(searchQuery, ignoreCase = true) == true ||
                    priorities.find { it.priority_id == maintenance.priority_id }?.priority?.contains(searchQuery, ignoreCase = true) == true
                }.toMutableList()
            }

            //update the filtered list
            filteredMaintenanceList.clear()
            filteredMaintenanceList.addAll(filteredMaintenance)
            maintenanceAdapter.notifyDataSetChanged()
            Log.d(TAG, "Updated adapter with ${filteredMaintenance.size} items")
        }
    }

    private fun showFilterDialog() {
        val filterDialog = MaintenanceFilterDialogFragment.newInstance(
            priority = currentPriority,
            state = currentState,
            date = currentDate,
            equipmentStatus = currentEquipmentStatus
        ).apply {
            setPriorityCallback { priority ->
                currentPriority = priority
                applyFilters()
            }
            setStateCallback { state ->
                currentState = state
                applyFilters()
            }
            setDateCallback { date ->
                currentDate = date
                applyFilters()
            }
            setEquipmentStatusCallback { status ->
                currentEquipmentStatus = status
                applyFilters()
            }
            setClearCallback {
                clearFilters()
            }
        }

        filterDialog.show(childFragmentManager, MaintenanceFilterDialogFragment.TAG)
    }

    private fun clearFilters() {
        //clears stored filters
        currentPriority = null
        currentState = null
        currentDate = null
        currentEquipmentStatus = null

        //resets the list to show all items
        filteredMaintenanceList.clear()
        filteredMaintenanceList.addAll(maintenanceList)
        maintenanceAdapter.notifyDataSetChanged()
        Log.d(TAG, "Cleared filters, showing all ${maintenanceList.size} items")
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