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

class IssuesFilterDialogFragment : DialogFragment() {

    private val TAG = "IssuesFilterDialogFragment"
    private lateinit var ownershipChipGroup: ChipGroup
    private lateinit var priorityChipGroup: ChipGroup
    private lateinit var stateChipGroup: ChipGroup
    private lateinit var equipmentStatusChipGroup: ChipGroup
    private lateinit var publicationDateEditText: EditText
    private lateinit var clearButton: Button
    private lateinit var applyButton: Button
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var priorityCallback: ((String?) -> Unit)? = null
    private var ownershipCallback: ((String?) -> Unit)? = null
    private var stateCallback: ((String?) -> Unit)? = null
    private var dateCallback: ((String?) -> Unit)? = null
    private var equipmentStatusCallback: ((Boolean?) -> Unit)? = null
    private var clearCallback: (() -> Unit)? = null

    //current filter state
    private var currentOwnership: String? = null
    private var currentPriority: String? = null
    private var currentState: String? = null
    private var currentDate: String? = null
    private var currentEquipmentStatus: Boolean? = null

    companion object {
        const val TAG = "IssuesFilterDialogFragment"
        private const val ARG_OWNERSHIP = "ownership"
        private const val ARG_PRIORITY = "priority"
        private const val ARG_STATE = "state"
        private const val ARG_DATE = "date"
        private const val ARG_EQUIPMENT_STATUS = "equipment_status"

        fun newInstance(
            ownership: String?,
            priority: String?,
            state: String?,
            date: String?,
            equipmentStatus: Boolean?
        ): IssuesFilterDialogFragment {
            return IssuesFilterDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_OWNERSHIP, ownership)
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
            currentOwnership = args.getString(ARG_OWNERSHIP)
            currentPriority = args.getString(ARG_PRIORITY)
            currentState = args.getString(ARG_STATE)
            currentDate = args.getString(ARG_DATE)
            currentEquipmentStatus = if (args.containsKey(ARG_EQUIPMENT_STATUS)) args.getBoolean(ARG_EQUIPMENT_STATUS) else null
        }
    }

    fun setPriorityCallback(callback: (String?) -> Unit) {
        priorityCallback = callback
    }

    fun setOwnershipCallback(callback: (String?) -> Unit) {
        ownershipCallback = callback
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
        return inflater.inflate(R.layout.fragment_issues_filter_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        ownershipChipGroup = view.findViewById(R.id.ownership_chip_group)
        priorityChipGroup = view.findViewById(R.id.priority_chip_group)
        stateChipGroup = view.findViewById(R.id.stateChipGroup)
        equipmentStatusChipGroup = view.findViewById(R.id.equipment_status_chip_group)
        publicationDateEditText = view.findViewById(R.id.publication_date_edit_text)
        clearButton = view.findViewById(R.id.clear_button)
        applyButton = view.findViewById(R.id.apply_button)

        setupDatePicker()

        setupInitialSelections()

        clearButton.setOnClickListener {
            clearFilters()
        }

        applyButton.setOnClickListener {
            applyFilters()
        }

        setupChipGroupListeners()
    }

    private fun setupChipGroupListeners() {
        ownershipChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            currentOwnership = chip?.text?.toString()
        }

        priorityChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            currentPriority = chip?.text?.toString()
        }

        stateChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            currentState = chip?.text?.toString()
        }

        equipmentStatusChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            currentEquipmentStatus = when (chip?.text?.toString()) {
                "Active" -> true
                "Inactive" -> false
                else -> null
            }
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
    }

    private fun setupInitialSelections() {
        //ownership
        when (currentOwnership) {
            "All Issues" -> ownershipChipGroup.check(R.id.ownership_all_chip)
            "My Issues" -> ownershipChipGroup.check(R.id.ownership_my_chip)
            else -> ownershipChipGroup.clearCheck()
        }

        //priority
        when (currentPriority) {
            requireContext().getString(R.string.text_priority_low) -> priorityChipGroup.check(R.id.priority_low_chip)
            requireContext().getString(R.string.text_priority_medium) -> priorityChipGroup.check(R.id.priority_medium_chip)
            requireContext().getString(R.string.text_priority_high) -> priorityChipGroup.check(R.id.priority_high_chip)
            else -> priorityChipGroup.clearCheck()
        }

        //state
        when (currentState) {
            requireContext().getString(R.string.text_state_pending) -> stateChipGroup.check(R.id.state_pending_chip)
            requireContext().getString(R.string.text_state_assigned) -> stateChipGroup.check(R.id.state_assigned_chip)
            requireContext().getString(R.string.text_state_under_repair) -> stateChipGroup.check(R.id.state_under_repair_chip)
            requireContext().getString(R.string.text_state_resolved) -> stateChipGroup.check(R.id.state_resolved_chip)
            else -> stateChipGroup.clearCheck()
        }

        //equipment status
        when (currentEquipmentStatus) {
            true -> equipmentStatusChipGroup.check(R.id.status_active_chip)
            false -> equipmentStatusChipGroup.check(R.id.status_inactive_chip)
            else -> equipmentStatusChipGroup.clearCheck()
        }

        //date
        publicationDateEditText.setText(currentDate ?: "")
    }

    private fun clearFilters() {
        //clear ui
        ownershipChipGroup.clearCheck()
        priorityChipGroup.clearCheck()
        stateChipGroup.clearCheck()
        equipmentStatusChipGroup.clearCheck()
        publicationDateEditText.text.clear()

        //clear stored state
        currentOwnership = null
        currentPriority = null
        currentState = null
        currentDate = null
        currentEquipmentStatus = null

        clearCallback?.invoke()
    }

    private fun applyFilters() {

        ownershipCallback?.invoke(currentOwnership)
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