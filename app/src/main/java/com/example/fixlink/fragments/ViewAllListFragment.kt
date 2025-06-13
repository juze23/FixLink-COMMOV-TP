package com.example.fixlink.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.adapters.*
import com.example.fixlink.data.repository.*
import com.example.fixlink.activities.AdminActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewAllListFragment : Fragment() {
    companion object {
        private const val ARG_LIST_TYPE = "list_type"
        private const val ARG_TITLE = "title"

        fun newInstance(listType: String, title: String): ViewAllListFragment {
            return ViewAllListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LIST_TYPE, listType)
                    putString(ARG_TITLE, title)
                }
            }
        }
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var maintenanceTypeRepository: MaintenanceTypeRepository
    private lateinit var issueTypeRepository: IssueTypeRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var equipmentRepository: EquipmentRepository
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        maintenanceTypeRepository = MaintenanceTypeRepository()
        issueTypeRepository = IssueTypeRepository()
        locationRepository = LocationRepository()
        equipmentRepository = EquipmentRepository()
        userRepository = UserRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_list, container, false)
        
        // Configure title
        val titleTextView = view.findViewById<TextView>(R.id.viewAllTitle)
        arguments?.getString(ARG_TITLE)?.let { title ->
            titleTextView.text = title
        }

        // Configure ver menos button
        view.findViewById<MaterialButton>(R.id.verMenosButton).setOnClickListener {
            (activity as? AdminActivity)?.hideViewAllListFragment()
        }

        // Configure RecyclerView
        recyclerView = view.findViewById(R.id.viewAllRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Load data based on list type
        arguments?.getString(ARG_LIST_TYPE)?.let { listType ->
            when (listType) {
                "technicians" -> loadTechnicians()
                "equipments" -> loadEquipments()
                "maintenance_types" -> loadMaintenanceTypes()
                "issue_types" -> loadIssueTypes()
                "locations" -> loadLocations()
                else -> {
                    // If unsupported type, go back to previous screen
                    parentFragmentManager.popBackStack()
                }
            }
        }

        return view
    }

    private fun loadTechnicians() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.getAllUsers()
                if (result.isSuccess) {
                    val technicians = result.getOrNull()?.filter { it.typeId == 2 } ?: emptyList()
                    withContext(Dispatchers.Main) {
                        recyclerView.adapter = TechnicianAdapter { technician ->
                            // Handle technician click if needed
                        }.apply {
                            submitList(technicians)
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error loading technicians", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadEquipments() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = equipmentRepository.getEquipmentList()
                if (result.isSuccess) {
                    val equipments = result.getOrNull() ?: emptyList()
                    withContext(Dispatchers.Main) {
                        recyclerView.adapter = EquipmentAdapter(equipments)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error loading equipments", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        recyclerView.adapter = MaintenanceTypeAdapter(types)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error loading maintenance types", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        recyclerView.adapter = IssueTypeAdapter(types)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error loading issue types", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        recyclerView.adapter = LocationAdapter(locations)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error loading locations", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 