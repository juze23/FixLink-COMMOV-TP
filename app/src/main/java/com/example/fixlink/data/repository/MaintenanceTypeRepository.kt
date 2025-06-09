package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Type_maintenance
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MaintenanceTypeRepository {
    suspend fun getMaintenanceTypes(): Result<List<Type_maintenance>> = withContext(Dispatchers.IO) {
        try {
            Log.d("MaintenanceTypeRepository", "Attempting to fetch maintenance types from Supabase")
            val response = SupabaseClient.supabase.postgrest["Type_maintenance"]
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
} 