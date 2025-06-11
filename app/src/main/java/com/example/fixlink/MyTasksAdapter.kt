package com.example.fixlink

import android.graphics.drawable.GradientDrawable
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.data.entities.*

class MyTasksAdapter(
    private val issues: List<Issue>,
    private val maintenances: List<Maintenance>,
    private var priorities: List<Priority>,
    private var equipments: List<Equipment>,
    private var locations: List<Location>,
    private var states: List<Issue_state>,
    private var users: List<User>,
    private var maintenanceStates: List<State_maintenance>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ISSUE = 0
        private const val TYPE_MAINTENANCE = 1
    }

    fun setAuxiliaryData(
        priorities: List<Priority>,
        equipments: List<Equipment>,
        locations: List<Location>,
        states: List<Issue_state>,
        users: List<User>,
        maintenanceStates: List<State_maintenance>
    ) {
        this.priorities = priorities
        this.equipments = equipments
        this.locations = locations
        this.states = states
        this.users = users
        this.maintenanceStates = maintenanceStates
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < issues.size) TYPE_ISSUE else TYPE_MAINTENANCE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ISSUE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_issue, parent, false)
            IssueViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_maintenance, parent, false)
            MaintenanceViewHolder(view)
        }
    }

    override fun getItemCount(): Int = issues.size + maintenances.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IssueViewHolder) {
            val issue = issues[position]
            holder.bind(issue, priorities, equipments, locations, states, users)
        } else if (holder is MaintenanceViewHolder) {
            val maintenance = maintenances[position - issues.size]
            holder.bind(maintenance, priorities, equipments, locations, maintenanceStates, users)
        }
    }

    class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.taskTypeIcon)
        private val titleTextView: TextView = itemView.findViewById(R.id.issueTitleTextView)
        private val reporterTextView: TextView = itemView.findViewById(R.id.issueReporterTextView)
        private val priorityChip: TextView = itemView.findViewById(R.id.priorityChip)
        private val statusChip: TextView = itemView.findViewById(R.id.statusChip)
        private val equipmentChip: TextView = itemView.findViewById(R.id.equipmentChip)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)

        private fun setChipColor(chip: TextView, color: Int) {
            val drawable = GradientDrawable()
            drawable.cornerRadius = 32f
            drawable.setColor(color)
            chip.background = drawable
        }

        fun bind(
            issue: Issue,
            priorities: List<Priority>,
            equipments: List<Equipment>,
            locations: List<Location>,
            states: List<Issue_state>,
            users: List<User>
        ) {
            icon.setImageResource(R.drawable.ic_issues)
            titleTextView.text = issue.title ?: "(Sem título)"
            val user = users.find { it.user_id == issue.id_user }
            val userName = if (user != null) "${user.firstname} ${user.lastname}" else issue.id_user
            reporterTextView.text = "Utilizador: $userName"
            val statusText = when (issue.state_id) {
                1 -> itemView.context.getString(R.string.text_state_pending)
                2 -> itemView.context.getString(R.string.text_state_assigned)
                3 -> itemView.context.getString(R.string.text_state_under_repair)
                4 -> itemView.context.getString(R.string.text_state_resolved)
                else -> issue.state_id.toString()
            }
            val equipment = equipments.find { it.equipment_id == issue.id_equipment }
            val equipmentState = if (equipment != null) {
                if (equipment.active) itemView.context.getString(R.string.text_status_active)
                else itemView.context.getString(R.string.text_status_inactive)
            } else "?"
            val locationText = locations.find { it.location_id == issue.localization_id }?.name ?: issue.localization_id.toString()

            statusChip.text = statusText
            equipmentChip.text = equipmentState
            locationTextView.text = locationText

            // Cores para prioridade
            when (issue.priority_id) {
                1 -> setChipColor(priorityChip, Color.parseColor("#B2DFDB")) // Verde claro - Low
                2 -> setChipColor(priorityChip, Color.parseColor("#FFEB3B")) // Amarelo - Medium
                3 -> setChipColor(priorityChip, Color.parseColor("#FF5252")) // Vermelho - High
                else -> setChipColor(priorityChip, Color.LTGRAY)
            }
            // Cores para status
            when (issue.state_id) {
                1 -> setChipColor(statusChip, Color.parseColor("#E0E0E0")) // Cinza claro - Pending
                2 -> setChipColor(statusChip, Color.parseColor("#B3E5FC")) // Azul claro - Assigned
                3 -> setChipColor(statusChip, Color.parseColor("#D6CDEA")) // Lilás claro - Under Repair
                4 -> setChipColor(statusChip, Color.parseColor("#66BB6A")) // Verde - Resolved
                else -> setChipColor(statusChip, Color.LTGRAY)
            }
            // Cores para equipamento
            when (equipmentState.lowercase()) {
                itemView.context.getString(R.string.text_status_active).lowercase() -> setChipColor(equipmentChip, Color.parseColor("#FFC107")) // Amarelo
                itemView.context.getString(R.string.text_status_inactive).lowercase() -> setChipColor(equipmentChip, Color.parseColor("#00BCD4")) // Azul
                else -> setChipColor(equipmentChip, Color.LTGRAY)
            }
        }
    }

    class MaintenanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.taskTypeIcon)
        private val titleTextView: TextView = itemView.findViewById(R.id.maintenanceTitleTextView)
        private val creatorTextView: TextView = itemView.findViewById(R.id.maintenanceCreatorTextView)
        private val priorityChip: TextView = itemView.findViewById(R.id.priorityChip)
        private val statusChip: TextView = itemView.findViewById(R.id.statusChip)
        private val equipmentChip: TextView = itemView.findViewById(R.id.equipmentChip)
        private val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)

        private fun setChipColor(chip: TextView, color: Int) {
            val drawable = GradientDrawable()
            drawable.cornerRadius = 32f
            drawable.setColor(color)
            chip.background = drawable
        }

        fun bind(
            maintenance: Maintenance,
            priorities: List<Priority>,
            equipments: List<Equipment>,
            locations: List<Location>,
            states: List<State_maintenance>,
            users: List<User>
        ) {
            icon.setImageResource(R.drawable.ic_maintenance)
            titleTextView.text = maintenance.title ?: "(Sem título)"
            val user = users.find { it.user_id == maintenance.id_user }
            val userName = if (user != null) {
                if (user.lastname.isNullOrEmpty()) user.firstname else "${user.firstname} ${user.lastname}"
            } else maintenance.id_user
            creatorTextView.text = "Utilizador: $userName"
            val priorityText = when (maintenance.priority_id) {
                1 -> itemView.context.getString(R.string.text_priority_low)
                2 -> itemView.context.getString(R.string.text_priority_medium)
                3 -> itemView.context.getString(R.string.text_priority_high)
                else -> maintenance.priority_id.toString()
            }
            val statusText = when (maintenance.state_id) {
                1 -> itemView.context.getString(R.string.text_state_pending)
                2 -> itemView.context.getString(R.string.text_state_assigned)
                3 -> itemView.context.getString(R.string.text_state_ongoing)
                4 -> itemView.context.getString(R.string.text_state_completed)
                else -> maintenance.state_id.toString()
            }
            val equipment = equipments.find { it.equipment_id == maintenance.id_equipment }
            val equipmentState = if (equipment != null) {
                if (equipment.active) itemView.context.getString(R.string.text_status_active)
                else itemView.context.getString(R.string.text_status_inactive)
            } else "?"
            val locationText = locations.find { it.location_id == maintenance.localization_id }?.name ?: maintenance.localization_id.toString()

            priorityChip.text = priorityText
            statusChip.text = statusText
            equipmentChip.text = equipmentState
            locationTextView.text = locationText

            // Cores para prioridade
            when (maintenance.priority_id) {
                1 -> setChipColor(priorityChip, Color.parseColor("#B2DFDB")) // Verde claro - Low
                2 -> setChipColor(priorityChip, Color.parseColor("#FFEB3B")) // Amarelo - Medium
                3 -> setChipColor(priorityChip, Color.parseColor("#FF5252")) // Vermelho - High
                else -> setChipColor(priorityChip, Color.LTGRAY)
            }
            // Cores para status de manutenção
            when (maintenance.state_id) {
                1 -> setChipColor(statusChip, Color.parseColor("#E0E0E0")) // Cinza claro - Pending
                2 -> setChipColor(statusChip, Color.parseColor("#B3E5FC")) // Azul claro - Assigned
                3 -> setChipColor(statusChip, Color.parseColor("#D6CDEA")) // Lilás claro - Under Repair
                4 -> setChipColor(statusChip, Color.parseColor("#66BB6A")) // Verde - Resolved
                else -> setChipColor(statusChip, Color.LTGRAY)
            }
            // Cores para equipamento
            when (equipmentState.lowercase()) {
                itemView.context.getString(R.string.text_status_active).lowercase() -> setChipColor(equipmentChip, Color.parseColor("#FFC107")) // Amarelo
                itemView.context.getString(R.string.text_status_inactive).lowercase() -> setChipColor(equipmentChip, Color.parseColor("#00BCD4")) // Azul
                else -> setChipColor(equipmentChip, Color.LTGRAY)
            }
        }
    }
} 