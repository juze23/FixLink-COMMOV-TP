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
import com.example.fixlink.ui.filters.IssuesFilterDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.entities.Issue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.fixlink.data.repository.PriorityRepository
import com.example.fixlink.data.repository.EquipmentRepository
import com.example.fixlink.data.repository.LocationRepository
import com.example.fixlink.data.repository.StateIssueRepository
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.Issue_state
import com.example.fixlink.data.entities.User
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import java.text.SimpleDateFormat
import java.util.*

class IssuesContentFragment : Fragment() {
    companion object {
        private const val TAG = "IssuesContentFragment"
    }

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

    private var priorities: List<Priority> = emptyList()
    private var equipments: List<Equipment> = emptyList()
    private var locations: List<Location> = emptyList()
    private var states: List<Issue_state> = emptyList()
    private var users: List<User> = emptyList()

    private lateinit var loadingProgressBar: View
    private lateinit var issuesContent: View

    //stores current filter state
    private var currentOwnership: String? = null
    private var currentPriority: String? = null
    private var currentState: String? = null
    private var currentDate: String? = null
    private var currentEquipmentStatus: Boolean? = null

    //add debounce mechanism | limits how often a function can be called
    private var filterJob: kotlinx.coroutines.Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_issues_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val filterIcon: ImageView = view.findViewById(R.id.filterIcon)
        val fabAddIssue: FloatingActionButton = view.findViewById(R.id.fab_add_issue)
        issuesRecyclerView = view.findViewById(R.id.issuesRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        issuesContent = view.findViewById(R.id.issuesContent)

        filterIcon.setOnClickListener {
            showFilterDialog()
        }

        fabAddIssue.setOnClickListener {
            Log.d(TAG, "FAB clicked, attempting to launch RegisterIssueActivity")
            try {
                val intent = Intent(requireContext(), RegisterIssueActivity::class.java)
                startActivity(intent)
                Log.d(TAG, "Successfully launched RegisterIssueActivity")
            } catch (e: Exception) {
                Log.e(TAG, "Error launching RegisterIssueActivity", e)
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        issueAdapter = IssueAdapter(filteredIssuesList)
        issuesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        issuesRecyclerView.adapter = issueAdapter

        setupSearch()

        showLoading(true)

        loadAuxiliaryDataAndIssues()
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchIssues(s.toString())
            }
        })
    }

    private fun searchIssues(query: String) {
        applyFilters()
    }

    private fun showLoading(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        issuesContent.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun loadAuxiliaryDataAndIssues() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prioritiesResult = priorityRepository.getPriorityList()
                val equipmentsResult = equipmentRepository.getEquipmentList()
                val locationsResult = locationRepository.getLocationList()
                val statesResult = stateIssueRepository.getIssueStates()
                val usersResult = userRepository.getAllUsers()

                if (prioritiesResult.isFailure || equipmentsResult.isFailure || 
                    locationsResult.isFailure || statesResult.isFailure || usersResult.isFailure) {
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

                withContext(Dispatchers.Main) {
                    loadIssues()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadIssues() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = issueRepository.getAllIssues()
                withContext(Dispatchers.Main) {
                    result.onSuccess { issues ->
                        //clear both lists first
                        issuesList.clear()
                        filteredIssuesList.clear()
                        
                        //add to issuesList
                        issuesList.addAll(issues)
                        
                        //sets up the adapter with auxiliary data
                        issueAdapter.setAuxiliaryData(priorities, equipments, locations, states, users)
                        
                        //update the filtered list directly instead of calling applyFilters
                        filteredIssuesList.addAll(issues)
                        issueAdapter.notifyDataSetChanged()
                        
                        showLoading(false)
                    }.onFailure {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Erro ao carregar issues", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyFilters() {

        filterJob?.cancel()
        
        filterJob = CoroutineScope(Dispatchers.Main).launch {
            //add a small delay to debounce rapid filter changes
            kotlinx.coroutines.delay(100)
            
            filteredIssuesList.clear()
            
            var filteredIssues = issuesList.toMutableList()

            //ownership filter
            if (currentOwnership == "My Issues") {
                val currentUserId = withContext(Dispatchers.IO) {
                    userRepository.getCurrentUserId()
                }
                if (currentUserId != null) {
                    Log.d(TAG, "Filtering for user ID: $currentUserId")
                    filteredIssues = filteredIssues.filter { issue ->
                        Log.d(TAG, "Comparing issue user ID: ${issue.id_user} with current user ID: $currentUserId")
                        issue.id_user == currentUserId
                    }.toMutableList()
                    Log.d(TAG, "Filtered issues count: ${filteredIssues.size}")
                } else {
                    Log.e(TAG, "Current user ID is null")
                    filteredIssues.clear()
                }
            }

            //priority filter
            if (currentPriority != null) {
                val priorityId = priorities.find { it.priority == currentPriority }?.priority_id
                filteredIssues = filteredIssues.filter { it.priority_id == priorityId }.toMutableList()
            }

            //date filter
            if (currentDate != null) {
                try {
                    val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = inputFormat.parse(currentDate)
                    val selectedDateStr = selectedDate?.let { outputFormat.format(it) }

                    filteredIssues = filteredIssues.filter { issue ->
                        val issueDate = issue.publicationDate.split("T")[0] // Get just the date part
                        issueDate == selectedDateStr
                    }.toMutableList()
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing date: ${e.message}")
                }
            }

            //state filter
            if (currentState != null) {
                val stateId = states.find { it.state == currentState }?.state_id
                filteredIssues = filteredIssues.filter { it.state_id == stateId }.toMutableList()
            }

            //equipment status filter
            if (currentEquipmentStatus != null) {
                filteredIssues = filteredIssues.filter { issue ->
                    val equipment = equipments.find { it.equipment_id == issue.id_equipment }
                    equipment?.active == currentEquipmentStatus
                }.toMutableList()
            }

            //search filter
            val searchQuery = searchEditText.text.toString()
            if (searchQuery.isNotEmpty()) {
                filteredIssues = filteredIssues.filter { issue ->
                    issue.description?.contains(searchQuery, ignoreCase = true) == true
                }.toMutableList()
            }

            //update the filtered list
            filteredIssuesList.addAll(filteredIssues)
            issueAdapter.notifyDataSetChanged()
        }
    }

    private fun showFilterDialog() {
        val filterDialog = IssuesFilterDialogFragment.newInstance(
            ownership = currentOwnership,
            priority = currentPriority,
            state = currentState,
            date = currentDate,
            equipmentStatus = currentEquipmentStatus
        ).apply {
            setOwnershipCallback { ownership ->
                currentOwnership = ownership
                applyFilters()
            }
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

        filterDialog.show(childFragmentManager, IssuesFilterDialogFragment.TAG)
    }

    private fun clearFilters() {
        //clears stored filters
        currentOwnership = null
        currentPriority = null
        currentState = null
        currentDate = null
        currentEquipmentStatus = null

        //resets the list to show all items
        filteredIssuesList.clear()
        filteredIssuesList.addAll(issuesList)
        issueAdapter.notifyDataSetChanged()
    }
} 