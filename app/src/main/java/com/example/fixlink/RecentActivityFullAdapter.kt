package com.example.fixlink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.data.entities.*
import java.text.SimpleDateFormat
import java.util.*

class RecentActivityFullAdapter(
    private val issues: List<Issue>,
    private val users: List<User>,
    private val equipments: List<Equipment>,
    private val locations: List<Location>
) : RecyclerView.Adapter<RecentActivityFullAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.activityTitleText)
        val dateText: TextView = view.findViewById(R.id.activityDateText)
        val creatorText: TextView = view.findViewById(R.id.activityCreatorText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity_full, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val issue = issues[position]
        
        // Set title
        holder.titleText.text = issue.title

        // Set date with full format
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(issue.publicationDate)
        holder.dateText.text = date?.let { outputFormat.format(it) }

        // Set creator
        val creator = users.find { it.user_id == issue.id_user }
        holder.creatorText.text = creator?.let { user ->
            if (user.lastname.isNullOrEmpty()) user.firstname else "${user.firstname} ${user.lastname}"
        } ?: "Unknown User"
    }

    override fun getItemCount() = issues.size
} 