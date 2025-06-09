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

    suspend fun getMaintenanceByTechnician(technicianId: String): Result<List<Maintenance>> = withContext(Dispatchers.IO) {
        try {
            val maintenance = SupabaseClient.supabase.postgrest["Maintenance"]
                .select {
                    filter {
                        eq("id_technician", technicianId)
                    }
                    order("publication_date", Order.DESCENDING)
                }
                .decodeList<Maintenance>()
            Result.success(maintenance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMaintenance(maintenance: Maintenance): Result<Maintenance> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.supabase.postgrest["Maintenance"]
                .update(maintenance) {
                    filter {
                        eq("maintenance_id", maintenance.maintenance_id)
                    }
                }
            Result.success(maintenance)
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Error updating maintenance: ", e)
            Result.failure(e)
        }
    }

    suspend fun assignTechnicianToMaintenance(maintenanceId: String, technicianId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current maintenance to update
            val currentMaintenance = getMaintenanceById(maintenanceId).getOrNull() ?: return@withContext Result.failure(Exception("Maintenance not found"))
            
            // Create updated maintenance with new technician and state
            val updatedMaintenance = currentMaintenance.copy(
                id_technician = technicianId,
                state_id = 2  // Set state to "assigned"
            )
            
            // Update the maintenance using the existing updateMaintenance method
            updateMaintenance(updatedMaintenance).map { Unit }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Error assigning technician: ", e)
            Result.failure(e)
        }
    }

    suspend fun updateMaintenanceReport(maintenanceId: String, report: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current maintenance to update
            val currentMaintenance = getMaintenanceById(maintenanceId).getOrNull() ?: return@withContext Result.failure(Exception("Maintenance not found"))
            
            // Create updated maintenance with new report and state_id = 4 (completed)
            val updatedMaintenance = currentMaintenance.copy(
                report = report,
                state_id = 4  // Set state to "completed"
            )
            
            // Update the maintenance using the existing updateMaintenance method
            updateMaintenance(updatedMaintenance).map { Unit }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Error updating maintenance report: ", e)
            Result.failure(e)
        }
    }

    suspend fun changeMaintenanceStatus(maintenanceId: String, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current maintenance to update
            val currentMaintenance = getMaintenanceById(maintenanceId).getOrNull() ?: return@withContext Result.failure(Exception("Maintenance not found"))
            
            // Get all states to find the correct state_id
            val statesResult = StateMaintenanceRepository().getMaintenanceStates()
            if (statesResult.isFailure) {
                return@withContext Result.failure(Exception("Failed to get maintenance states"))
            }
            
            val states = statesResult.getOrNull() ?: return@withContext Result.failure(Exception("No maintenance states found"))
            val newState = states.find { it.state.lowercase() == newStatus.lowercase() }
            
            if (newState == null) {
                return@withContext Result.failure(Exception("Invalid state: $newStatus"))
            }
            
            // Create updated maintenance with new state
            val updatedMaintenance = currentMaintenance.copy(state_id = newState.state_id)
            
            // Update the maintenance using the existing updateMaintenance method
            updateMaintenance(updatedMaintenance).map { Unit }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Error changing maintenance status: ", e)
            Result.failure(e)
        }
    }
}