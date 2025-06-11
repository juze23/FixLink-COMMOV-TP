package com.example.fixlink.ui.filters

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private lateinit var priorityChipGroup: ChipGroup
    private lateinit var stateChipGroup: ChipGroup
    private lateinit var equipmentStatusChipGroup: ChipGroup
    private lateinit var publicationDateEditText: EditText
    private lateinit var clearButton: Button
    private lateinit var applyButton: Button
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var currentPriority: String? = null
    private var currentState: String? = null
    private var currentDate: String? = null
    private var currentEquipmentStatus: Boolean? = null

    private var priorityCallback: ((String?) -> Unit)? = null
    private var stateCallback: ((String?) -> Unit)? = null
    private var dateCallback: ((String?) -> Unit)? = null
    private var equipmentStatusCallback: ((Boolean?) -> Unit)? = null
    private var clearCallback: (() -> Unit)? = null

    companion object {
        const val TAG = "MaintenanceFilterDialogFragment"
        private const val ARG_PRIORITY = "priority"
        private const val ARG_STATE = "state"
        private const val ARG_DATE = "date"
        private const val ARG_EQUIPMENT_STATUS = "equipment_status"

        fun newInstance(
            priority: String? = null,
            state: String? = null,
            date: String? = null,
            equipmentStatus: Boolean? = null
        ): MaintenanceFilterDialogFragment {
            return MaintenanceFilterDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PRIORITY, priority)
                    putString(ARG_STATE, state)
                    putString(ARG_DATE, date)
                    if (equipmentStatus != null) {
                        putBoolean(ARG_EQUIPMENT_STATUS, equipmentStatus)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            currentPriority = args.getString(ARG_PRIORITY)
            currentState = args.getString(ARG_STATE)
            currentDate = args.getString(ARG_DATE)
            currentEquipmentStatus = if (args.containsKey(ARG_EQUIPMENT_STATUS)) args.getBoolean(ARG_EQUIPMENT_STATUS) else null
        }
    }

    fun setPriorityCallback(callback: (String?) -> Unit) {
        priorityCallback = callback
    }

    fun setStateCallback(callback: (String?) -> Unit) {
        stateCallback = callback
    }

    fun setDateCallback(callback: (String?) -> Unit) {
        dateCallback = callback
    }

    fun setEquipmentStatusCallback(callback: (Boolean?) -> Unit) {
        equipmentStatusCallback = callback
    }

    fun setClearCallback(callback: () -> Unit) {
        clearCallback = callback
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maintenance_filter_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupListeners()
        setupInitialSelections()
    }

    private fun initializeViews(view: View) {
        priorityChipGroup = view.findViewById(R.id.priority_chip_group)
        stateChipGroup = view.findViewById(R.id.state_chip_group)
        equipmentStatusChipGroup = view.findViewById(R.id.equipment_status_chip_group)
        publicationDateEditText = view.findViewById(R.id.publication_date_edit_text)
        clearButton = view.findViewById(R.id.clear_button)
        applyButton = view.findViewById(R.id.apply_button)

        // Set chip texts using localized strings
        view.findViewById<Chip>(R.id.priority_low_chip)?.text = getString(R.string.text_priority_low)
        view.findViewById<Chip>(R.id.priority_medium_chip)?.text = getString(R.string.text_priority_medium)
        view.findViewById<Chip>(R.id.priority_high_chip)?.text = getString(R.string.text_priority_high)
        view.findViewById<Chip>(R.id.state_pending_chip)?.text = getString(R.string.text_state_pending)
        view.findViewById<Chip>(R.id.state_assigned_chip)?.text = getString(R.string.text_state_assigned)
        view.findViewById<Chip>(R.id.state_ongoing_chip)?.text = getString(R.string.text_state_ongoing)
        view.findViewById<Chip>(R.id.state_completed_chip)?.text = getString(R.string.text_state_completed)
        view.findViewById<Chip>(R.id.status_active_chip)?.text = getString(R.string.text_status_active)
        view.findViewById<Chip>(R.id.status_inactive_chip)?.text = getString(R.string.text_status_inactive)
    }

    private fun setupListeners() {
        priorityChipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentPriority = when (checkedId) {
                R.id.priority_low_chip -> FilterConstants.PRIORITY_LOW
                R.id.priority_medium_chip -> FilterConstants.PRIORITY_MEDIUM
                R.id.priority_high_chip -> FilterConstants.PRIORITY_HIGH
                else -> null
            }
        }

        stateChipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentState = when (checkedId) {
                R.id.state_pending_chip -> FilterConstants.STATE_PENDING
                R.id.state_assigned_chip -> FilterConstants.STATE_ASSIGNED
                R.id.state_ongoing_chip -> FilterConstants.STATE_ONGOING
                R.id.state_completed_chip -> FilterConstants.STATE_COMPLETED
                else -> null
            }
        }

        equipmentStatusChipGroup.setOnCheckedChangeListener { group, checkedId ->
            currentEquipmentStatus = when (checkedId) {
                R.id.status_active_chip -> true
                R.id.status_inactive_chip -> false
                else -> null
            }
        }

        publicationDateEditText.setOnClickListener {
            showDatePicker()
        }

        clearButton.setOnClickListener {
            clearFilters()
            clearCallback?.invoke()
        }

        applyButton.setOnClickListener {
            applyFilters()
        }
    }

    private fun setupInitialSelections() {
        // Priority
        when (currentPriority) {
            FilterConstants.PRIORITY_LOW -> priorityChipGroup.check(R.id.priority_low_chip)
            FilterConstants.PRIORITY_MEDIUM -> priorityChipGroup.check(R.id.priority_medium_chip)
            FilterConstants.PRIORITY_HIGH -> priorityChipGroup.check(R.id.priority_high_chip)
            else -> priorityChipGroup.clearCheck()
        }

        // State
        when (currentState) {
            FilterConstants.STATE_PENDING -> stateChipGroup.check(R.id.state_pending_chip)
            FilterConstants.STATE_ASSIGNED -> stateChipGroup.check(R.id.state_assigned_chip)
            FilterConstants.STATE_ONGOING -> stateChipGroup.check(R.id.state_ongoing_chip)
            FilterConstants.STATE_COMPLETED -> stateChipGroup.check(R.id.state_completed_chip)
            else -> stateChipGroup.clearCheck()
        }

        // Equipment status
        when (currentEquipmentStatus) {
            true -> equipmentStatusChipGroup.check(R.id.status_active_chip)
            false -> equipmentStatusChipGroup.check(R.id.status_inactive_chip)
            else -> equipmentStatusChipGroup.clearCheck()
        }

        // Date
        publicationDateEditText.setText(currentDate ?: "")
    }

    private fun clearFilters() {
        priorityChipGroup.clearCheck()
        stateChipGroup.clearCheck()
        equipmentStatusChipGroup.clearCheck()
        publicationDateEditText.text.clear()
        currentDate = null
        currentPriority = null
        currentState = null
        currentEquipmentStatus = null
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                currentDate = dateFormatter.format(calendar.time)
                publicationDateEditText.setText(currentDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        
        datePickerDialog.show()
    }

    private fun applyFilters() {
        priorityCallback?.invoke(currentPriority)
        stateCallback?.invoke(currentState)
        dateCallback?.invoke(currentDate)
        equipmentStatusCallback?.invoke(currentEquipmentStatus)
        dismiss()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
} 