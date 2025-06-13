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
import androidx.fragment.app.DialogFragment
import com.example.fixlink.R
import com.example.fixlink.data.repository.PriorityRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditPriorityFragment : DialogFragment() {
    private lateinit var priorityNameTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    private lateinit var priorityRepository: PriorityRepository
    private var currentPriorityId: Int = 0
    private var currentPriorityName: String = ""

    companion object {
        private const val ARG_PRIORITY_ID = "priority_id"
        private const val ARG_PRIORITY_NAME = "priority_name"

        fun newInstance(priorityId: Int, priorityName: String) = EditPriorityFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_PRIORITY_ID, priorityId)
                putString(ARG_PRIORITY_NAME, priorityName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogTheme)
        arguments?.let {
            currentPriorityId = it.getInt(ARG_PRIORITY_ID)
            currentPriorityName = it.getString(ARG_PRIORITY_NAME) ?: ""
        }
        priorityRepository = PriorityRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_priority, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        priorityNameTextView = view.findViewById(R.id.priorityNameTextView)
        nameEditText = view.findViewById(R.id.nameEditText)
        cancelButton = view.findViewById(R.id.cancelButton)
        saveButton = view.findViewById(R.id.saveButton)

        // Set initial values
        priorityNameTextView.text = currentPriorityName
        nameEditText.setText(currentPriorityName)

        // Set click listeners
        cancelButton.setOnClickListener { dismiss() }
        saveButton.setOnClickListener { saveChanges() }
    }

    private fun saveChanges() {
        val newName = nameEditText.text.toString().trim()
        if (newName.isEmpty()) {
            nameEditText.error = getString(R.string.error_name_required)
            return
        }

        if (newName == currentPriorityName) {
            Toast.makeText(context, getString(R.string.text_no_changes), Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = priorityRepository.updatePriority(currentPriorityId, newName)
                withContext(Dispatchers.Main) {
                    if (result.isSuccess) {
                        Toast.makeText(context, getString(R.string.text_priority_updated), Toast.LENGTH_SHORT).show()
                        dismiss()
                    } else {
                        Toast.makeText(context, getString(R.string.error_updating_priority), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("EditPriorityFragment", "Error updating priority", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, getString(R.string.error_updating_priority, e.message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 