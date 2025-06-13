package com.example.fixlink.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.components.Legend
import com.example.fixlink.data.repository.*
import com.example.fixlink.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.button.MaterialButton
import android.widget.Toast
import com.example.fixlink.R
import com.example.fixlink.adapters.RecentActivityAdapter
import com.example.fixlink.RecentActivityFullFragment
import java.util.concurrent.TimeUnit

class AdminDashboardFragment : Fragment() {
    private lateinit var totalIssuesText: TextView
    private lateinit var openIssuesText: TextView
    private lateinit var activeEquipmentText: TextView
    private lateinit var totalEquipmentText: TextView
    private lateinit var totalUsersText: TextView
    private lateinit var activeUsersText: TextView
    private lateinit var totalLocationsText: TextView
    private lateinit var activeLocationsText: TextView
    private lateinit var avgIssueTimeText: TextView
    private lateinit var avgIssueTimeDaysText: TextView
    private lateinit var avgMaintenanceTimeText: TextView
    private lateinit var avgMaintenanceTimeDaysText: TextView
    private lateinit var viewAllButton: MaterialButton
    private lateinit var issuesByStateChart: PieChart
    private lateinit var issuesByPriorityChart: BarChart
    private lateinit var issuesByLocationChart: HorizontalBarChart
    private lateinit var recentActivityRecyclerView: RecyclerView
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private lateinit var loadingView: View
    private lateinit var contentView: View

    private val issueRepository = IssueRepository()
    private val equipmentRepository = EquipmentRepository()
    private val stateIssueRepository = StateIssueRepository()
    private val priorityRepository = PriorityRepository()
    private val locationRepository = LocationRepository()
    private val userRepository = UserRepository()
    private val maintenanceRepository = MaintenanceRepository()

    private var currentIssues: List<Issue> = emptyList()
    private var currentUsers: List<User> = emptyList()
    private var currentEquipments: List<Equipment> = emptyList()
    private var currentLocations: List<Location> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        loadingView = view.findViewById(R.id.loadingView)
        contentView = view.findViewById(R.id.contentView)
        totalIssuesText = view.findViewById(R.id.totalIssuesText)
        openIssuesText = view.findViewById(R.id.openIssuesText)
        activeEquipmentText = view.findViewById(R.id.activeEquipmentText)
        totalEquipmentText = view.findViewById(R.id.totalEquipmentText)
        totalUsersText = view.findViewById(R.id.totalUsersText)
        activeUsersText = view.findViewById(R.id.activeUsersText)
        totalLocationsText = view.findViewById(R.id.totalLocationsText)
        activeLocationsText = view.findViewById(R.id.activeLocationsText)
        avgIssueTimeText = view.findViewById(R.id.avgIssueTimeText)
        avgIssueTimeDaysText = view.findViewById(R.id.avgIssueTimeDaysText)
        avgMaintenanceTimeText = view.findViewById(R.id.avgMaintenanceTimeText)
        avgMaintenanceTimeDaysText = view.findViewById(R.id.avgMaintenanceTimeDaysText)
        issuesByStateChart = view.findViewById(R.id.issuesByStateChart)
        issuesByPriorityChart = view.findViewById(R.id.issuesByPriorityChart)
        issuesByLocationChart = view.findViewById(R.id.issuesByLocationChart)
        recentActivityRecyclerView = view.findViewById(R.id.recentActivityRecyclerView)
        viewAllButton = view.findViewById(R.id.viewAllButton)

        // Setup charts
        setupCharts()

        // Setup recent activity list
        setupRecentActivityList()

        // Setup view all button
        viewAllButton.setOnClickListener {
            showFullRecentActivity()
        }

