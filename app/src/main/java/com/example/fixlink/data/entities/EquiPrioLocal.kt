package com.example.fixlink.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Equipment(
    @SerialName("equipment_id")
    val equipmentId: String,
    val name: String,
    val description: String? = null,
)

@Serializable
data class Priority(
    @SerialName("priority_id")
    val priorityId: String,
    val priority: String
)

@Serializable
data class Location(
    @SerialName("location_id")
    val locationId: String,
    val name: String
)