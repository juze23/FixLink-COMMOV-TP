package com.example.fixlink.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.data.entities.User
import com.example.fixlink.databinding.ListItemTechnicianBinding

class TechnicianAdapter(
    private val onTechnicianClick: (User) -> Unit
) : ListAdapter<User, TechnicianAdapter.TechnicianViewHolder>(TechnicianDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TechnicianViewHolder {
        val binding = ListItemTechnicianBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TechnicianViewHolder(binding, onTechnicianClick)
    }

    override fun onBindViewHolder(holder: TechnicianViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TechnicianViewHolder(
        private val binding: ListItemTechnicianBinding,
        private val onTechnicianClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(technician: User) {
            binding.apply {
                technicianNameTextView.text = if (technician.lastname.isNullOrEmpty()) technician.firstname else "${technician.firstname} ${technician.lastname}"
                root.setOnClickListener { onTechnicianClick(technician) }
            }
        }
    }

    private class TechnicianDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.user_id == newItem.user_id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
} 