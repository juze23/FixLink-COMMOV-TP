package com.example.fixlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.fixlink.data.entities.User
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.data.repository.EquipmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewAllListFragment : Fragment() {
    companion object {
        private const val ARG_LIST_TYPE = "list_type"
        
        fun newInstance(listType: String) = ViewAllListFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_LIST_TYPE, listType)
            }
        }
    }

    private lateinit var listContainer: LinearLayout
    private lateinit var fragmentTitleTextView: TextView
    private val userRepository = UserRepository()
    private val equipmentRepository = EquipmentRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_all_list, container, false)

        listContainer = view.findViewById(R.id.listContainer)
        fragmentTitleTextView = view.findViewById(R.id.fragmentTitleTextView)

        val listType = arguments?.getString(ARG_LIST_TYPE)

        when (listType) {
            "technicians" -> {
                fragmentTitleTextView.text = "All Technicians"
                loadTechnicians()
            }
            "equipments" -> {
                fragmentTitleTextView.text = "All Equipments"
                loadEquipments()
            }
        }

        return view
    }

    private fun loadTechnicians() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val usersResult = userRepository.getAllUsers()
                if (usersResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error loading technicians", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val technicians = usersResult.getOrNull()?.filter { it.typeId == 2 } ?: emptyList()
                withContext(Dispatchers.Main) {
                    populateList(technicians.map { it.name })
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error loading technicians: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(requireContext(), "Error loading equipments", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val equipments = equipmentsResult.getOrNull() ?: emptyList()
                withContext(Dispatchers.Main) {
                    populateList(equipments.map { it.name })
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error loading equipments: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateList(items: List<String>) {
        val inflater = layoutInflater
        listContainer.removeAllViews()

        for (item in items) {
            val itemView = inflater.inflate(R.layout.list_item_admin, listContainer, false)
            val nameTextView = itemView.findViewById<TextView>(R.id.itemNameTextView)
            val editIcon = itemView.findViewById<ImageView>(R.id.editIcon)
            val deleteIcon = itemView.findViewById<ImageView>(R.id.deleteIcon)

            nameTextView.text = item

            // TODO: Implement edit and delete functionality
            editIcon.setOnClickListener {
                // Handle edit
            }

            deleteIcon.setOnClickListener {
                // Handle delete
            }

            listContainer.addView(itemView)
        }
    }
} 