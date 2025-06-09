package com.example.fixlink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.data.entities.Issue
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.Issue_state
import android.graphics.drawable.GradientDrawable
import android.graphics.Color
import com.example.fixlink.data.entities.User

class IssueAdapter(private val issues: List<Issue>) : RecyclerView.Adapter<IssueAdapter.IssueViewHolder>() {
    private var priorities: List<Priority> = emptyList()
    private var equipments: List<Equipment> = emptyList()
    private var locations: List<Location> = emptyList()
    private var states: List<Issue_state> = emptyList()
    private var users: List<User> = emptyList()

    fun setAuxiliaryData(
        priorities: List<Priority>,
        equipments: List<Equipment>,
        locations: List<Location>,
        states: List<Issue_state>,
        users: List<User>
    ) {
        this.priorities = priorities
        this.equipments = equipments
        this.locations = locations
        this.states = states
        this.users = users
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.bind(issue, priorities, equipments, locations, states, users)
        
        // Add click listener to navigate to issue details
        holder.itemView.setOnClickListener {
            val fragment = IssueDetailFragment.newInstance(issue.issue_id)
            val activity = holder.itemView.context as? androidx.fragment.app.FragmentActivity
            
            // Get the parent fragment to determine which container to use
            val parentFragment = activity?.supportFragmentManager?.fragments?.firstOrNull { 
                it is IssuesContentFragment || it is MyTasksFragment 
            }
            
            val containerId = when (parentFragment) {
                is IssuesContentFragment -> R.id.issuesContentFragmentContainer
                is MyTasksFragment -> R.id.myTasksContentFragmentContainer
                else -> return@setOnClickListener
            }
            
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(containerId, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    override fun getItemCount(): Int = issues.size

    class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
            titleTextView.text = issue.title ?: "(Sem título)"
            val userName = users.find { it.user_id == issue.id_user }?.name ?: issue.id_user
            reporterTextView.text = "Utilizador: $userName"
            val priorityText = priorities.find { it.priority_id == issue.priority_id }?.priority ?: issue.priority_id.toString()
            val statusText = states.find { it.state_id == issue.state_id }?.state ?: issue.state_id.toString()
            val equipment = equipments.find { it.equipment_id == issue.id_equipment }
            val equipmentText = equipment?.name ?: issue.id_equipment.toString()
            val equipmentState = if (equipment != null) if (equipment.active) "Active" else "Inactive" else "?"
            val locationText = locations.find { it.location_id == issue.localization_id }?.name ?: issue.localization_id.toString()

            priorityChip.text = priorityText
            statusChip.text = statusText
            equipmentChip.text = equipmentState
            locationTextView.text = locationText

            // Cores para prioridade
            when (priorityText.lowercase()) {
                "high", "alta" -> setChipColor(priorityChip, Color.parseColor("#FF5252")) // Vermelho
                "medium", "média", "media" -> setChipColor(priorityChip, Color.parseColor("#FFEB3B")) // Amarelo
                "low", "baixa" -> setChipColor(priorityChip, Color.parseColor("#B2DFDB")) // Verde claro
                else -> setChipColor(priorityChip, Color.LTGRAY)
            }
            // Cores para status (exatamente como na imagem)
            when (statusText.lowercase()) {
                "pending", "pendente" -> setChipColor(statusChip, Color.parseColor("#E0E0E0")) // Cinza claro
                "assigned", "atribuído", "atribuido" -> setChipColor(statusChip, Color.parseColor("#B3E5FC")) // Azul claro
                "under repair", "em andamento", "em reparacao" -> setChipColor(statusChip, Color.parseColor("#D6CDEA")) // Lilás claro
                "resolved", "completo" -> setChipColor(statusChip, Color.parseColor("#66BB6A")) // Verde
                else -> setChipColor(statusChip, Color.LTGRAY)
            }
            // Cores para equipamento (ativo/inativo)
            when (equipmentState.lowercase()) {
                "active", "ativo" -> setChipColor(equipmentChip, Color.parseColor("#FFC107")) // Amarelo
                "inactive", "inativo" -> setChipColor(equipmentChip, Color.parseColor("#00BCD4")) // Azul
                else -> setChipColor(equipmentChip, Color.LTGRAY)
            }
        }
    }
} 