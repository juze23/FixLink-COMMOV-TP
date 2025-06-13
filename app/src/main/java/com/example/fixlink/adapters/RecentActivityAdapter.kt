package com.example.fixlink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.data.entities.Issue
import com.example.fixlink.data.entities.User
import java.text.SimpleDateFormat
import java.util.*

class RecentActivityAdapter(
    private val issues: List<Issue>,
    private val users: List<User>
) : RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.activityTitle)
        val date: TextView = view.findViewById(R.id.activityDate)
        val creator: TextView = view.findViewById(R.id.activityCreator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_activity, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val issue = issues[position]
        holder.title.text = issue.title

        // Format date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = try {
            val parsedDate = inputFormat.parse(issue.publicationDate)
            parsedDate?.let { outputFormat.format(it) } ?: issue.publicationDate
        } catch (e: Exception) {
            issue.publicationDate
        }
        holder.date.text = date

        // Get creator name
        val creator = users.find { it.user_id == issue.id_user }
        holder.creator.text = "${holder.itemView.context.getString(R.string.text_user_label)} ${
            creator?.let {
                if (it.lastname.isNullOrEmpty()) it.firstname else "${it.firstname} ${it.lastname}"
            } ?: "Unknown User"
        }"
    }

    override fun getItemCount() = issues.size
} 