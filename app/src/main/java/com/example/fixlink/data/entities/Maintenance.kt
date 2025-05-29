package com.example.fixlink.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Maintenance(
    @SerialName("maintenance_id")
    val id: String,
    @SerialName("id_user")
    val idUser: String,
    @SerialName("id_technician")
    val idTechnician: String? = null,
    @SerialName("id_equipment")
    val idEquipment: String,
    @SerialName("publication_date")
    val publicationDate: String,
    @SerialName("state_id")
    val stateId: String,
    val description: String? = null,
    val report: String? = null,
    @SerialName("beginning_date")
    val beginningDate: String? = null,
    @SerialName("ending_date")
    val endingDate: String? = null,
    @SerialName("localization_id")
    val localizationId: String,
    @SerialName("priority_id")
    val priorityId: String,
    @SerialName("type_id")
    val typeId: String
)

@Serializable
data class StateMaintenance(
    @SerialName("state_id")
    val stateId: String? = null,
    val state: String
)

@Serializable
data class MaintenanceType(
    @SerialName("type_id")
    val typeId: String,
    val type: String
)