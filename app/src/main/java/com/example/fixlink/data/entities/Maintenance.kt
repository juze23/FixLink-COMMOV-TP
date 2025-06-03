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
    val id_equipment: String,
    @SerialName("publication_date")
    val publicationDate: String,
    @SerialName("state_id")
    val state_id: String,
    val description: String? = null,
    val report: String? = null,
    @SerialName("beginning_date")
    val beginningDate: String? = null,
    @SerialName("ending_date")
    val endingDate: String? = null,
    @SerialName("localization_id")
    val localization_id: String,
    @SerialName("priority_id")
    val priority_id: String,
    @SerialName("type_id")
    val type_id: String
)

@Serializable
data class StateMaintenance(
    @SerialName("state_id")
    val state_id: String? = null,
    val state: String
)

@Serializable
data class MaintenanceType(
    @SerialName("type_id")
    val type_id: String,
        val type: String
)