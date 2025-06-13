package com.example.fixlink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.data.entities.Type_maintenance
import com.example.fixlink.data.entities.Issue_type
import com.example.fixlink.activities.AdminActivity

abstract class BaseTypeAdapter<T>(
    protected val types: List<T>,
    protected val typeNameExtractor: (T) -> String
) : RecyclerView.Adapter<BaseTypeAdapter.TypeViewHolder<T>>() {

    class TypeViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TypeViewHolder<T> {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_admin, parent, false)
        return TypeViewHolder(view)
    }

    override fun getItemCount() = types.size

    abstract override fun onBindViewHolder(holder: TypeViewHolder<T>, position: Int)
}

class MaintenanceTypeAdapter(
    types: List<Type_maintenance>
) : BaseTypeAdapter<Type_maintenance>(types, { it.type }) {

    override fun onBindViewHolder(holder: TypeViewHolder<Type_maintenance>, position: Int) {
        val type = types[position]
        holder.nameTextView.text = type.type
        
        holder.editIcon.setOnClickListener {
            val activity = holder.itemView.context as? AdminActivity
            activity?.showEditFragment(type.type, "maintenance_type", type.type_id)
        }
    }
}

class IssueTypeAdapter(
    types: List<Issue_type>
) : BaseTypeAdapter<Issue_type>(types, { it.type }) {

    override fun onBindViewHolder(holder: TypeViewHolder<Issue_type>, position: Int) {
        val type = types[position]
        holder.nameTextView.text = type.type
        
        holder.editIcon.setOnClickListener {
            val activity = holder.itemView.context as? AdminActivity
            activity?.showEditFragment(type.type, "issue_type", type.type_id)
        }
    }
} 