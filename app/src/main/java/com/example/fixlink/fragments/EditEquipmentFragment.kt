package com.example.fixlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.repository.EquipmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditEquipmentFragment : Fragment() {

    private lateinit var equipmentNameTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var statusSpinner: Spinner
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button
    private lateinit var equipmentRepository: EquipmentRepository
    private var currentEquipment: Equipment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_equipment, container, false)

        equipmentNameTextView = view.findViewById(R.id.equipmentNameTextView)
        nameEditText = view.findViewById(R.id.nameEditText)
        descriptionEditText = view.findViewById(R.id.descriptionEditText)
        statusSpinner = view.findViewById(R.id.statusSpinner)
        cancelButton = view.findViewById(R.id.cancelButton)
        saveButton = view.findViewById(R.id.saveButton)
        equipmentRepository = EquipmentRepository()

        // Set click listeners for buttons
        cancelButton.setOnClickListener { dismissFragment() }
        saveButton.setOnClickListener { saveChanges() }

        // Load equipment data if provided
        arguments?.getString("equipmentName")?.let { equipmentName ->
            loadEquipmentData(equipmentName)
        }

        return view
    }

    private fun loadEquipmentData(equipmentName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val equipment = equipmentRepository.getEquipmentByName(equipmentName)
                withContext(Dispatchers.Main) {
                    equipment?.let {
                        currentEquipment = it
                        equipmentNameTextView.text = it.name
                        nameEditText.setText(it.name)
                        descriptionEditText.setText(it.description)
                        statusSpinner.setSelection(if (it.active) 0 else 1) // 0 for Active, 1 for Inactive
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading equipment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun dismissFragment() {
        (activity as? AdminActivity)?.hideEditFragment()
    }

    private fun saveChanges() {
        val name = nameEditText.text.toString().trim()
        val description = descriptionEditText.text.toString().trim()
        val isActive = statusSpinner.selectedItemPosition == 0 // 0 for Active, 1 for Inactive

        if (name.isEmpty()) {
            nameEditText.error = "Name is required"
            return
        }

        currentEquipment?.let { equipment ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val updatedEquipment = equipment.copy(
                        name = name,
                        description = description,
                        active = isActive
                    )
                    val result = equipmentRepository.updateEquipment(updatedEquipment)
                    withContext(Dispatchers.Main) {
                        result.fold(
                            onSuccess = {
                                Toast.makeText(context, "Equipment updated successfully!", Toast.LENGTH_SHORT).show()
                                dismissFragment()
                            },
                            onFailure = { error ->
                                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(equipmentName: String? = null): EditEquipmentFragment {
            return EditEquipmentFragment().apply {
                arguments = Bundle().apply {
                    equipmentName?.let { putString("equipmentName", it) }
                }
            }
        }
    }
} 