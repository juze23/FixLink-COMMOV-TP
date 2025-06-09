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
            creatorTextView.text = "Creator: $creatorName"

            val priorityText = priorities.find { it.priority_id == maintenance.priority_id }?.priority ?: maintenance.priority_id.toString()
            val statusText = states.find { it.state_id == maintenance.state_id }?.state ?: maintenance.state_id.toString()
            val equipment = equipments.find { it.equipment_id == maintenance.id_equipment }
            val equipmentText = equipment?.name ?: maintenance.id_equipment
            val equipmentState = if (equipment != null) if (equipment.active) "Active" else "Inactive" else "?"
            val locationText = locations.find { it.location_id == maintenance.localization_id }?.name ?: maintenance.localization_id.toString()

            priorityChip.text = priorityText
            statusChip.text = statusText
            equipmentChip.text = equipmentState
            locationTextView.text = locationText

            // Set colors based on priority
            when (priorityText.lowercase()) {
                "high" -> setChipColor(priorityChip, Color.parseColor("#FF5252"))
                "medium" -> setChipColor(priorityChip, Color.parseColor("#FFEB3B"))
                "low" -> setChipColor(priorityChip, Color.parseColor("#B2DFDB"))
                else -> setChipColor(priorityChip, Color.GRAY)
            }

            // Set colors based on status
            when (statusText.lowercase()) {
                "pending", "pendente" -> setChipColor(statusChip, Color.parseColor("#D3D3D3")) // Cinza claro
                "assigned", "atribuído", "atribuido" -> setChipColor(statusChip, Color.parseColor("#ADD8E6")) // Azul claro
                "ongoing", "em reparação" -> setChipColor(statusChip, Color.parseColor("#D6CDEA")) // Lilás claro
                "completed", "terminada" -> setChipColor(statusChip, Color.parseColor("#6DBF5B")) // Verde
                else -> setChipColor(statusChip, Color.LTGRAY)
            }

            when (equipmentState.lowercase()) {
                "active", "ativo" -> setChipColor(equipmentChip, Color.parseColor("#FFC107")) // Amarelo
                "inactive", "inativo" -> setChipColor(equipmentChip, Color.parseColor("#00BCD4")) // Azul
                else -> setChipColor(equipmentChip, Color.LTGRAY)
            }
        }
    }
}