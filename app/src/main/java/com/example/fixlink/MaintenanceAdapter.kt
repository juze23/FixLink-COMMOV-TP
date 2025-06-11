package com.example.fixlink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.data.entities.Maintenance
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.User
import android.graphics.drawable.GradientDrawable
import android.graphics.Color
import com.example.fixlink.data.entities.State_maintenance

class MaintenanceAdapter(private val maintenances: List<Maintenance>) : RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder>() {
    private var priorities: List<Priority> = emptyList()
    private var equipments: List<Equipment> = emptyList()
    private var locations: List<Location> = emptyList()
    private var states: List<State_maintenance> = emptyList()
    private var users: List<User> = emptyList()

    fun setAuxiliaryData(
        priorities: List<Priority>,
        equipments: List<Equipment>,
        locations: List<Location>,
        states: List<State_maintenance>,
        users: List<User>
    ) {
        this.priorities = priorities
        this.equipments = equipments
        this.locations = locations
        this.states = states
        this.users = users
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_maintenance, parent, false)
        return MaintenanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {
        val maintenance = maintenances[position]
        holder.bind(maintenance, priorities, equipments, locations, states, users)

        // Add click listener to navigate to maintenance details
        holder.itemView.setOnClickListener {
            val fragment = MaintenanceDetailFragment.newInstance(maintenance.maintenance_id)
            val activity = holder.itemView.context as? androidx.fragment.app.FragmentActivity
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.maintenanceContentFragmentContainer, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    override fun getItemCount(): Int = maintenances.size

    class MaintenanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
            titleTextView.text = maintenance.title ?: "(No title)"
            
            // Set creator name
            val creator = users.find { it.user_id == maintenance.id_user }
            val creatorName = if (creator != null) {
                if (creator.lastname.isNullOrEmpty()) creator.firstname else "${creator.firstname} ${creator.lastname}"
            } else maintenance.id_user
            creatorTextView.text = "${itemView.context.getString(R.string.text_user_label)} $creatorName"

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

            // Set colors based on priority
            when (maintenance.priority_id) {
                1 -> setChipColor(priorityChip, Color.parseColor("#B2DFDB")) // Verde claro - Low
                2 -> setChipColor(priorityChip, Color.parseColor("#FFEB3B")) // Amarelo - Medium
                3 -> setChipColor(priorityChip, Color.parseColor("#FF5252")) // Vermelho - High
                else -> setChipColor(priorityChip, Color.GRAY)
            }

            // Set colors based on status
            when (maintenance.state_id) {
                1 -> setChipColor(statusChip, Color.parseColor("#D3D3D3")) // Cinza claro - Pending
                2 -> setChipColor(statusChip, Color.parseColor("#ADD8E6")) // Azul claro - Assigned
                3 -> setChipColor(statusChip, Color.parseColor("#D6CDEA")) // Lilás claro - Under Repair
                4 -> setChipColor(statusChip, Color.parseColor("#6DBF5B")) // Verde - Resolved
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