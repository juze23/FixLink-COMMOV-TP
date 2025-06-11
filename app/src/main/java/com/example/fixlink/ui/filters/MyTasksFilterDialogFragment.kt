package com.example.fixlink.ui.filters

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.fixlink.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class MyTasksFilterDialogFragment : DialogFragment() {
    private var onFilterAppliedListener: ((FilterOptions) -> Unit)? = null
    private var currentFilterOptions: FilterOptions = FilterOptions()
    private lateinit var taskTypeChipGroup: ChipGroup
    private lateinit var priorityChipGroup: ChipGroup
    private lateinit var stateChipGroup: ChipGroup
    private lateinit var equipmentStatusChipGroup: ChipGroup
    private lateinit var publicationDateEditText: EditText
    private lateinit var clearButton: Button
    private lateinit var applyButton: Button
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    companion object {
        const val TAG = "MyTasksFilterDialogFragment"
    }

    data class FilterOptions(
        var showIssues: Boolean = true,
        var showMaintenance: Boolean = true,
        var selectedPriority: String? = null,
        var selectedState: String? = null,
        var selectedDate: String? = null,
        var equipmentStatus: Boolean? = null
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.DialogTheme).apply {
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_filter_my_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupListeners()
        setupInitialSelections()
    }

    private fun initializeViews(view: View) {
        taskTypeChipGroup = view.findViewById(R.id.taskTypeChipGroup)
        priorityChipGroup = view.findViewById(R.id.priorityChipGroup)
        stateChipGroup = view.findViewById(R.id.stateChipGroup)
        equipmentStatusChipGroup = view.findViewById(R.id.equipmentStatusChipGroup)
        publicationDateEditText = view.findViewById(R.id.publicationDateEditText)
        clearButton = view.findViewById(R.id.clearButton)
        applyButton = view.findViewById(R.id.applyButton)

        // Set chip texts using localized strings
        view.findViewById<Chip>(R.id.priorityLowChip)?.text = getString(R.string.text_priority_low)
        view.findViewById<Chip>(R.id.priorityMediumChip)?.text = getString(R.string.text_priority_medium)
        view.findViewById<Chip>(R.id.priorityHighChip)?.text = getString(R.string.text_priority_high)
        view.findViewById<Chip>(R.id.statePendingChip)?.text = getString(R.string.text_state_pending)
        view.findViewById<Chip>(R.id.stateAssignedChip)?.text = getString(R.string.text_state_assigned)
        view.findViewById<Chip>(R.id.stateOngoingChip)?.text = getString(R.string.text_state_ongoing)
        view.findViewById<Chip>(R.id.stateCompletedChip)?.text = getString(R.string.text_state_completed)
        view.findViewById<Chip>(R.id.statusActiveChip)?.text = getString(R.string.text_status_active)
        view.findViewById<Chip>(R.id.statusInactiveChip)?.text = getString(R.string.text_status_inactive)
    }

    private fun setupListeners() {
        // Task Type
        taskTypeChipGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipAllTasks -> {
                    currentFilterOptions.showIssues = true
                    currentFilterOptions.showMaintenance = true
                }
                R.id.chipIssues -> {
                    currentFilterOptions.showIssues = true
                    currentFilterOptions.showMaintenance = false
                }
                R.id.chipMaintenance -> {
                    currentFilterOptions.showIssues = false
                    currentFilterOptions.showMaintenance = true
                }
            }
        }

        // Priority
        priorityChipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentFilterOptions.selectedPriority = when (checkedId) {
                R.id.priorityLowChip -> FilterConstants.PRIORITY_LOW
                R.id.priorityMediumChip -> FilterConstants.PRIORITY_MEDIUM
                R.id.priorityHighChip -> FilterConstants.PRIORITY_HIGH
                else -> null
            }
        }

        // State
        stateChipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentFilterOptions.selectedState = when (checkedId) {
                R.id.statePendingChip -> FilterConstants.STATE_PENDING
                R.id.stateAssignedChip -> FilterConstants.STATE_ASSIGNED
                R.id.stateOngoingChip -> FilterConstants.STATE_ONGOING
                R.id.stateCompletedChip -> FilterConstants.STATE_COMPLETED
                else -> null
            }
        }

        // Equipment Status
        equipmentStatusChipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentFilterOptions.equipmentStatus = when (checkedId) {
                R.id.statusActiveChip -> true
                R.id.statusInactiveChip -> false
                else -> null
            }
        }

        // Publication Date
        publicationDateEditText.setOnClickListener {
            showDatePicker()
        }

        // Buttons
        clearButton.setOnClickListener {
            clearFilters()
        }

        applyButton.setOnClickListener {
            onFilterAppliedListener?.invoke(currentFilterOptions)
            dismiss()
        }
    }

    private fun setupInitialSelections() {
        // Task Type
        when {
            currentFilterOptions.showIssues && currentFilterOptions.showMaintenance -> taskTypeChipGroup.check(R.id.chipAllTasks)
            currentFilterOptions.showIssues -> taskTypeChipGroup.check(R.id.chipIssues)
            currentFilterOptions.showMaintenance -> taskTypeChipGroup.check(R.id.chipMaintenance)
        }

        // Priority
        when (currentFilterOptions.selectedPriority) {
            FilterConstants.PRIORITY_LOW -> priorityChipGroup.check(R.id.priorityLowChip)
            FilterConstants.PRIORITY_MEDIUM -> priorityChipGroup.check(R.id.priorityMediumChip)
            FilterConstants.PRIORITY_HIGH -> priorityChipGroup.check(R.id.priorityHighChip)
            else -> priorityChipGroup.clearCheck()
        }

        // State
        when (currentFilterOptions.selectedState) {
            FilterConstants.STATE_PENDING -> stateChipGroup.check(R.id.statePendingChip)
            FilterConstants.STATE_ASSIGNED -> stateChipGroup.check(R.id.stateAssignedChip)
            FilterConstants.STATE_ONGOING -> stateChipGroup.check(R.id.stateOngoingChip)
            FilterConstants.STATE_COMPLETED -> stateChipGroup.check(R.id.stateCompletedChip)
            else -> stateChipGroup.clearCheck()
        }

        // Equipment Status
        when (currentFilterOptions.equipmentStatus) {
            true -> equipmentStatusChipGroup.check(R.id.statusActiveChip)
            false -> equipmentStatusChipGroup.check(R.id.statusInactiveChip)
            else -> equipmentStatusChipGroup.clearCheck()
        }

        // Date
        currentFilterOptions.selectedDate?.let { date ->
            publicationDateEditText.setText(date)
        }
    }

    private fun clearFilters() {
        taskTypeChipGroup.check(R.id.chipAllTasks)
        priorityChipGroup.clearCheck()
        stateChipGroup.clearCheck()
        equipmentStatusChipGroup.clearCheck()
        publicationDateEditText.text.clear()
        
        currentFilterOptions = FilterOptions()
        onFilterAppliedListener?.invoke(currentFilterOptions)
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                currentFilterOptions.selectedDate = dateFormatter.format(calendar.time)
                publicationDateEditText.setText(currentFilterOptions.selectedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    fun setOnFilterAppliedListener(listener: (FilterOptions) -> Unit) {
        onFilterAppliedListener = listener
    }

    fun setCurrentFilterOptions(options: FilterOptions) {
        currentFilterOptions = options
    }
} 