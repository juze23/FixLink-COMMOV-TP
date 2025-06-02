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

class EditEquipmentFragment : Fragment() {

    private lateinit var equipmentNameTextView: TextView
    private lateinit var locationEditText: EditText
    private lateinit var statusSpinner: Spinner
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edit_equipment, container, false)

        equipmentNameTextView = view.findViewById(R.id.equipmentNameTextView)
        locationEditText = view.findViewById(R.id.locationEditText)
        statusSpinner = view.findViewById(R.id.statusSpinner)
        cancelButton = view.findViewById(R.id.cancelButton)
        saveButton = view.findViewById(R.id.saveButton)

        // Set click listeners for buttons
        cancelButton.setOnClickListener { dismissFragment() }
        saveButton.setOnClickListener { saveChanges() }

        // TODO: Load equipment data here

        return view
    }

    private fun dismissFragment() {
        // Hide the fragment and its container
        (activity as? AdminActivity)?.hideEditFragment()
    }

    private fun saveChanges() {
        // TODO: Implement save logic
        // Get data from EditText and Spinner
        val location = locationEditText.text.toString()
        val status = statusSpinner.selectedItem.toString()

        // TODO: Save data (e.g., update database, send to API)

        // Dismiss the fragment after saving
        dismissFragment()
    }

    // TODO: Add a method to receive equipment data when showing the fragment

    companion object {
        fun newInstance(): EditEquipmentFragment {
            return EditEquipmentFragment()
        }
    }
} 