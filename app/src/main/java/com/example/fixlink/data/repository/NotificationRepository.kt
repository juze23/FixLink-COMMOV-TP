package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Notification
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class NotificationRepository {
    suspend fun createNotification(
        userId: String,
        issueId: String? = null,
        maintenanceId: String? = null,
        description: String
    ): Result<Notification> = withContext(Dispatchers.IO) {
        try {
            val notification = Notification(
                notification_id = UUID.randomUUID().toString(),
                id_user = userId,
                issue_id = issueId,
                maintenance_id = maintenanceId,
                description = description,
                date = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                read = false
            )

            try {
                SupabaseClient.supabase.postgrest["Notification"]
                    .insert(notification)
                Result.success(notification)
            } catch (e: Exception) {
                Log.e("NotificationRepository", "Error inserting notification: ${e.message}", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error in createNotification: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUnreadNotificationsCount(userId: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val notifications = SupabaseClient.supabase.postgrest["Notification"]
                .select {
                    filter {
                        eq("id_user", userId)
                        eq("read", false)
                    }
                }
                .decodeList<Notification>()

            Result.success(notifications.size)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error getting unread notifications count: ", e)
            Result.failure(e)
        }
    }

    suspend fun getNotifications(userId: String): Result<List<Notification>> = withContext(Dispatchers.IO) {
        try {
            val notifications = SupabaseClient.supabase.postgrest["Notification"]
                .select {
                    filter {
                        eq("id_user", userId)
                    }
                    order("date", Order.DESCENDING)
                }
                .decodeList<Notification>()

            Result.success(notifications)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error getting notifications: ", e)
            Result.failure(e)
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.supabase.postgrest["Notification"]
                .update({
                    set("read", true)
                }) {
                    filter {
                        eq("notification_id", notificationId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking notification as read: ", e)
            Result.failure(e)
        }
    }
} 