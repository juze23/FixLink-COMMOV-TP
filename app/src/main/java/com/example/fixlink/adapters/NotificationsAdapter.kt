package com.example.fixlink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.fixlink.R
import com.example.fixlink.data.entities.Notification
import com.google.android.material.button.MaterialButton
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationsAdapter(
    private val notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.notificationDescription)
        val date: TextView = view.findViewById(R.id.notificationDate)
        val unreadIndicator: View = view.findViewById(R.id.unreadIndicator)
        val viewButton: MaterialButton = view.findViewById(R.id.viewButton)
        val cardView: CardView = view.findViewById(R.id.notificationCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        val context = holder.itemView.context
        
        // Translate the notification description
        val translatedDescription = when {
            // Issue status changes
            notification.description.contains("Issue status changed to") && notification.issue_id != null -> {
                val state = notification.description.substringAfter("to ").trim()
                val translatedState = when (state) {
                    "Under Repair" -> context.getString(R.string.text_state_under_repair)
                    "Resolved" -> context.getString(R.string.text_state_resolved)
                    "Assigned" -> context.getString(R.string.text_state_assigned)
                    "Pending" -> context.getString(R.string.text_state_pending)
                    "Cancelled" -> context.getString(R.string.text_state_cancelled)
                    else -> state
                }
                context.getString(R.string.text_notification_issue_status_changed, translatedState)
            }
            // Maintenance status changes
            notification.maintenance_id != null -> {
                val state = notification.description.substringAfter("to ").trim()
                val translatedState = when (state) {
                    "Ongoing" -> context.getString(R.string.text_state_ongoing)
                    "Completed" -> context.getString(R.string.text_state_completed)
                    "Assigned" -> context.getString(R.string.text_state_assigned)
                    "Pending" -> context.getString(R.string.text_state_pending)
                    "Cancelled" -> context.getString(R.string.text_state_cancelled)
                    else -> state
                }
                context.getString(R.string.text_notification_maintenance_status_changed, translatedState)
            }
            // Assignment notifications
            notification.description == "You have been assigned to an issue" ->
                context.getString(R.string.text_notification_issue_assigned)
            notification.description == "You have been assigned to a maintenance task" ->
                context.getString(R.string.text_notification_maintenance_assigned)
            // Creation notifications
            notification.description == "New issue has been created" ->
                context.getString(R.string.text_notification_issue_created)
            notification.description == "New maintenance task has been created" ->
                context.getString(R.string.text_notification_maintenance_created)
            // Keep original if no translation needed
            else -> notification.description
        }
        
        // Format the date
        val notificationDate = LocalDateTime.parse(notification.date, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()
        val formattedDate = when {
            // Today
            notificationDate.truncatedTo(ChronoUnit.DAYS) == now.truncatedTo(ChronoUnit.DAYS) ->
                context.getString(R.string.text_today) + " " +
                notificationDate.format(DateTimeFormatter.ofPattern("HH:mm"))
            // Yesterday
            notificationDate.truncatedTo(ChronoUnit.DAYS) == now.minusDays(1).truncatedTo(ChronoUnit.DAYS) ->
                context.getString(R.string.text_yesterday) + " " +
                notificationDate.format(DateTimeFormatter.ofPattern("HH:mm"))
            // Other days
            else -> notificationDate.format(DateTimeFormatter.ofPattern(context.getString(R.string.text_date_format)))
        }
        
        holder.description.text = translatedDescription
        holder.date.text = formattedDate
        
        // Update visual appearance based on read status
        if (notification.read) {
            holder.unreadIndicator.visibility = View.GONE
            holder.cardView.alpha = 0.7f
            holder.viewButton.visibility = View.GONE
        } else {
            holder.unreadIndicator.visibility = View.VISIBLE
            holder.cardView.alpha = 1.0f
            holder.viewButton.visibility = View.VISIBLE
        }
        
        // Set up view button click listener
        holder.viewButton.setOnClickListener {
            onNotificationClick(notification)
        }
        
        // Remove the click listener from the entire item view
        holder.itemView.setOnClickListener(null)
    }

    override fun getItemCount() = notifications.size
} 