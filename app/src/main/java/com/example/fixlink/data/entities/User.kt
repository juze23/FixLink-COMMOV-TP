package com.example.fixlink.data.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val user_id: String,
    val firstname: String,
    val lastname: String? = null,
    val email: String,
    @SerialName("telephone")
    val phoneNumber: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("type_id")
    val typeId: Int,
)

@Serializable
data class UserType(
    @SerialName("type_id")
    val id: String,
    val type: String
)