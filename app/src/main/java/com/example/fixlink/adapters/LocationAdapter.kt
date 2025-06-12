package com.example.fixlink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.data.entities.Location

class LocationAdapter(
    private var locations: List<Location>,
    private val onEditClick: (Location) -> Unit,
    private val onDeleteClick: (Location) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    fun updateLocations(newLocations: List<Location>) {
        locations = newLocations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_admin, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.bind(location)
    }

    override fun getItemCount() = locations.size

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)

        fun bind(location: Location) {
            nameTextView.text = location.name

            editIcon.setOnClickListener { onEditClick(location) }
            deleteIcon.setOnClickListener { onDeleteClick(location) }
        }
    }
} 