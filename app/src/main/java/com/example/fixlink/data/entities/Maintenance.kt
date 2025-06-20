package com.example.fixlink.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Maintenance(
    @SerialName("maintenance_id")
    val maintenance_id: String,
    @SerialName("id_user")
    val id_user: String,
    @SerialName("id_technician")
    val id_technician: String? = null,
    @SerialName("id_equipment")
    val id_equipment: Int,
    @SerialName("publication_date")
    val publicationDate: String,
    @SerialName("title")
    val title: String,
    @SerialName("state_id")
    val state_id: Int,
    val description: String? = null,
    val report: String? = null,
    @SerialName("beginning_date")
    val beginningDate: String? = null,
    @SerialName("ending_date")
    val endingDate: String? = null,
    @SerialName("localization_id")
    val localization_id: Int,
    @SerialName("priority_id")
    val priority_id: Int,
    @SerialName("type_id")
    val type_id: Int
)

@Serializable
data class State_maintenance(
    @SerialName("state_id")
    val state_id: Int,
    val state: String
)

@Serializable
data class Type_maintenance(
    @SerialName("type_id")
    val type_id: Int? = null,
    val type: String
)