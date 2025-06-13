package com.example.fixlink.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import android.widget.Toast
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.example.fixlink.data.entities.Maintenance
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.State_maintenance
import com.example.fixlink.data.entities.User
import com.example.fixlink.data.repository.MaintenanceRepository
import com.example.fixlink.data.repository.PriorityRepository
import com.example.fixlink.data.repository.EquipmentRepository
import com.example.fixlink.data.repository.LocationRepository
import com.example.fixlink.data.repository.StateMaintenanceRepository
import com.example.fixlink.data.repository.UserRepository
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log
import coil.load
import coil.request.CachePolicy
import coil.transition.CrossfadeTransition
import android.content.Intent
import android.app.Activity
import android.widget.Button
import com.example.fixlink.AssignTaskFragment
import com.example.fixlink.R
import com.example.fixlink.activities.ChooseTechnicianActivity
import com.example.fixlink.activities.ReportActivity
import com.example.fixlink.activities.ViewReportActivity

class MaintenanceDetailFragment : Fragment() {
    companion object {
        private const val TAG = "MaintenanceDetailFragment"
        private const val ARG_MAINTENANCE_ID = "maintenance_id"
        private const val REQUEST_ASSIGN_TECHNICIAN = 1001

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param maintenanceId The ID of the maintenance.
         * @return A new instance of fragment MaintenanceDetailFragment.
         */
        @JvmStatic
        fun newInstance(maintenanceId: String) =
            MaintenanceDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MAINTENANCE_ID, maintenanceId)
                }
            }
    }

    private var maintenanceId: String? = null
    private lateinit var maintenanceRepository: MaintenanceRepository
    private lateinit var priorityRepository: PriorityRepository
    private lateinit var equipmentRepository: EquipmentRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var stateMaintenanceRepository: StateMaintenanceRepository
    private lateinit var userRepository: UserRepository
    private var isAdmin: Boolean = false
    private var isTechnician: Boolean = false
    private var currentUserId: String? = null

    // Views
    private lateinit var maintenanceTitle: TextView
    private lateinit var maintenanceCreator: TextView
    private lateinit var maintenanceImage: ImageView
    private lateinit var maintenanceDescription: TextView
    private lateinit var maintenanceLocation: TextView
    private lateinit var responsibleTechnician: TextView
    private lateinit var maintenanceDate: TextView
    private lateinit var priorityChip: TextView
    private lateinit var statusChip: TextView
    private lateinit var equipmentChip: TextView
    private lateinit var loadingProgressBar: View
    private lateinit var contentScrollView: View
    private lateinit var startTaskButton: Button
    private lateinit var endTaskButton: Button
    private lateinit var assignTechnicianButton: Button
    private lateinit var viewReportButton: Button
    private lateinit var assignYourselfButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            maintenanceId = it.getString(ARG_MAINTENANCE_ID)
        }

        // Initialize repositories
        maintenanceRepository = MaintenanceRepository()
        priorityRepository = PriorityRepository()
        equipmentRepository = EquipmentRepository()
        locationRepository = LocationRepository()
        stateMaintenanceRepository = StateMaintenanceRepository()
        userRepository = UserRepository()

        // Get current user role
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userResult = userRepository.getCurrentUser()
                if (userResult.isSuccess) {
                    val user = userResult.getOrNull()
                    isAdmin = user?.typeId == 3  // 3 is the typeId for admin
                    Log.d(TAG, "User typeId: ${user?.typeId}, isAdmin: $isAdmin")
                } else {
                    Log.e(TAG, "Failed to get current user: ${userResult.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user role", e)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_maintenance_detail, container, false)
        
        // Initialize views
        initializeViews(view)
        
        // Show loading state
        showLoading(true)
        
        // Load maintenance data
        loadMaintenanceData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Show back button in top app bar
        val topAppBarFragment = parentFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.showBackButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Hide back button in top app bar
        val topAppBarFragment = parentFragmentManager.findFragmentById(R.id.topAppBarFragmentContainer) as? TopAppBarFragment
        topAppBarFragment?.hideBackButton()
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning from ReportActivity
        loadMaintenanceData()
    }

    private fun initializeViews(view: View) {
        maintenanceTitle = view.findViewById(R.id.maintenanceTitle)
        maintenanceCreator = view.findViewById(R.id.maintenanceCreator)
        maintenanceImage = view.findViewById(R.id.maintenanceImage)
        maintenanceDescription = view.findViewById(R.id.maintenanceDescription)
        maintenanceLocation = view.findViewById(R.id.maintenanceLocation)
        responsibleTechnician = view.findViewById(R.id.responsibleTechnician)
        maintenanceDate = view.findViewById(R.id.maintenanceDate)
        priorityChip = view.findViewById(R.id.priorityChip)
        statusChip = view.findViewById(R.id.statusChip)
        equipmentChip = view.findViewById(R.id.equipmentChip)
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        contentScrollView = view.findViewById(R.id.contentScrollView)
        startTaskButton = view.findViewById(R.id.startTaskButton)
        endTaskButton = view.findViewById(R.id.endTaskButton)
        assignTechnicianButton = view.findViewById(R.id.assignTechnicianButton)
        viewReportButton = view.findViewById(R.id.viewReportButton)
        assignYourselfButton = view.findViewById(R.id.assignYourselfButton)

        // Set click listener for assign technician button
        assignTechnicianButton.setOnClickListener {
            val intent = Intent(requireContext(), ChooseTechnicianActivity::class.java)
            intent.putExtra("MAINTENANCE_ID", maintenanceId)
            startActivityForResult(intent, REQUEST_ASSIGN_TECHNICIAN)
        }

        assignYourselfButton.setOnClickListener {
            showAssignYourselfDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ASSIGN_TECHNICIAN && resultCode == Activity.RESULT_OK) {
            // Refresh maintenance data after technician assignment
            loadMaintenanceData()
        }
        if (requestCode == 2002 && resultCode == Activity.RESULT_OK) {
            assignYourselfToMaintenance()
        }
    }

    private fun showLoading(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        contentScrollView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun loadMaintenanceData() {
        val maintenanceId = maintenanceId ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load maintenance
                val maintenanceResult = maintenanceRepository.getMaintenanceById(maintenanceId)
                val userResult = userRepository.getCurrentUser()
                if (maintenanceResult.isFailure || userResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Error loading maintenance or user", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val maintenance = maintenanceResult.getOrNull()!!
                val user = userResult.getOrNull()!!
                isTechnician = user.typeId == 2
                currentUserId = user.user_id

                // Load auxiliary data
                val prioritiesResult = priorityRepository.getPriorityList()
                val equipmentsResult = equipmentRepository.getEquipmentList()
                val locationsResult = locationRepository.getLocationList()
                val statesResult = stateMaintenanceRepository.getMaintenanceStates()
                val usersResult = userRepository.getAllUsers()

                if (prioritiesResult.isFailure || equipmentsResult.isFailure || 
                    locationsResult.isFailure || statesResult.isFailure || usersResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Error loading auxiliary data", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val priorities = prioritiesResult.getOrNull() ?: emptyList()
                val equipments = equipmentsResult.getOrNull() ?: emptyList()
                val locations = locationsResult.getOrNull() ?: emptyList()
                val states = statesResult.getOrNull() ?: emptyList()
                val users = usersResult.getOrNull() ?: emptyList()

                withContext(Dispatchers.Main) {
                    displayMaintenanceData(maintenance, priorities, equipments, locations, states, users)
                    assignTechnicianButton.visibility = if (isAdmin && maintenance.id_technician == null) View.VISIBLE else View.GONE
                    val statusText = states.find { it.state_id == maintenance.state_id }?.state ?: ""
                    val showAssignYourself = isTechnician && maintenance.id_technician == null && statusText.lowercase() == "pending"
                    assignYourselfButton.visibility = if (showAssignYourself) View.VISIBLE else View.GONE
                    showLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayMaintenanceData(
        maintenance: Maintenance,
        priorities: List<Priority>,
        equipments: List<Equipment>,
        locations: List<Location>,
        states: List<State_maintenance>,
        users: List<User>
    ) {
        // Set basic info
        maintenanceTitle.text = maintenance.title ?: "(No title)"
        
        // Set creator name
        val creator = users.find { it.user_id == maintenance.id_user }
        val creatorName = if (creator != null) {
            if (creator.lastname.isNullOrEmpty()) creator.firstname else "${creator.firstname} ${creator.lastname}"
        } else maintenance.id_user
        maintenanceCreator.text = "User: $creatorName"

        // Set description
        maintenanceDescription.text = maintenance.description

        // Set location
        val locationName = locations.find { it.location_id == maintenance.localization_id }?.name ?: maintenance.localization_id.toString()
        maintenanceLocation.text = locationName

        // Set responsible technician
        val technician = if (maintenance.id_technician != null) {
            users.find { it.user_id == maintenance.id_technician }
        } else null
        val technicianName = if (technician != null) {
            if (technician.lastname.isNullOrEmpty()) technician.firstname else "${technician.firstname} ${technician.lastname}"
        } else "Not assigned"
        responsibleTechnician.text = technicianName

        // Set date (formatted to show only the date)
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(maintenance.publicationDate)
            maintenanceDate.text = date?.let { outputFormat.format(it) } ?: maintenance.publicationDate.split("T")[0] // Fallback to just the date part
        } catch (e: Exception) {
            // If parsing fails, try to extract just the date part
            maintenanceDate.text = maintenance.publicationDate.split("T")[0]
        }

        // Set priority chip
        val priorityText = when (maintenance.priority_id) {
            1 -> requireContext().getString(R.string.text_priority_low)
            2 -> requireContext().getString(R.string.text_priority_medium)
            3 -> requireContext().getString(R.string.text_priority_high)
            else -> maintenance.priority_id.toString()
        }
        priorityChip.text = priorityText
        // Set colors based on priority
        when (maintenance.priority_id) {
            1 -> setChipColor(priorityChip, Color.parseColor("#B2DFDB")) // Verde claro - Low
            2 -> setChipColor(priorityChip, Color.parseColor("#FFEB3B")) // Amarelo - Medium
            3 -> setChipColor(priorityChip, Color.parseColor("#FF5252")) // Vermelho - High
            else -> setChipColor(priorityChip, Color.LTGRAY)
        }

        // Set status chip
        val statusText = when (maintenance.state_id) {
            1 -> requireContext().getString(R.string.text_state_pending)
            2 -> requireContext().getString(R.string.text_state_assigned)
            3 -> requireContext().getString(R.string.text_state_ongoing)
            4 -> requireContext().getString(R.string.text_state_completed)
            else -> maintenance.state_id.toString()
        }
        statusChip.text = statusText
        // Set colors based on status
        when (maintenance.state_id) {
            1 -> setChipColor(statusChip, Color.parseColor("#E0E0E0")) // Cinza claro - Pending
            2 -> setChipColor(statusChip, Color.parseColor("#B3E5FC")) // Azul claro - Assigned
            3 -> setChipColor(statusChip, Color.parseColor("#D6CDEA")) // Lilás claro - Under Repair
            4 -> setChipColor(statusChip, Color.parseColor("#66BB6A")) // Verde - Resolved
            else -> setChipColor(statusChip, Color.LTGRAY)
        }

        // Set equipment chip
        val equipment = equipments.find { it.equipment_id == maintenance.id_equipment }
        val equipmentState = if (equipment != null) {
            if (equipment.active) requireContext().getString(R.string.text_status_active)
            else requireContext().getString(R.string.text_status_inactive)
        } else "?"
        equipmentChip.text = equipmentState
        // Set colors for equipment status
        when (equipmentState.lowercase()) {
            requireContext().getString(R.string.text_status_active).lowercase() -> setChipColor(equipmentChip, Color.parseColor("#FFC107")) // Amarelo
            requireContext().getString(R.string.text_status_inactive).lowercase() -> setChipColor(equipmentChip, Color.parseColor("#00BCD4")) // Azul
            else -> setChipColor(equipmentChip, Color.LTGRAY)
        }

        // Log the current status and admin state for debugging
        Log.d(TAG, "Current status: $statusText, isAdmin: $isAdmin")

        // Show/hide buttons based on status and user role
        when (maintenance.state_id) {
            1 -> { // Pending
                assignTechnicianButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
                startTaskButton.visibility = View.GONE
                endTaskButton.visibility = View.GONE
                viewReportButton.visibility = View.GONE
            }
            2 -> { // Assigned
                assignTechnicianButton.visibility = View.GONE
                // Show start task button for admin or the assigned technician
                val isAssignedTechnician = isTechnician && maintenance.id_technician == currentUserId
                startTaskButton.visibility = if (isAdmin || isAssignedTechnician) View.VISIBLE else View.GONE
                endTaskButton.visibility = View.GONE
                viewReportButton.visibility = View.GONE
            }
            3 -> { // Ongoing
                assignTechnicianButton.visibility = View.GONE
                startTaskButton.visibility = View.GONE
                // Show end task button for admin or the assigned technician
                val isAssignedTechnician = isTechnician && maintenance.id_technician == currentUserId
                endTaskButton.visibility = if (isAdmin || isAssignedTechnician) View.VISIBLE else View.GONE
                viewReportButton.visibility = View.GONE
            }
            4 -> { // Completed
                assignTechnicianButton.visibility = View.GONE
                startTaskButton.visibility = View.GONE
                endTaskButton.visibility = View.GONE
                viewReportButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
            }
            else -> {
                assignTechnicianButton.visibility = View.GONE
                startTaskButton.visibility = View.GONE
                endTaskButton.visibility = View.GONE
                viewReportButton.visibility = View.GONE
            }
        }

        startTaskButton.setOnClickListener {
            showChangeStatusDialog(
                title = requireContext().getString(R.string.text_change_status_title, requireContext().getString(R.string.text_state_ongoing)),
                message = requireContext().getString(R.string.text_change_status_message, requireContext().getString(R.string.text_state_ongoing)),
                onConfirm = {
                    changeMaintenanceStatus(maintenance, states, "ongoing")
                }
            )
        }

        endTaskButton.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle(requireContext().getString(R.string.text_report))
                .setMessage(requireContext().getString(R.string.text_change_status_message, requireContext().getString(R.string.text_state_completed)))
                .setPositiveButton(requireContext().getString(R.string.button_send_report)) { _, _ ->
                    val intent = Intent(requireContext(), ReportActivity::class.java)
                    intent.putExtra("MAINTENANCE_ID", maintenance.maintenance_id)
                    startActivity(intent)
                }
                .setNegativeButton(requireContext().getString(R.string.button_cancel), null)
                .show()
        }

        viewReportButton.setOnClickListener {
            val intent = Intent(requireContext(), ViewReportActivity::class.java)
            intent.putExtra("MAINTENANCE_ID", maintenance.maintenance_id)
            startActivity(intent)
        }

        assignYourselfButton.setOnClickListener {
            showAssignYourselfDialog()
        }

        // Load maintenance image
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val imageUrl = SupabaseClient.supabase.storage.from("fixlink")
                        .downloadAuthenticated("Maintenances/maintenance_${maintenance.maintenance_id}.jpg")
                    
                    withContext(Dispatchers.Main) {
                        maintenanceImage.load(imageUrl) {
                            crossfade(true)
                            error(R.drawable.placeholder_printer_image)
                            memoryCachePolicy(CachePolicy.ENABLED)
                            diskCachePolicy(CachePolicy.ENABLED)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading maintenance image: ${e.message}")
                    withContext(Dispatchers.Main) {
                        maintenanceImage.setImageResource(R.drawable.placeholder_printer_image)
                        Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up image loading: ${e.message}")
            maintenanceImage.setImageResource(R.drawable.placeholder_printer_image)
            Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangeStatusDialog(title: String, message: String, onConfirm: () -> Unit) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(requireContext().getString(R.string.button_confirm)) { _, _ -> onConfirm() }
            .setNegativeButton(requireContext().getString(R.string.button_cancel), null)
            .show()
    }

    private fun changeMaintenanceStatus(maintenance: Maintenance, states: List<State_maintenance>, newStatus: String, report: String? = null) {
        val newState = states.find { it.state.equals(newStatus, true) }
        if (newState == null) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.text_invalid_state), Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            // Map Portuguese status to English for database
            val englishStatus = when (newStatus.lowercase()) {
                "ongoing", "em curso" -> "Ongoing"
                "completed", "concluído", "concluido" -> "Completed"
                "assigned", "atribuído", "atribuido" -> "Assigned"
                "pending", "pendente" -> "Pending"
                "cancelled", "cancelado" -> "Cancelled"
                else -> newStatus
            }
            
            val notificationText = "Maintenance status changed to $englishStatus"
            
            val result = maintenanceRepository.changeMaintenanceStatus(maintenance.maintenance_id, newStatus, notificationText)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.text_status_updated), Toast.LENGTH_SHORT).show()
                    loadMaintenanceData()
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.text_error_updating_status), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAssignYourselfDialog() {
        val fragment = AssignTaskFragment()
        fragment.setTargetFragment(this, 2002)
        fragment.show(parentFragmentManager, "AssignTaskFragment")
    }

    private fun assignYourselfToMaintenance() {
        val maintenanceId = maintenanceId ?: return
        val technicianId = currentUserId ?: return
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            val notificationText = requireContext().getString(R.string.text_notification_maintenance_assigned)
            val result = maintenanceRepository.assignTechnicianToMaintenance(maintenanceId, technicianId, notificationText)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.text_assigned_to_maintenance), Toast.LENGTH_SHORT).show()
                    loadMaintenanceData()
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.text_error_assigning), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setChipColor(chip: TextView, color: Int) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 32f
        drawable.setColor(color)
        chip.background = drawable
    }
}