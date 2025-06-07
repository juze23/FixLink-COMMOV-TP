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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_ISSUE_ID = "issue_id"

/**
 * A simple [Fragment] subclass.
 * Use the [IssueDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IssueDetailFragment : Fragment() {
    companion object {
        private const val TAG = "IssueDetailFragment"
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_ISSUE_ID = "issue_id"

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

                withContext(Dispatchers.Main) {
                    displayIssueData(issue, priorities, equipments, locations, states, users)
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
        users: List<User>
    ) {
        // Set basic info
        issueTitle.text = issue.description ?: "(Sem título)"
        
        // Set reporter name
        val reporterName = users.find { it.user_id == issue.id_user }?.name ?: issue.id_user
        issueReporter.text = "Utilizador: $reporterName"

        // Set description
        issueDescription.text = issue.description

        // Set location
        val locationName = locations.find { it.location_id == issue.localization_id }?.name ?: issue.localization_id.toString()
        issueLocation.text = locationName

        // Set responsible technician
        val technicianName = if (issue.id_technician != null) {
            users.find { it.user_id == issue.id_technician }?.name ?: issue.id_technician
        } else {
            "Não atribuído"
        }
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
        val priorityText = priorities.find { it.priority_id == issue.priority_id }?.priority ?: issue.priority_id.toString()
        priorityChip.text = priorityText
        setChipColor(priorityChip, getPriorityColor(priorityText))

        // Set status chip
        val statusText = states.find { it.state_id == issue.state_id }?.state ?: issue.state_id.toString()
        statusChip.text = statusText
        setChipColor(statusChip, getStatusColor(statusText))

        // Set equipment chip
        val equipment = equipments.find { it.equipment_id == issue.id_equipment }
        val equipmentState = if (equipment != null) if (equipment.active) "Active" else "Inactive" else "?"
        equipmentChip.text = equipmentState
        setChipColor(equipmentChip, getEquipmentColor(equipmentState))

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
    }

    private fun setChipColor(chip: TextView, color: Int) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 32f
        drawable.setColor(color)
        chip.background = drawable
    }

    private fun getPriorityColor(priority: String): Int {
        return when (priority.lowercase()) {
            "high", "alta" -> Color.parseColor("#FF5252") // Vermelho
            "medium", "média", "media" -> Color.parseColor("#FFEB3B") // Amarelo
            "low", "baixa" -> Color.parseColor("#B2DFDB") // Verde claro
            else -> Color.LTGRAY
        }
    }

    private fun getStatusColor(status: String): Int {
        return when (status.lowercase()) {
            "pending", "pendente" -> Color.parseColor("#E0E0E0") // Cinza claro
            "assigned", "atribuído", "atribuido" -> Color.parseColor("#B3E5FC") // Azul claro
            "under repair", "em reparação", "em reparacao" -> Color.parseColor("#E1E0F7") // Lilás claro
            "resolved", "resolvido" -> Color.parseColor("#66BB6A") // Verde
            else -> Color.LTGRAY
        }
    }

    private fun getEquipmentColor(state: String): Int {
        return when (state.lowercase()) {
            "active", "ativo" -> Color.parseColor("#FFC107") // Amarelo
            "inactive", "inativo" -> Color.parseColor("#00BCD4") // Azul
            else -> Color.LTGRAY
        }
    }
}