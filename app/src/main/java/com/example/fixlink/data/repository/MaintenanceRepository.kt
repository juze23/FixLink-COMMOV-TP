package com.example.fixlink.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.fixlink.data.entities.Maintenance
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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

    suspend fun assignTechnicianToMaintenance(
        maintenanceId: String, 
        technicianId: String,
        notificationText: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current maintenance to update
            val currentMaintenance = getMaintenanceById(maintenanceId).getOrNull() ?: return@withContext Result.failure(Exception("Maintenance not found"))
            
            // Create updated maintenance with new technician and state
            val updatedMaintenance = currentMaintenance.copy(
                id_technician = technicianId,
                state_id = 2  // Set state to "assigned"
            )
            
            // Update the maintenance using the existing updateMaintenance method
            val updateResult = updateMaintenance(updatedMaintenance)
            
            if (updateResult.isSuccess) {
                // Create notification for the assigned technician
                NotificationRepository().createNotification(
                    userId = technicianId,
                    maintenanceId = maintenanceId,
                    description = notificationText
                )
            }
            
            updateResult.map { Unit }
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

    suspend fun changeMaintenanceStatus(
        maintenanceId: String, 
        newStatus: String,
        notificationText: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
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
            val updateResult = updateMaintenance(updatedMaintenance)
            
            if (updateResult.isSuccess) {
                // Create notification for the maintenance creator
                NotificationRepository().createNotification(
                    userId = currentMaintenance.id_user,
                    maintenanceId = maintenanceId,
                    description = notificationText
                )
            }
            
            updateResult.map { Unit }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Error changing maintenance status: ", e)
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createMaintenance(
        userId: String,
        equipmentId: Int,
        title: String,
        description: String,
        locationId: Int,
        priorityId: Int,
        typeId: Int,
        imageUri: Uri?,
        context: Context
    ): Result<Maintenance> = withContext(Dispatchers.IO) {
        try {
            // Generate maintenance ID first
            val maintenanceId = UUID.randomUUID().toString()
            
            // Upload image if provided
            if (imageUri != null) {
                uploadImage(imageUri, context, maintenanceId)
            }
            
            // Get current timestamp
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            
            // Create maintenance object
            val maintenance = Maintenance(
                maintenance_id = maintenanceId,
                id_user = userId,
                id_technician = null,
                id_equipment = equipmentId,
                publicationDate = currentTime,
                state_id = 1,
                title = title,
                description = description,
                report = null,
                beginningDate = null,
                endingDate = null,
                localization_id = locationId,
                priority_id = priorityId,
                type_id = typeId
            )

            try {
                SupabaseClient.supabase.postgrest["Maintenance"]
                    .insert(maintenance)
                Result.success(maintenance)
            } catch (e: Exception) {
                Log.e("MaintenanceRepository", "Error inserting maintenance: ${e.message}", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Error in createMaintenance: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun uploadImage(imageUri: Uri, context: Context, maintenanceId: String): String? {
        return try {
            // Use maintenance ID as filename
            val fileName = "maintenance_${maintenanceId}.jpg"
            
            // Get the image bytes from the URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("MaintenanceRepository", "Failed to open input stream for image URI: $imageUri")
                return null
            }
            
            val imageBytes = inputStream.readBytes()
            inputStream.close()
            
            if (imageBytes.isEmpty()) {
                Log.e("MaintenanceRepository", "Image bytes are empty")
                return null
            }
            
            try {
                // Upload to Supabase Storage in the 'Maintenances' folder
                SupabaseClient.supabase.storage.from("fixlink")
                    .upload("Maintenances/$fileName", imageBytes)
                
                // Return the public URL of the uploaded image
                SupabaseClient.supabase.storage.from("fixlink")
                    .publicUrl("Maintenances/$fileName")
            } catch (e: Exception) {
                Log.e("MaintenanceRepository", "Error during Supabase upload: ${e.message}", e)
                null
            }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Error in uploadImage: ${e.message}", e)
            null
        }
    }
}