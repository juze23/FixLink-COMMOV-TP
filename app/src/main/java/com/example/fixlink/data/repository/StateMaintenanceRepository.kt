package com.example.fixlink.data.repository

import com.example.fixlink.data.entities.State_maintenance
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StateMaintenanceRepository {
    suspend fun getMaintenanceStates(): Result<List<State_maintenance>> = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseClient.supabase.postgrest["Maintenance_state"]
                .select()
            val states = response.decodeList<State_maintenance>()
            Result.success(states)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}