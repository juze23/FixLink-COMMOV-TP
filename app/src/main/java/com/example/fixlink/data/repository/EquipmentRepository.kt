package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.supabaseConfig.SupabaseClient as SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Returning
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class EquipmentRepository {
    suspend fun getEquipmentList(): Result<List<Equipment>> = withContext(Dispatchers.IO) {
        try {
            Log.d("EquipmentRepository", "Attempting to fetch equipment list from Supabase")
            val response = SupabaseClient.supabase.postgrest["Equipment"]
                .select {
                    filter {
                        eq("active", true)
                    }
                }
            Log.d("EquipmentRepository", "Supabase query executed, attempting to decode response")
            val equipment = response.decodeList<Equipment>()
            Log.d("EquipmentRepository", "Successfully decoded equipment list: ${equipment.size} items")
            Log.d("EquipmentRepository", "Equipment names: ${equipment.map { it.name }}")
            Result.success(equipment)
        } catch (e: Exception) {
            Log.e("EquipmentRepository", "Error fetching equipment list: ${e.message}", e)
            Log.e("EquipmentRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    suspend fun registerEquipment(name: String, description: String? = null): Result<Equipment> = withContext(Dispatchers.IO) {
        try {
            Log.d("EquipmentRepository", "Attempting to register new equipment: $name")

            val equipment = Equipment(
                name = name,
                description = description,
                active = true
            )

            // Insert into database
            SupabaseClient.supabase.postgrest["Equipment"]
                .insert(listOf(equipment))

            // Get the created equipment by name
            val response = SupabaseClient.supabase.postgrest["Equipment"]
                .select {
                    filter {
                        eq("name", name)
                    }
                }
                .decodeSingle<Equipment>()

            Log.d("EquipmentRepository", "Successfully registered equipment: ${response.name}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("EquipmentRepository", "Error registering equipment: ${e.message}", e)
            Log.e("EquipmentRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    suspend fun getEquipmentByName(name: String): Equipment? = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseClient.supabase.postgrest["Equipment"]
                .select {
                    filter {
                        eq("name", name)
                    }
                }
                .decodeSingle<Equipment>()
            response
        } catch (e: Exception) {
            Log.e("EquipmentRepository", "Error getting equipment by name: ${e.message}", e)
            null
        }
    }

    suspend fun updateEquipment(equipment: Equipment): Result<Equipment> = withContext(Dispatchers.IO) {
        try {
            Log.d("EquipmentRepository", "Attempting to update equipment: ${equipment.name}")

            val equipmentId = equipment.equipment_id ?: throw IllegalArgumentException("Cannot update equipment without an ID")

            // Update in database
            SupabaseClient.supabase.postgrest["Equipment"]
                .update({
                    set("name", equipment.name)
                    set("description", equipment.description)
                    set("active", equipment.active)
                }) {
                    filter {
                        eq("equipment_id", equipmentId)
                    }
                }

            // Get the updated equipment
            val response = SupabaseClient.supabase.postgrest["Equipment"]
                .select {
                    filter {
                        eq("equipment_id", equipmentId)
                    }
                }
                .decodeSingle<Equipment>()

            Log.d("EquipmentRepository", "Successfully updated equipment: ${response.name}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("EquipmentRepository", "Error updating equipment: ${e.message}", e)
            Log.e("EquipmentRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
}