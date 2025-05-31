package com.example.fixlink.ui.filters

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.fixlink.R
import android.widget.Button
import android.widget.EditText
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MaintenanceFilterDialogFragment : DialogFragment() {

    private val TAG = "MaintenanceFilterDialogFragment"
    private lateinit var ownershipChipGroup: ChipGroup
    private lateinit var priorityChipGroup: ChipGroup
    private lateinit var stateChipGroup: ChipGroup
    private lateinit var publicationDateEditText: EditText
    private lateinit var clearButton: Button
    private lateinit var applyButton: Button
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maintenance_filter_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        ownershipChipGroup = view.findViewById(R.id.ownership_chip_group)
        priorityChipGroup = view.findViewById(R.id.priority_chip_group)
        stateChipGroup = view.findViewById(R.id.state_chip_group)
        publicationDateEditText = view.findViewById(R.id.publication_date_edit_text)
        clearButton = view.findViewById(R.id.clear_button)
        applyButton = view.findViewById(R.id.apply_button)

        // Setup date picker
        setupDatePicker()

        // Setup button click listeners
        clearButton.setOnClickListener {
            clearFilters()
        }

        applyButton.setOnClickListener {
            applyFilters()
        }
    }

    private fun setupDatePicker() {
        publicationDateEditText.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    publicationDateEditText.setText(dateFormatter.format(calendar.time))
                },
                year,
                month,
                day
            )

            // Set max date to today
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            
            datePickerDialog.show()
        }
    }

    private fun clearFilters() {
        // Clear ownership selection
        ownershipChipGroup.clearCheck()

        // Clear priority selection
        priorityChipGroup.clearCheck()

        // Clear state selections
        for (i in 0 until stateChipGroup.childCount) {
            val chip = stateChipGroup.getChildAt(i) as? Chip
            chip?.isChecked = false
        }

        // Clear date
        publicationDateEditText.text.clear()

        // Dismiss dialog
        dismiss()
    }

    private fun applyFilters() {
        // Get selected ownership
        val selectedOwnershipChip = ownershipChipGroup.findViewById<Chip>(ownershipChipGroup.checkedChipId)
        val ownership = selectedOwnershipChip?.text?.toString()

        // Get selected priority
        val selectedPriorityChip = priorityChipGroup.findViewById<Chip>(priorityChipGroup.checkedChipId)
        val priority = selectedPriorityChip?.text?.toString()

        // Get selected states
        val selectedStates = mutableListOf<String>()
        for (i in 0 until stateChipGroup.childCount) {
            val chip = stateChipGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                selectedStates.add(chip.text.toString())
            }
        }

        // Get selected date
        val date = publicationDateEditText.text.toString()

        // TODO: Apply filters to the list
        Log.d(TAG, "Applying filters: Ownership=$ownership, Priority=$priority, States=$selectedStates, Date=$date")

        // Dismiss dialog
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    companion object {
        const val TAG = "MaintenanceFilterDialogFragment"
    }
} 