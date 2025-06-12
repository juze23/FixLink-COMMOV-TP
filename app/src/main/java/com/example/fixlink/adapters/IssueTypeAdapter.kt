package com.example.fixlink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.data.entities.Issue_type

class IssueTypeAdapter(
    private var issueTypes: List<Issue_type>,
    private val onEditClick: (Issue_type) -> Unit,
    private val onDeleteClick: (Issue_type) -> Unit
) : RecyclerView.Adapter<IssueTypeAdapter.IssueTypeViewHolder>() {

    fun updateIssueTypes(newIssueTypes: List<Issue_type>) {
        issueTypes = newIssueTypes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_admin, parent, false)
        return IssueTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueTypeViewHolder, position: Int) {
        holder.bind(issueTypes[position])
    }

    override fun getItemCount() = issueTypes.size

    inner class IssueTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.itemNameTextView)
        private val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)

        fun bind(issueType: Issue_type) {
            nameTextView.text = issueType.type

            editIcon.setOnClickListener { onEditClick(issueType) }
            deleteIcon.setOnClickListener { onDeleteClick(issueType) }
        }
    }
} 