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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_issues_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val filterIcon: ImageView = view.findViewById(R.id.filterIcon)
        val fabAddIssue: FloatingActionButton = view.findViewById(R.id.fab_add_issue)
        issuesRecyclerView = view.findViewById(R.id.issuesRecyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)

        filterIcon.setOnClickListener {
            IssuesFilterDialogFragment().show(childFragmentManager, IssuesFilterDialogFragment.TAG)
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

        // Setup RecyclerView
        issueAdapter = IssueAdapter(filteredIssuesList)
        issuesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        issuesRecyclerView.adapter = issueAdapter

        // Setup search functionality
        setupSearch()

        // Carregar dados auxiliares e issues
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
        filteredIssuesList.clear()
        if (query.isEmpty()) {
            filteredIssuesList.addAll(issuesList)
        } else {
            val searchQuery = query.lowercase()
            filteredIssuesList.addAll(issuesList.filter { issue ->
                // Search in description
                issue.description?.lowercase()?.contains(searchQuery) == true ||
                // Search in equipment name
                equipments.find { it.equipment_id == issue.id_equipment }?.name?.lowercase()?.contains(searchQuery) == true ||
                // Search in location name
                locations.find { it.location_id == issue.localization_id }?.name?.lowercase()?.contains(searchQuery) == true ||
                // Search in user name
                users.find { it.user_id == issue.id_user }?.name?.lowercase()?.contains(searchQuery) == true ||
                // Search in state
                states.find { it.state_id == issue.state_id }?.state?.lowercase()?.contains(searchQuery) == true ||
                // Search in priority
                priorities.find { it.priority_id == issue.priority_id }?.priority?.lowercase()?.contains(searchQuery) == true
            })
        }
        issueAdapter.notifyDataSetChanged()
    }

    private fun loadAuxiliaryDataAndIssues() {
        CoroutineScope(Dispatchers.IO).launch {
            val prioritiesResult = priorityRepository.getPriorityList()
            val equipmentsResult = equipmentRepository.getEquipmentList()
            val locationsResult = locationRepository.getLocationList()
            val statesResult = stateIssueRepository.getIssueStates()
            val usersResult = userRepository.getAllUsers()

            priorities = prioritiesResult.getOrNull() ?: emptyList()
            equipments = equipmentsResult.getOrNull() ?: emptyList()
            locations = locationsResult.getOrNull() ?: emptyList()
            states = statesResult.getOrNull() ?: emptyList()
            users = usersResult.getOrNull() ?: emptyList()

            withContext(Dispatchers.Main) {
                loadIssues()
            }
        }
    }

    private fun loadIssues() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = issueRepository.getAllIssues()
            withContext(Dispatchers.Main) {
                result.onSuccess { issues ->
                    issuesList.clear()
                    issuesList.addAll(issues)
                    filteredIssuesList.clear()
                    filteredIssuesList.addAll(issues)
                    issueAdapter.setAuxiliaryData(priorities, equipments, locations, states, users)
                    issueAdapter.notifyDataSetChanged()
                }.onFailure {
                    Toast.makeText(requireContext(), "Erro ao carregar issues", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 