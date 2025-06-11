package com.example.fixlink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.repository.PriorityRepository
import com.example.fixlink.data.repository.EquipmentRepository
import com.example.fixlink.data.repository.LocationRepository
import com.example.fixlink.data.repository.StateIssueRepository
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.repository.MaintenanceRepository
import com.example.fixlink.data.entities.Issue
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.Issue_state
import com.example.fixlink.data.entities.User
import com.example.fixlink.data.entities.Maintenance
import com.example.fixlink.data.entities.State_maintenance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.fixlink.supabaseConfig.SupabaseClient
import com.example.fixlink.ui.filters.MyTasksFilterDialogFragment
import kotlinx.coroutines.Job
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class MyTasksFragment : Fragment() {
    private lateinit var issuesRecyclerView: RecyclerView
    private lateinit var issueAdapter: IssueAdapter
    private lateinit var searchEditText: EditText
    private val issueRepository = IssueRepository()
    private val issuesList = mutableListOf<Issue>()
    private val filteredIssuesList = mutableListOf<Issue>()
    private val priorityRepository = PriorityRepository()
    private val equipmentRepository = EquipmentRepository()
    private val locationRepository = LocationRepository()
    private val stateIssueRepository = StateIssueRepository()
    private val userRepository = UserRepository()
    private val maintenanceRepository = MaintenanceRepository()
    private val maintenanceList = mutableListOf<Maintenance>()
    private val filteredMaintenanceList = mutableListOf<Maintenance>()
    private var maintenanceStates: List<State_maintenance> = emptyList()

    private var priorities: List<Priority> = emptyList()
    private var equipments: List<Equipment> = emptyList()
    private var locations: List<Location> = emptyList()
    private var states: List<Issue_state> = emptyList()
    private var users: List<User> = emptyList()

    private lateinit var loadingProgressBar: View
    private lateinit var issuesContent: View
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var tasksAdapter: MyTasksAdapter

    private var currentFilterOptions = MyTasksFilterDialogFragment.FilterOptions()
    private var filterJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tasksRecyclerView = view.findViewById(R.id.issuesRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        issuesContent = view.findViewById(R.id.issuesContent)

        val filterIcon: ImageView = view.findViewById(R.id.filterIcon)
        filterIcon.setOnClickListener {
            showFilterDialog()
        }

        tasksAdapter = MyTasksAdapter(filteredIssuesList, filteredMaintenanceList, priorities, equipments, locations, states, users, maintenanceStates)
        tasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        tasksRecyclerView.adapter = tasksAdapter

        setupSearch()
        showLoading(true)
        loadAuxiliaryDataAndTasks()
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                searchTasks(s.toString())
            }
        })
    }

    private fun showFilterDialog() {
        val dialog = MyTasksFilterDialogFragment().apply {
            setCurrentFilterOptions(currentFilterOptions)
            setOnFilterAppliedListener { options ->
                currentFilterOptions = options
                applyFilters()
            }
        }
        dialog.show(childFragmentManager, MyTasksFilterDialogFragment.TAG)
    }

    private fun applyFilters() {
        filterJob?.cancel()
        filterJob = CoroutineScope(Dispatchers.Default).launch {
            val filteredIssues = issuesList.filter { issue ->
                var matches = true

                // Filter by task type
                if (!currentFilterOptions.showIssues) {
                    matches = false
                }

                // Filter by priority
                if (matches && currentFilterOptions.selectedPriority != null) {
                    matches = priorities.find { it.priority_id == issue.priority_id }?.priority == currentFilterOptions.selectedPriority
                    Log.d(TAG, "Priority filter: ${priorities.find { it.priority_id == issue.priority_id }?.priority} == ${currentFilterOptions.selectedPriority} -> $matches")
                }

                // Filter by state
                if (matches && currentFilterOptions.selectedState != null) {
                    matches = states.find { it.state_id == issue.state_id }?.state == currentFilterOptions.selectedState
                    Log.d(TAG, "State filter: ${states.find { it.state_id == issue.state_id }?.state} == ${currentFilterOptions.selectedState} -> $matches")
                }

                // Filter by publication date
                if (matches && currentFilterOptions.selectedDate != null) {
                    try {
                        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val selectedDate = inputFormat.parse(currentFilterOptions.selectedDate)
                        val selectedDateStr = selectedDate?.let { outputFormat.format(it) }

                        val issueDate = issue.publicationDate.split("T")[0] // Get just the date part
                        matches = issueDate == selectedDateStr
                        Log.d(TAG, "Date filter: $issueDate == $selectedDateStr -> $matches")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing date: ${e.message}")
                        matches = false
                    }
                }

                // Filter by equipment status
                if (matches && currentFilterOptions.equipmentStatus != null) {
                    matches = equipments.find { it.equipment_id == issue.id_equipment }?.active == currentFilterOptions.equipmentStatus
                    Log.d(TAG, "Equipment status filter: ${equipments.find { it.equipment_id == issue.id_equipment }?.active} == ${currentFilterOptions.equipmentStatus} -> $matches")
                }

                matches
            }

            val filteredMaintenance = maintenanceList.filter { maintenance ->
                var matches = true

                // Filter by task type
                if (!currentFilterOptions.showMaintenance) {
                    matches = false
                }

                // Filter by priority
                if (matches && currentFilterOptions.selectedPriority != null) {
                    matches = priorities.find { it.priority_id == maintenance.priority_id }?.priority == currentFilterOptions.selectedPriority
                    Log.d(TAG, "Priority filter: ${priorities.find { it.priority_id == maintenance.priority_id }?.priority} == ${currentFilterOptions.selectedPriority} -> $matches")
                }

                // Filter by state
                if (matches && currentFilterOptions.selectedState != null) {
                    matches = maintenanceStates.find { it.state_id == maintenance.state_id }?.state == currentFilterOptions.selectedState
                    Log.d(TAG, "State filter: ${maintenanceStates.find { it.state_id == maintenance.state_id }?.state} == ${currentFilterOptions.selectedState} -> $matches")
                }

                // Filter by publication date
                if (matches && currentFilterOptions.selectedDate != null) {
                    try {
                        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val selectedDate = inputFormat.parse(currentFilterOptions.selectedDate)
                        val selectedDateStr = selectedDate?.let { outputFormat.format(it) }

                        val maintenanceDate = maintenance.publicationDate.split("T")[0] // Get just the date part
                        matches = maintenanceDate == selectedDateStr
                        Log.d(TAG, "Date filter: $maintenanceDate == $selectedDateStr -> $matches")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing date: ${e.message}")
                        matches = false
                    }
                }

                // Filter by equipment status
                if (matches && currentFilterOptions.equipmentStatus != null) {
                    matches = equipments.find { it.equipment_id == maintenance.id_equipment }?.active == currentFilterOptions.equipmentStatus
                    Log.d(TAG, "Equipment status filter: ${equipments.find { it.equipment_id == maintenance.id_equipment }?.active} == ${currentFilterOptions.equipmentStatus} -> $matches")
                }

                matches
            }

            withContext(Dispatchers.Main) {
                filteredIssuesList.clear()
                filteredIssuesList.addAll(filteredIssues)
                filteredMaintenanceList.clear()
                filteredMaintenanceList.addAll(filteredMaintenance)
                tasksAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun searchTasks(query: String) {
        filteredIssuesList.clear()
        filteredMaintenanceList.clear()
        val searchQuery = query.lowercase()

        // First apply filters
        val filteredIssues = issuesList.filter { issue ->
            val matchesTaskType = currentFilterOptions.showIssues
            val matchesPriority = currentFilterOptions.selectedPriority?.let { priority ->
                priorities.find { it.priority_id == issue.priority_id }?.priority == priority
            } ?: true
            val matchesState = currentFilterOptions.selectedState?.let { state ->
                states.find { it.state_id == issue.state_id }?.state == state
            } ?: true
            val matchesDate = currentFilterOptions.selectedDate?.let { date ->
                try {
                    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = inputFormat.parse(date)
                    val selectedDateStr = selectedDate?.let { outputFormat.format(it) }

                    val issueDate = issue.publicationDate.split("T")[0] // Get just the date part
                    issueDate == selectedDateStr
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing date: ${e.message}")
                    false
                }
            } ?: true
            val matchesEquipmentStatus = currentFilterOptions.equipmentStatus?.let { status ->
                equipments.find { it.equipment_id == issue.id_equipment }?.active == status
            } ?: true

            matchesTaskType && matchesPriority && matchesState && matchesDate && matchesEquipmentStatus
        }

        val filteredMaintenance = maintenanceList.filter { maintenance ->
            val matchesTaskType = currentFilterOptions.showMaintenance
            val matchesPriority = currentFilterOptions.selectedPriority?.let { priority ->
                priorities.find { it.priority_id == maintenance.priority_id }?.priority == priority
            } ?: true
            val matchesState = currentFilterOptions.selectedState?.let { state ->
                maintenanceStates.find { it.state_id == maintenance.state_id }?.state == state
            } ?: true
            val matchesDate = currentFilterOptions.selectedDate?.let { date ->
                try {
                    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = inputFormat.parse(date)
                    val selectedDateStr = selectedDate?.let { outputFormat.format(it) }

                    val maintenanceDate = maintenance.publicationDate.split("T")[0] // Get just the date part
                    maintenanceDate == selectedDateStr
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing date: ${e.message}")
                    false
                }
            } ?: true
            val matchesEquipmentStatus = currentFilterOptions.equipmentStatus?.let { status ->
                equipments.find { it.equipment_id == maintenance.id_equipment }?.active == status
            } ?: true

            matchesTaskType && matchesPriority && matchesState && matchesDate && matchesEquipmentStatus
        }

        // Then apply search
        if (query.isEmpty()) {
            filteredIssuesList.addAll(filteredIssues)
            filteredMaintenanceList.addAll(filteredMaintenance)
        } else {
            filteredIssuesList.addAll(filteredIssues.filter { issue ->
                (issue.title?.lowercase()?.contains(searchQuery) == true) ||
                (issue.description?.lowercase()?.contains(searchQuery) == true) ||
                equipments.find { it.equipment_id == issue.id_equipment }?.name?.lowercase()?.contains(searchQuery) == true ||
                locations.find { it.location_id == issue.localization_id }?.name?.lowercase()?.contains(searchQuery) == true ||
                users.find { it.user_id == issue.id_user }?.let { user ->
                    if (user.lastname.isNullOrEmpty()) user.firstname.lowercase() else "${user.firstname} ${user.lastname}".lowercase()
                }?.contains(searchQuery) == true ||
                states.find { it.state_id == issue.state_id }?.state?.lowercase()?.contains(searchQuery) == true ||
                priorities.find { it.priority_id == issue.priority_id }?.priority?.lowercase()?.contains(searchQuery) == true
            })

            filteredMaintenanceList.addAll(filteredMaintenance.filter { maintenance ->
                maintenance.description?.lowercase()?.contains(searchQuery) == true ||
                equipments.find { it.equipment_id == maintenance.id_equipment }?.name?.lowercase()?.contains(searchQuery) == true ||
                locations.find { it.location_id == maintenance.localization_id }?.name?.lowercase()?.contains(searchQuery) == true ||
                users.find { it.user_id == maintenance.id_user }?.let { user ->
                    if (user.lastname.isNullOrEmpty()) user.firstname.lowercase() else "${user.firstname} ${user.lastname}".lowercase()
                }?.contains(searchQuery) == true ||
                maintenanceStates.find { it.state_id == maintenance.state_id }?.state?.lowercase()?.contains(searchQuery) == true ||
                priorities.find { it.priority_id == maintenance.priority_id }?.priority?.lowercase()?.contains(searchQuery) == true
            })
        }
        tasksAdapter.notifyDataSetChanged()
    }

    private fun showLoading(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        issuesContent.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun loadAuxiliaryDataAndTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prioritiesResult = priorityRepository.getPriorityList()
                val equipmentsResult = equipmentRepository.getEquipmentList()
                val locationsResult = locationRepository.getLocationList()
                val statesResult = stateIssueRepository.getIssueStates()
                val usersResult = userRepository.getAllUsers()
                val maintenanceStatesResult = com.example.fixlink.data.repository.StateMaintenanceRepository().getMaintenanceStates()

                if (prioritiesResult.isFailure || equipmentsResult.isFailure ||
                    locationsResult.isFailure || statesResult.isFailure || usersResult.isFailure || maintenanceStatesResult.isFailure) {
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
                maintenanceStates = maintenanceStatesResult.getOrNull() ?: emptyList()

                withContext(Dispatchers.Main) {
                    loadTasks()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: "+e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val technicianId = getLoggedInUserId()
                val issuesResult = issueRepository.getIssuesByTechnician(technicianId)
                val maintenanceResult = maintenanceRepository.getMaintenanceByTechnician(technicianId)
                withContext(Dispatchers.Main) {
                    issuesResult.onSuccess { issues ->
                        issuesList.clear()
                        issuesList.addAll(issues)
                        filteredIssuesList.clear()
                        filteredIssuesList.addAll(issues)
                    }
                    maintenanceResult.onSuccess { maintenances ->
                        maintenanceList.clear()
                        maintenanceList.addAll(maintenances)
                        filteredMaintenanceList.clear()
                        filteredMaintenanceList.addAll(maintenances)
                    }
                    tasksAdapter.setAuxiliaryData(priorities, equipments, locations, states, users, maintenanceStates)
                    tasksAdapter.notifyDataSetChanged()
                    showLoading(false)
                    if (issuesList.isEmpty() && maintenanceList.isEmpty()) {
                        Toast.makeText(requireContext(), "Não há tarefas atribuídas a você", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Erro: "+e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun getLoggedInUserId(): String {
        val result = userRepository.getCurrentUser()
        return result.getOrNull()?.user_id ?: throw Exception("User not found")
    }

    companion object {
        private const val TAG = "MyTasksFragment"
    }
} 