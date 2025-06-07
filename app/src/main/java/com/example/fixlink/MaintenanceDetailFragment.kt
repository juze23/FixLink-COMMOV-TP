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

class MaintenanceDetailFragment : Fragment() {
    companion object {
        private const val TAG = "MaintenanceDetailFragment"
        private const val ARG_MAINTENANCE_ID = "maintenance_id"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            maintenanceId = it.getString(ARG_MAINTENANCE_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_maintenance_detail, container, false)
        
        // Initialize repositories
        maintenanceRepository = MaintenanceRepository()
        priorityRepository = PriorityRepository()
        equipmentRepository = EquipmentRepository()
        locationRepository = LocationRepository()
        stateMaintenanceRepository = StateMaintenanceRepository()
        userRepository = UserRepository()

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
                if (maintenanceResult.isFailure) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Error loading maintenance", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val maintenance = maintenanceResult.getOrNull() ?: return@launch

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
        maintenanceTitle.text = maintenance.description ?: "(No description)"
        
        // Set creator name
        val creatorName = users.find { it.user_id == maintenance.id_user }?.name ?: maintenance.id_user
        maintenanceCreator.text = "User: $creatorName"

        // Set description
        maintenanceDescription.text = maintenance.description

        // Set location
        val locationName = locations.find { it.location_id == maintenance.localization_id }?.name ?: maintenance.localization_id.toString()
        maintenanceLocation.text = locationName

        // Set responsible technician
        val technicianName = if (maintenance.id_technician != null) {
            users.find { it.user_id == maintenance.id_technician }?.name ?: maintenance.id_technician
        } else {
            "Not assigned"
        }
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
        val priorityText = priorities.find { it.priority_id == maintenance.priority_id }?.priority ?: maintenance.priority_id.toString()
        priorityChip.text = priorityText
        setChipColor(priorityChip, getPriorityColor(priorityText))

        // Set status chip
        val statusText = states.find { it.state_id == maintenance.state_id }?.state ?: maintenance.state_id.toString()
        statusChip.text = statusText
        setChipColor(statusChip, getStatusColor(statusText))

        // Set equipment chip
        val equipment = equipments.find { it.equipment_id == maintenance.id_equipment }
        val equipmentState = if (equipment != null) if (equipment.active) "Active" else "Inactive" else "?"
        equipmentChip.text = equipmentState
        setChipColor(equipmentChip, getEquipmentColor(equipmentState))

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
            "pending", "pendente" -> Color.parseColor("#D3D3D3") // Cinza claro
            "assigned", "atribuído", "atribuido" -> Color.parseColor("#ADD8E6") // Azul claro
            "ongoing", "em curso" -> Color.parseColor("#D6CDEA") // Lilás claro
            "completed", "terminada" -> Color.parseColor("#6DBF5B") // Verde
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

    private fun setChipColor(chip: TextView, color: Int) {
        val drawable = GradientDrawable()
        drawable.cornerRadius = 32f
        drawable.setColor(color)
        chip.background = drawable
    }
}