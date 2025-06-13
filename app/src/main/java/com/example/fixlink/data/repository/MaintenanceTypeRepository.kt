package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Type_maintenance
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MaintenanceTypeRepository {
    private val TAG = "MaintenanceTypeRepository"
    private val supabase = SupabaseClient.supabase

    suspend fun getMaintenanceTypes(): Result<List<Type_maintenance>> = withContext(Dispatchers.IO) {
        try {
            Log.d("MaintenanceTypeRepository", "Attempting to fetch maintenance types from Supabase")
            val response = supabase.postgrest["Maintenance_type"]
                .select()
            Log.d("MaintenanceTypeRepository", "Supabase query executed, attempting to decode response")
            val types = response.decodeList<Type_maintenance>()
            Log.d("MaintenanceTypeRepository", "Successfully decoded maintenance types: ${types.size} items")
            Log.d("MaintenanceTypeRepository", "Maintenance types: ${types.map { it.type }}")
            Result.success(types)
        } catch (e: Exception) {
            Log.e("MaintenanceTypeRepository", "Error fetching maintenance types: ${e.message}", e)
            Log.e("MaintenanceTypeRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    suspend fun addMaintenanceType(typeName: String): Result<Type_maintenance> = withContext(Dispatchers.IO) {
        try {
            Log.d("MaintenanceTypeRepository", "Attempting to add new maintenance type: $typeName")
            val newType = Type_maintenance(
                type = typeName
            )
            
            // Insert into database
            supabase.postgrest["Maintenance_type"]
                .insert(newType)
            
            // Get the created type by name
            val response = supabase.postgrest["Maintenance_type"]
                .select {
                    filter {
                        eq("type", typeName)
                    }
                }
                .decodeSingle<Type_maintenance>()
            
            Log.d("MaintenanceTypeRepository", "Successfully added new maintenance type: ${response.type}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("MaintenanceTypeRepository", "Error adding maintenance type: ${e.message}", e)
            Log.e("MaintenanceTypeRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    suspend fun updateType(typeId: Int, newName: String): Result<Type_maintenance> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating maintenance type: id=$typeId, newName=$newName")
            
            // Update the type in the database
            supabase.postgrest["Maintenance_type"]
                .update({
                    set("type", newName)
                }) {
                    filter {
                        eq("type_id", typeId)
                    }
                }
            
            // Get the updated type
            val result = supabase.postgrest["Maintenance_type"]
                .select {
                    filter {
                        eq("type_id", typeId)
                    }
                }
                .decodeSingle<Type_maintenance>()

            Log.d(TAG, "Successfully updated maintenance type: $result")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating maintenance type", e)
            Result.failure(e)
        }
    }
} 