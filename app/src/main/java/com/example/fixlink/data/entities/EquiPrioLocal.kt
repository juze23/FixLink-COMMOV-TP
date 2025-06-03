package com.example.fixlink.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Equipment(
    @SerialName("equipment_id")
    val equipment_id: Int,
    val name: String,
    val description: String? = null,
    val active: Boolean
)

@Serializable
data class Priority(
    @SerialName("priority_id")
    val priority_id: Int,
    val priority: String
)

@Serializable
data class Location(
    @SerialName("location_id")
    val location_id: Int,
    val name: String
)