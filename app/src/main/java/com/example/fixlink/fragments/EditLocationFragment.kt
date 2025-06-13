package com.example.fixlink.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fixlink.R
import com.example.fixlink.activities.AdminActivity
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.repository.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditLocationFragment : Fragment() {
    private lateinit var locationNameTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    private lateinit var locationRepository: LocationRepository
    private var currentLocation: Location? = null

    companion object {
        private const val ARG_LOCATION_ID = "location_id"
        private const val ARG_LOCATION_NAME = "location_name"

        fun newInstance(locationId: Int, locationName: String) = EditLocationFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_LOCATION_ID, locationId)
                putString(ARG_LOCATION_NAME, locationName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val locationId = it.getInt(ARG_LOCATION_ID)
            val locationName = it.getString(ARG_LOCATION_NAME) ?: ""
            currentLocation = Location(location_id = locationId, name = locationName)
        }
        locationRepository = LocationRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationNameTextView = view.findViewById(R.id.locationNameTextView)
        nameEditText = view.findViewById(R.id.nameEditText)
        cancelButton = view.findViewById(R.id.cancelButton)
        saveButton = view.findViewById(R.id.saveButton)

        // Configure input handling
        nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                nameEditText.clearFocus()
            }
        }

        // Set initial values
        currentLocation?.let { location ->
            locationNameTextView.text = location.name
            nameEditText.setText(location.name)
        }

        // Set click listeners
        cancelButton.setOnClickListener { 
            try {
                nameEditText.clearFocus()
                (activity as? AdminActivity)?.hideEditFragment()
            } catch (e: Exception) {
                Log.e("EditLocationFragment", "Error dismissing fragment", e)
            }
        }
        saveButton.setOnClickListener { 
            try {
                nameEditText.clearFocus()
                saveChanges() 
            } catch (e: Exception) {
                Log.e("EditLocationFragment", "Error saving changes", e)
                Toast.makeText(context, getString(R.string.error_updating_location), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        try {
            nameEditText.setOnFocusChangeListener(null)
            super.onDestroyView()
        } catch (e: Exception) {
            Log.e("EditLocationFragment", "Error in onDestroyView", e)
        }
    }

    private fun saveChanges() {
        val newName = nameEditText.text.toString().trim()
        if (newName.isEmpty()) {
            nameEditText.error = getString(R.string.error_name_required)
            return
        }

        val currentLocationId = currentLocation?.location_id
        if (currentLocationId == null) {
            Toast.makeText(context, getString(R.string.error_location_not_found), Toast.LENGTH_SHORT).show()
            return
        }

        if (newName == currentLocation?.name) {
            Toast.makeText(context, getString(R.string.text_no_changes), Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = locationRepository.updateLocation(currentLocationId, newName)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(context, getString(R.string.text_location_updated), Toast.LENGTH_SHORT).show()
                        (activity as? AdminActivity)?.hideEditFragment()
                    } else {
                        Toast.makeText(context, getString(R.string.error_updating_location), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, getString(R.string.error_updating_location, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 