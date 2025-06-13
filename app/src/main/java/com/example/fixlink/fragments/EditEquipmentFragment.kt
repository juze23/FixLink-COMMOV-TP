package com.example.fixlink.fragments

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
import com.example.fixlink.R
import com.example.fixlink.activities.AdminActivity
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

        // Configure input handling
        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                nameEditText.clearFocus()
            }
        }
        descriptionEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                descriptionEditText.clearFocus()
            }
        }

        // Set click listeners for buttons
        cancelButton.setOnClickListener { 
            nameEditText.clearFocus()
            descriptionEditText.clearFocus()
            dismissFragment() 
        }
        saveButton.setOnClickListener { 
            nameEditText.clearFocus()
            descriptionEditText.clearFocus()
            saveChanges() 
        }

        // Load equipment data if provided
        arguments?.getString("equipmentName")?.let { equipmentName ->
            loadEquipmentData(equipmentName)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        nameEditText.setOnFocusChangeListener(null)
        descriptionEditText.setOnFocusChangeListener(null)
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
        val newName = nameEditText.text.toString().trim()
        val newDescription = descriptionEditText.text.toString().trim()
        val newStatus = statusSpinner.selectedItem.toString() == getString(R.string.text_status_active)

        if (newName.isEmpty()) {
            nameEditText.error = getString(R.string.error_name_required)
            return
        }

        val currentEquipmentId = currentEquipment?.equipment_id ?: run {
            Toast.makeText(context, getString(R.string.error_equipment_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        val updatedEquipment = Equipment(
            equipment_id = currentEquipmentId,
            name = newName,
            description = newDescription,
            active = newStatus
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = equipmentRepository.updateEquipment(updatedEquipment)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(context, getString(R.string.text_equipment_updated), Toast.LENGTH_SHORT).show()
                        (activity as? AdminActivity)?.hideEditFragment()
                    } else {
                        Toast.makeText(context, getString(R.string.error_updating_equipment), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, getString(R.string.error_updating_equipment, e.message), Toast.LENGTH_SHORT).show()
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