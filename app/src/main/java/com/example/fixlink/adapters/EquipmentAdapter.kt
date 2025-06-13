package com.example.fixlink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.activities.AdminActivity

class EquipmentAdapter(private val equipments: List<Equipment>) : RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder>() {

    inner class EquipmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val editIcon: ImageView = itemView.findViewById(R.id.editIcon)

        fun bind(equipment: Equipment) {
            nameTextView.text = equipment.name
            
            editIcon.setOnClickListener {
                val activity = itemView.context as? AdminActivity
                activity?.showEditFragment(equipment.name, "equipment")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_admin, parent, false)
        return EquipmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipmentViewHolder, position: Int) {
        val equipment = equipments[position]
        holder.bind(equipment)
    }

    override fun getItemCount() = equipments.size

}