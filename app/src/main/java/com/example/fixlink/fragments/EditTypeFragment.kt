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
import androidx.lifecycle.lifecycleScope
import com.example.fixlink.R
import com.example.fixlink.activities.AdminActivity
import com.example.fixlink.data.entities.Issue_type
import com.example.fixlink.data.entities.Type_maintenance
import com.example.fixlink.data.repository.MaintenanceTypeRepository
import com.example.fixlink.data.repository.IssueTypeRepository
import kotlinx.coroutines.launch

class EditTypeFragment : Fragment() {
    private lateinit var typeNameTextView: TextView
    private lateinit var nameEditText: EditText
    private lateinit var cancelButton: Button
    private lateinit var saveButton: Button

    private lateinit var maintenanceTypeRepository: MaintenanceTypeRepository
    private lateinit var issueTypeRepository: IssueTypeRepository
    private var currentType: Any? = null
    private var typeCategory: String = ""

    companion object {
        private const val TAG = "EditTypeFragment"
        private const val ARG_TYPE_ID = "type_id"
        private const val ARG_TYPE_NAME = "type_name"
        private const val ARG_TYPE_CATEGORY = "type_category"

        fun newInstance(typeId: Int, typeName: String, typeCategory: String) = EditTypeFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_TYPE_ID, typeId)
                putString(ARG_TYPE_NAME, typeName)
                putString(ARG_TYPE_CATEGORY, typeCategory)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val typeId = it.getInt(ARG_TYPE_ID)
            val typeName = it.getString(ARG_TYPE_NAME) ?: ""
            typeCategory = it.getString(ARG_TYPE_CATEGORY) ?: ""
            currentType = when (typeCategory) {
                "maintenance" -> Type_maintenance(type_id = typeId, type = typeName)
                "issue" -> Issue_type(type_id = typeId, type = typeName)
                else -> null
            }
        }
        maintenanceTypeRepository = MaintenanceTypeRepository()
        issueTypeRepository = IssueTypeRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        typeNameTextView = view.findViewById(R.id.typeNameTextView)
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
        val currentTypeName = when (currentType) {
            is Type_maintenance -> (currentType as Type_maintenance).type
            is Issue_type -> (currentType as Issue_type).type
            else -> ""
        }
        typeNameTextView.text = currentTypeName
        nameEditText.setText(currentTypeName)

        // Set click listeners
        cancelButton.setOnClickListener { 
            try {
                nameEditText.clearFocus()
                (activity as? AdminActivity)?.hideEditFragment()
            } catch (e: Exception) {
                Log.e(TAG, "Error dismissing fragment", e)
            }
        }
        saveButton.setOnClickListener { 
            try {
                nameEditText.clearFocus()
                saveChanges() 
            } catch (e: Exception) {
                Log.e(TAG, "Error saving changes", e)
                Toast.makeText(context, getString(R.string.error_updating_type), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        try {
            nameEditText.setOnFocusChangeListener(null)
            super.onDestroyView()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroyView", e)
        }
    }

    private fun saveChanges() {
        val newName = nameEditText.text.toString().trim()
        
        if (newName.isEmpty()) {
            Toast.makeText(context, getString(R.string.error_name_required), Toast.LENGTH_SHORT).show()
            return
        }

        val currentTypeName = when (currentType) {
            is Type_maintenance -> (currentType as Type_maintenance).type
            is Issue_type -> (currentType as Issue_type).type
            else -> ""
        }

        if (newName == currentTypeName) {
            Toast.makeText(context, getString(R.string.text_no_changes), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val typeId = when (currentType) {
                    is Type_maintenance -> (currentType as Type_maintenance).type_id
                    is Issue_type -> (currentType as Issue_type).type_id
                    else -> null
                }

                if (typeId == null) {
                    Toast.makeText(context, getString(R.string.error_type_not_found), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val result = when (typeCategory) {
                    "maintenance" -> maintenanceTypeRepository.updateType(typeId, newName)
                    "issue" -> issueTypeRepository.updateType(typeId, newName)
                    else -> Result.failure(Exception("Invalid type category"))
                }

                result.fold(
                    onSuccess = {
                        Toast.makeText(context, getString(R.string.text_type_updated), Toast.LENGTH_SHORT).show()
                        (activity as? AdminActivity)?.hideEditFragment()
                    },
                    onFailure = { e ->
                        Log.e(TAG, "Error updating type", e)
                        Toast.makeText(context, getString(R.string.error_updating_type), Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error updating type", e)
                Toast.makeText(context, getString(R.string.error_updating_type), Toast.LENGTH_SHORT).show()
            }
        }
    }
} 