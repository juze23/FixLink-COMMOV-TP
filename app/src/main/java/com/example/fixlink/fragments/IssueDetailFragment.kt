package com.example.fixlink

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
import android.widget.ImageButton
import android.widget.Button
import android.widget.EditText
import coil.load
import coil.request.CachePolicy
import coil.transition.CrossfadeTransition
import com.example.fixlink.data.entities.Issue
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.Issue_state
import com.example.fixlink.data.entities.User
import com.example.fixlink.data.repository.IssueRepository
import com.example.fixlink.data.repository.PriorityRepository
import com.example.fixlink.data.repository.EquipmentRepository
import com.example.fixlink.data.repository.LocationRepository
import com.example.fixlink.data.repository.StateIssueRepository
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
import android.content.Intent
import android.app.Activity

class IssueDetailFragment : Fragment() {
    companion object {
        private const val TAG = "IssueDetailFragment"
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_ISSUE_ID = "issue_id"
        private const val REQUEST_ASSIGN_TECHNICIAN = 1001

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param issueId Parameter 1.
         * @return A new instance of fragment IssueDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(issueId: String) =
            IssueDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ISSUE_ID, issueId)
                }
            }
    }

    private var issueId: String? = null
    private lateinit var issueRepository: IssueRepository
    private lateinit var priorityRepository: PriorityRepository
    private lateinit var equipmentRepository: EquipmentRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var stateIssueRepository: StateIssueRepository
    private lateinit var userRepository: UserRepository

    // Views
    private lateinit var issueTitle: TextView
    private lateinit var issueReporter: TextView
    private lateinit var issueImage: ImageView
    private lateinit var issueDescription: TextView
    private lateinit var issueLocation: TextView
    private lateinit var responsibleTechnician: TextView
    private lateinit var issueDate: TextView
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

    private var isTechnician: Boolean = false
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            issueId = it.getString(ARG_ISSUE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_issue_detail, container, false)
        
        // Initialize repositories
        issueRepository = IssueRepository()
        priorityRepository = PriorityRepository()
        equipmentRepository = EquipmentRepository()
        locationRepository = LocationRepository()
        stateIssueRepository = StateIssueRepository()
        userRepository = UserRepository()

        // Initialize views
        initializeViews(view)
        
        // Show loading state
        showLoading(true)
        
        // Load issue data
        loadIssueData()

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
        loadIssueData()
    }

    private fun initializeViews(view: View) {
        issueTitle = view.findViewById(R.id.issueTitle)
        issueReporter = view.findViewById(R.id.issueReporter)
        issueImage = view.findViewById(R.id.issueImage)
        issueDescription = view.findViewById(R.id.issueDescription)
        issueLocation = view.findViewById(R.id.issueLocation)
        responsibleTechnician = view.findViewById(R.id.responsibleTechnician)
        issueDate = view.findViewById(R.id.issueDate)
        priorityChip = view.findViewById(R.id.statusMedium)
        statusChip = view.findViewById(R.id.statusPending)
        equipmentChip = view.findViewById(R.id.statusActive)
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
            intent.putExtra("ISSUE_ID", issueId)
            startActivityForResult(intent, REQUEST_ASSIGN_TECHNICIAN)
        }
        assignYourselfButton.setOnClickListener {
            showAssignYourselfDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ASSIGN_TECHNICIAN && resultCode == Activity.RESULT_OK) {
            // Refresh the issue data to show the assigned technician
            loadIssueData()
        }
        if (requestCode == 2001 && resultCode == Activity.RESULT_OK) {
            assignYourselfToIssue()
        }
    }

    private fun showLoading(show: Boolean) {
        loadingProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        contentScrollView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun loadIssueData() {
        val issueId = issueId ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load issue
                val issueResult = issueRepository.getIssueById(issueId)
                if (issueResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Error loading issue", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val issue = issueResult.getOrNull() ?: return@launch

                // Load auxiliary data
                val prioritiesResult = priorityRepository.getPriorityList()
                val equipmentsResult = equipmentRepository.getEquipmentList()
                val locationsResult = locationRepository.getLocationList()
                val statesResult = stateIssueRepository.getIssueStates()
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

                // Buscar utilizador atual
                val currentUserResult = userRepository.getCurrentUser()
                val isAdmin = currentUserResult.isSuccess && currentUserResult.getOrNull()?.typeId == 3
                val user = currentUserResult.getOrNull()
                if (user != null) {
                    isTechnician = user.typeId == 2
                    currentUserId = user.user_id
                }

                withContext(Dispatchers.Main) {
                    displayIssueData(issue, priorities, equipments, locations, states, users, isAdmin)
                    // Mostrar botão Assign Technician só para admin e quando não há técnico atribuído
                    assignTechnicianButton.visibility = if (isAdmin && issue.id_technician == null) View.VISIBLE else View.GONE
                    val statusText = states.find { it.state_id == issue.state_id }?.state ?: ""
                    val showAssignYourself = isTechnician && issue.id_technician == null && statusText.lowercase() == "pending"
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

    private fun displayIssueData(
        issue: Issue,
        priorities: List<Priority>,
        equipments: List<Equipment>,
        locations: List<Location>,
        states: List<Issue_state>,
        users: List<User>,
        isAdmin: Boolean
    ) {
        // Set basic info
        issueTitle.text = issue.title ?: "(No title)"
        
        // Set reporter name
        val reporter = users.find { it.user_id == issue.id_user }
        val reporterName = if (reporter != null) "${reporter.firstname} ${reporter.lastname}" else issue.id_user
        issueReporter.text = "User: $reporterName"

        // Set description
        issueDescription.text = issue.description

        // Set location
        val locationName = locations.find { it.location_id == issue.localization_id }?.name ?: issue.localization_id.toString()
        issueLocation.text = locationName

        // Set responsible technician
        val technician = users.find { it.user_id == issue.id_technician }
        val technicianName = if (technician != null) "${technician.firstname} ${technician.lastname}" else "Not assigned"
        responsibleTechnician.text = technicianName

        // Set date (formatted to show only the date)
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(issue.publicationDate)
            issueDate.text = date?.let { outputFormat.format(it) } ?: issue.publicationDate.split("T")[0] // Fallback to just the date part
        } catch (e: Exception) {
            // If parsing fails, try to extract just the date part
            issueDate.text = issue.publicationDate.split("T")[0]
        }

        // Set priority chip
        val priorityText = when (issue.priority_id) {
            1 -> requireContext().getString(R.string.text_priority_low)
            2 -> requireContext().getString(R.string.text_priority_medium)
            3 -> requireContext().getString(R.string.text_priority_high)
            else -> issue.priority_id.toString()
        }
        priorityChip.text = priorityText
        // Set colors based on priority
        when (issue.priority_id) {
            1 -> setChipColor(priorityChip, Color.parseColor("#B2DFDB")) // Verde claro - Low
            2 -> setChipColor(priorityChip, Color.parseColor("#FFEB3B")) // Amarelo - Medium
            3 -> setChipColor(priorityChip, Color.parseColor("#FF5252")) // Vermelho - High
            else -> setChipColor(priorityChip, Color.LTGRAY)
        }

        // Set status chip
        val statusText = when (issue.state_id) {
            1 -> requireContext().getString(R.string.text_state_pending)
            2 -> requireContext().getString(R.string.text_state_assigned)
            3 -> requireContext().getString(R.string.text_state_under_repair)
            4 -> requireContext().getString(R.string.text_state_resolved)
            else -> issue.state_id.toString()
        }
        statusChip.text = statusText
        // Set colors based on status
        when (issue.state_id) {
            1 -> setChipColor(statusChip, Color.parseColor("#E0E0E0")) // Cinza claro - Pending
            2 -> setChipColor(statusChip, Color.parseColor("#B3E5FC")) // Azul claro - Assigned
            3 -> setChipColor(statusChip, Color.parseColor("#D6CDEA")) // Lilás claro - Under Repair
            4 -> setChipColor(statusChip, Color.parseColor("#66BB6A")) // Verde - Resolved
            else -> setChipColor(statusChip, Color.LTGRAY)
        }

        // Set equipment chip
        val equipment = equipments.find { it.equipment_id == issue.id_equipment }
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

        // Load issue image
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val imageUrl = SupabaseClient.supabase.storage.from("fixlink")
                        .downloadAuthenticated("Issues/issue_${issue.issue_id}.jpg")
                    
                    withContext(Dispatchers.Main) {
                        issueImage.load(imageUrl) {
                            crossfade(true)
                            error(R.drawable.placeholder_printer_image)
                            memoryCachePolicy(CachePolicy.ENABLED)
                            diskCachePolicy(CachePolicy.ENABLED)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading issue image: ${e.message}")
                    withContext(Dispatchers.Main) {
                        issueImage.setImageResource(R.drawable.placeholder_printer_image)
                        Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up image loading: ${e.message}")
            issueImage.setImageResource(R.drawable.placeholder_printer_image)
            Toast.makeText(requireContext(), "Error loading image", Toast.LENGTH_SHORT).show()
        }

        // Show/hide buttons based on status and user role
        when (issue.state_id) {
            1 -> { // Pending
                assignTechnicianButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
                startTaskButton.visibility = View.GONE
                endTaskButton.visibility = View.GONE
                viewReportButton.visibility = View.GONE
            }
            2 -> { // Assigned
                assignTechnicianButton.visibility = View.GONE
                // Show start task button for admin or the assigned technician
                val isAssignedTechnician = isTechnician && issue.id_technician == currentUserId
                startTaskButton.visibility = if (isAdmin || isAssignedTechnician) View.VISIBLE else View.GONE
                endTaskButton.visibility = View.GONE
                viewReportButton.visibility = View.GONE
            }
            3 -> { // Under Repair
                assignTechnicianButton.visibility = View.GONE
                startTaskButton.visibility = View.GONE
                // Show end task button for admin or the assigned technician
                val isAssignedTechnician = isTechnician && issue.id_technician == currentUserId
                endTaskButton.visibility = if (isAdmin || isAssignedTechnician) View.VISIBLE else View.GONE
                viewReportButton.visibility = View.GONE
            }
            4 -> { // Resolved
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
                title = requireContext().getString(R.string.text_change_status_title, requireContext().getString(R.string.text_state_under_repair)),
                message = requireContext().getString(R.string.text_change_status_message, requireContext().getString(R.string.text_state_under_repair)),
                onConfirm = {
                    changeIssueStatus(issue, states, "under repair")
                }
            )
        }
        endTaskButton.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle(requireContext().getString(R.string.text_report))
                .setMessage(requireContext().getString(R.string.text_change_status_message, requireContext().getString(R.string.text_state_resolved)))
                .setPositiveButton(requireContext().getString(R.string.button_send_report)) { _, _ ->
                    val intent = Intent(requireContext(), ReportActivity::class.java)
                    intent.putExtra("ISSUE_ID", issue.issue_id)
                    startActivity(intent)
                }
                .setNegativeButton(requireContext().getString(R.string.button_cancel), null)
                .show()
        }

        viewReportButton.setOnClickListener {
            val intent = Intent(requireContext(), ViewReportActivity::class.java)
            intent.putExtra("ISSUE_ID", issue.issue_id)
            startActivity(intent)
        }
    }

    private fun setChipColor(chip: TextView, color: Int) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 32f
        drawable.setColor(color)
        chip.background = drawable
    }

    private fun showChangeStatusDialog(title: String, message: String, onConfirm: () -> Unit) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(requireContext().getString(R.string.button_confirm)) { _, _ -> onConfirm() }
            .setNegativeButton(requireContext().getString(R.string.button_cancel), null)
            .show()
    }

    private fun changeIssueStatus(issue: Issue, states: List<Issue_state>, newStatus: String, report: String? = null) {
        val newState = states.find { it.state.equals(newStatus, true) }
        if (newState == null) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.text_invalid_state), Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            // Map Portuguese status to English for database
            val englishStatus = when (newStatus.lowercase()) {
                "under repair", "em andamento", "em reparacao" -> "Under Repair"
                "resolved", "resolvido" -> "Resolved"
                "assigned", "atribuído", "atribuido" -> "Assigned"
                "pending", "pendente" -> "Pending"
                "cancelled", "cancelado" -> "Cancelled"
                else -> newStatus
            }
            
            val notificationText = "Issue status changed to $englishStatus"
            
            val result = issueRepository.changeIssueStatus(issue.issue_id, newStatus, notificationText)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.text_status_updated), Toast.LENGTH_SHORT).show()
                    loadIssueData()
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.text_error_updating_status), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showAssignYourselfDialog() {
        val fragment = AssignTaskFragment()
        fragment.setTargetFragment(this, 2001)
        fragment.show(parentFragmentManager, "AssignTaskFragment")
    }

    private fun assignYourselfToIssue() {
        val issueId = issueId ?: return
        val technicianId = currentUserId ?: return
        showLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            val notificationText = requireContext().getString(R.string.text_notification_issue_assigned)
            val result = issueRepository.assignTechnicianToIssue(issueId, technicianId, notificationText)
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (result.isSuccess) {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.text_assigned_to_issue), Toast.LENGTH_SHORT).show()
                    loadIssueData()
                } else {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.text_error_assigning), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}