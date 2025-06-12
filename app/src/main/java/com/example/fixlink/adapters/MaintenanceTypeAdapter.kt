package com.example.fixlink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.data.entities.Type_maintenance

class MaintenanceTypeAdapter(
    private var maintenanceTypes: List<Type_maintenance>,
    private val onEditClick: (Type_maintenance) -> Unit,
    private val onDeleteClick: (Type_maintenance) -> Unit
) : RecyclerView.Adapter<MaintenanceTypeAdapter.MaintenanceTypeViewHolder>() {

    fun updateMaintenanceTypes(newMaintenanceTypes: List<Type_maintenance>) {
        maintenanceTypes = newMaintenanceTypes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_admin, parent, false)
        return MaintenanceTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaintenanceTypeViewHolder, position: Int) {
        holder.bind(maintenanceTypes[position])
    }

    override fun getItemCount() = maintenanceTypes.size

    inner class MaintenanceTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)

        fun bind(maintenanceType: Type_maintenance) {
            nameTextView.text = maintenanceType.type

            editIcon.setOnClickListener { onEditClick(maintenanceType) }
            deleteIcon.setOnClickListener { onDeleteClick(maintenanceType) }
        }
    }
} 