        // Show loading and load data
        showLoading(true)
        loadDashboardData()
    }

    private fun showLoading(show: Boolean) {
        loadingView.visibility = if (show) View.VISIBLE else View.GONE
        contentView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun setupCharts() {
        // Setup Pie Chart (Issues by State)
        issuesByStateChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            setTransparentCircleRadius(61f)
            setDrawCenterText(true)
            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            legend.isEnabled = true
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(12f)
        }

        // Setup Bar Chart (Issues by Priority)
        issuesByPriorityChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            xAxis.apply {
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            }
        }

        // Setup Horizontal Bar Chart (Issues by Location)
        issuesByLocationChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            setScaleEnabled(false)
            axisLeft.apply {
                setDrawGridLines(false)
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
            xAxis.apply {
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            }
        }
    }

    private fun setupRecentActivityList() {
        recentActivityRecyclerView.layoutManager = LinearLayoutManager(context)
        recentActivityAdapter = RecentActivityAdapter(emptyList(), emptyList())
        recentActivityRecyclerView.adapter = recentActivityAdapter
    }

    private fun showFullRecentActivity() {
        val fragment = RecentActivityFullFragment.newInstance(
            currentIssues,
            currentUsers,
            currentEquipments,
            currentLocations
        )

        // Add fragment with animation
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.dashboardContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadDashboardData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Load all necessary data
                val issues = issueRepository.getAllIssues().getOrNull() ?: emptyList()
                val equipments = equipmentRepository.getEquipmentList().getOrNull() ?: emptyList()
                val states = stateIssueRepository.getIssueStates().getOrNull() ?: emptyList()
                val priorities = priorityRepository.getPriorityList().getOrNull() ?: emptyList()
                val locations = locationRepository.getLocationList().getOrNull() ?: emptyList()
                val users = userRepository.getAllUsers().getOrNull() ?: emptyList()
                val maintenanceList = maintenanceRepository.getAllMaintenance().getOrNull() ?: emptyList()

                withContext(Dispatchers.Main) {
                    // Store current data for full view
                    currentIssues = issues
                    currentUsers = users
                    currentEquipments = equipments
                    currentLocations = locations

                    // Update overview statistics
                    updateOverviewStats(issues, equipments, users, locations, maintenanceList)
                    
                    // Update charts
                    updateIssuesByStateChart(issues, states)
                    updateIssuesByPriorityChart(issues, priorities)
                    updateIssuesByLocationChart(issues, locations)
                    
                    // Update recent activity
                    updateRecentActivity(issues, users, equipments, locations)

                    // Hide loading and show content
                    showLoading(false)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Error loading dashboard data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateOverviewStats(
        issues: List<Issue>,
        equipments: List<Equipment>,
        users: List<User>,
        locations: List<Location>,
        maintenanceList: List<Maintenance>
    ) {
        // Issues stats
        val totalIssues = issues.size
        val openIssues = issues.count { it.state_id == 1 } // Assuming 1 is the ID for "Open" state
        totalIssuesText.text = totalIssues.toString()
        openIssuesText.text = getString(R.string.text_open_issues, openIssues)

        // Equipment stats
        val activeEquipment = equipments.count { it.active }
        val totalEquipment = equipments.size
        activeEquipmentText.text = activeEquipment.toString()
        totalEquipmentText.text = getString(R.string.text_of_total, totalEquipment)

        // Users stats
        val totalUsers = users.size
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val activeUsers = users.count { user ->
            issues.any { it.id_user == user.user_id && it.publicationDate.startsWith(today) }
        }
        totalUsersText.text = totalUsers.toString()
        activeUsersText.text = getString(R.string.text_active_users, activeUsers)

        // Locations stats
        val totalLocations = locations.size
        val activeLocations = locations.count { location ->
            issues.any { it.localization_id == location.location_id && it.state_id != 3 } // Assuming 3 is "Closed" state
        }
        totalLocationsText.text = totalLocations.toString()
        activeLocationsText.text = getString(R.string.text_active_locations, activeLocations)

        // Calculate average issue time
        val completedIssues = issues.filter { 
            it.state_id == 4 && it.beginningDate != null && it.endingDate != null 
        } // Only count resolved issues with both dates
        val avgIssueTime = if (completedIssues.isNotEmpty()) {
            val totalMillis = completedIssues.sumOf { issue ->
                val startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(issue.beginningDate!!)
                val endDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(issue.endingDate!!)
                endDate.time - startDate.time
            }
            val avgMillis = totalMillis / completedIssues.size
            val hours = TimeUnit.MILLISECONDS.toHours(avgMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(avgMillis) % 60
            
            if (hours >= 1) {
                avgIssueTimeText.text = getString(R.string.text_hours, hours)
                avgIssueTimeDaysText.text = getString(R.string.text_minutes, minutes)
            } else {
                avgIssueTimeText.text = getString(R.string.text_minutes, minutes)
                avgIssueTimeDaysText.text = ""
            }
        } else {
            avgIssueTimeText.text = getString(R.string.text_minutes, 0)
            avgIssueTimeDaysText.text = ""
        }

        // Calculate average maintenance time
        val completedMaintenance = maintenanceList.filter { 
            it.state_id == 4 && it.beginningDate != null && it.endingDate != null 
        } // Only count completed maintenance with both dates
        val avgMaintenanceTime = if (completedMaintenance.isNotEmpty()) {
            val totalMillis = completedMaintenance.sumOf { maintenance ->
                val startDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(maintenance.beginningDate!!)
                val endDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(maintenance.endingDate!!)
                endDate.time - startDate.time
            }
            val avgMillis = totalMillis / completedMaintenance.size
            val hours = TimeUnit.MILLISECONDS.toHours(avgMillis)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(avgMillis) % 60
            
            if (hours >= 1) {
                avgMaintenanceTimeText.text = getString(R.string.text_hours, hours)
                avgMaintenanceTimeDaysText.text = getString(R.string.text_minutes, minutes)
            } else {
                avgMaintenanceTimeText.text = getString(R.string.text_minutes, minutes)
                avgMaintenanceTimeDaysText.text = ""
            }
        } else {
            avgMaintenanceTimeText.text = getString(R.string.text_minutes, 0)
            avgMaintenanceTimeDaysText.text = ""
        }
    }

    private fun updateIssuesByStateChart(issues: List<Issue>, states: List<Issue_state>) {
        val entries = states.map { state ->
            val count = issues.count { it.state_id == state.state_id }
            val label = when(state.state.lowercase()) {
                "pending" -> getString(R.string.text_state_pending)
                "assigned" -> getString(R.string.text_state_assigned)
                "under repair" -> getString(R.string.text_state_under_repair)
                "resolved" -> getString(R.string.text_state_resolved)
                "cancelled" -> getString(R.string.text_state_cancelled)
                else -> state.state
            }
            PieEntry(count.toFloat(), label)
        }.filter { it.value > 0 }

        val dataSet = PieDataSet(entries, getString(R.string.text_chart_issues_by_status)).apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueFormatter = PercentFormatter(issuesByStateChart)
        }

        issuesByStateChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            legend.textSize = 12f
            legend.form = Legend.LegendForm.CIRCLE
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            animateY(1000)
            invalidate()
        }
    }

    private fun updateIssuesByPriorityChart(issues: List<Issue>, priorities: List<Priority>) {
        val entries = priorities.map { priority ->
            val count = issues.count { it.priority_id == priority.priority_id }
            val label = when(priority.priority) {
                "Low" -> getString(R.string.text_chart_legend_low)
                "Medium" -> getString(R.string.text_chart_legend_medium)
                "High" -> getString(R.string.text_chart_legend_high)
                else -> priority.priority
            }
            BarEntry(priority.priority_id.toFloat(), count.toFloat())
        }

        val dataSet = BarDataSet(entries, getString(R.string.text_chart_issues_by_priority)).apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
        }

        val labels = priorities.map { priority ->
            when(priority.priority) {
                "Low" -> getString(R.string.text_chart_legend_low)
                "Medium" -> getString(R.string.text_chart_legend_medium)
                "High" -> getString(R.string.text_chart_legend_high)
                else -> priority.priority
            }
        }
        val xAxis = issuesByPriorityChart.xAxis
        xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)

        issuesByPriorityChart.data = BarData(dataSet)
        issuesByPriorityChart.invalidate()
    }

    private fun updateIssuesByLocationChart(issues: List<Issue>, locations: List<Location>) {
        val entries = locations.map { location ->
            val count = issues.count { it.localization_id == location.location_id }
            BarEntry(location.location_id.toFloat(), count.toFloat())
        }.filter { it.y > 0 }

        val dataSet = BarDataSet(entries, getString(R.string.text_chart_maintenance_by_status)).apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
        }

        val labels = locations.map { it.name }
        val yAxis = issuesByLocationChart.axisLeft
        yAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)

        issuesByLocationChart.data = BarData(dataSet)
        issuesByLocationChart.invalidate()
    }

    private fun updateRecentActivity(
        issues: List<Issue>,
        users: List<User>,
        equipments: List<Equipment>,
        locations: List<Location>
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val recentIssues = issues.sortedByDescending { 
            dateFormat.parse(it.publicationDate) 
        }.take(5)

        recentActivityAdapter = RecentActivityAdapter(recentIssues, users)
        recentActivityRecyclerView.adapter = recentActivityAdapter
    }
} 