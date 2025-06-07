package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Maintenance
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MaintenanceRepository {
    suspend fun getMaintenanceById(maintenanceId: String): Result<Maintenance> = withContext(Dispatchers.IO) {
        try {
            val maintenance = SupabaseClient.supabase.postgrest["Maintenance"]
                .select {
                    filter {
                        eq("maintenance_id", maintenanceId)
                    }
                }
                .decodeSingle<Maintenance>()
            
            Result.success(maintenance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllMaintenance(): Result<List<Maintenance>> = withContext(Dispatchers.IO) {
        try {
            val maintenance = SupabaseClient.supabase.postgrest["Maintenance"]
                .select {
                    order("publication_date", Order.DESCENDING)
                }
                .decodeList<Maintenance>()
            Result.success(maintenance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMaintenanceByUser(userId: String): Result<List<Maintenance>> = withContext(Dispatchers.IO) {
        try {
            val maintenance = SupabaseClient.supabase.postgrest["Maintenance"]
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                    order("publication_date", Order.DESCENDING)
                }
                .decodeList<Maintenance>()
            
            Result.success(maintenance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}