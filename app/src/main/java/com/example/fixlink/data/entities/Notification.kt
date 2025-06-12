package com.example.fixlink.data.entities


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    @SerialName("notification_id")
    val notification_id: String,
    @SerialName("id_user")
    val id_user: String,
    @SerialName("issue_id")
    val issue_id: String? = null,
    @SerialName("maintenance_id")
    val maintenance_id: String? = null,
    @SerialName("description")
    val description: String,
    @SerialName("date")
    val date: String,
    @SerialName("read")
    val read: Boolean
)