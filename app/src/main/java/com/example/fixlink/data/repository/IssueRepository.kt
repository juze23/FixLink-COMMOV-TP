package com.example.fixlink.data.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.fixlink.data.entities.Issue
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.data.entities.Location
import com.example.fixlink.data.entities.Issue_type
import com.example.fixlink.data.entities.Issue_state
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.client.utils.EmptyContent.headers
import kotlinx.atomicfu.TraceBase.None.append
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IssueRepository {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createIssue(
        userId: String,
        equipmentId: Int,
        title: String,
        description: String,
        locationId: Int,
        priorityId: Int,
        typeId: Int,
        imageUri: Uri?,
        context: Context
    ): Result<Issue> = withContext(Dispatchers.IO) {
        try {
            // Generate issue ID first
            val issueId = UUID.randomUUID().toString()
            
            // Upload image if provided
            if (imageUri != null) {
                uploadImage(imageUri, context, issueId)
            }
            
            // Get current timestamp
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            
            // Create issue object
            val issue = Issue(
                issue_id = issueId,
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
                createdAt = currentTime,
                type_id = typeId
            )
            
            try {
                SupabaseClient.supabase.postgrest["Issue"]
                    .insert(issue)
                Result.success(issue)
            } catch (e: Exception) {
                Log.e("IssueRepository", "Error inserting issue: ${e.message}", e)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("IssueRepository", "Error in createIssue: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    private suspend fun uploadImage(imageUri: Uri, context: Context, issueId: String): String? {
        return try {
            // Use issue ID as filename
            val fileName = "issue_${issueId}.jpg"
            
            // Get the image bytes from the URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("IssueRepository", "Failed to open input stream for image URI: $imageUri")
                return null
            }
            
            val imageBytes = inputStream.readBytes()
            inputStream.close()
            
            if (imageBytes.isEmpty()) {
                Log.e("IssueRepository", "Image bytes are empty")
                return null
            }
            
            try {
                // Upload to Supabase Storage in the 'Issues' folder
                SupabaseClient.supabase.storage.from("fixlink")
                    .upload("Issues/$fileName", imageBytes)
                
                // Return the public URL of the uploaded image
                SupabaseClient.supabase.storage.from("fixlink")
                    .publicUrl("Issues/$fileName")
            } catch (e: Exception) {
                Log.e("IssueRepository", "Error during Supabase upload: ${e.message}", e)
                null
            }
        } catch (e: Exception) {
            Log.e("IssueRepository", "Error in uploadImage: ${e.message}", e)
            null
        }
    }
    
    suspend fun getIssuesByUser(userId: String): Result<List<Issue>> = withContext(Dispatchers.IO) {
        try {
            val issues = SupabaseClient.supabase.postgrest["Issue"]
                .select {
                    filter {
                        eq("id_user", userId)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Issue>()
            
            Result.success(issues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getIssueById(issueId: String): Result<Issue> = withContext(Dispatchers.IO) {
        try {
            val issue = SupabaseClient.supabase.postgrest["Issue"]
                .select {
                    filter {
                        eq("issue_id", issueId)
                    }
                }
                .decodeSingle<Issue>()
            
            Result.success(issue)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllIssues(): Result<List<Issue>> = withContext(Dispatchers.IO) {
        try {
            val issues = SupabaseClient.supabase.postgrest["Issue"]
                .select {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Issue>()
            Result.success(issues)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getIssuesByTechnician(technicianId: String): Result<List<Issue>> = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseClient.supabase.postgrest["Issue"]
                .select {
                    filter {
                        eq("id_technician", technicianId)
                    }
                    order("created_at", Order.DESCENDING)
                }
            val issues = response.decodeList<Issue>()
            // Retorna uma lista vazia como sucesso, não como erro
            Result.success(issues)
        } catch (e: Exception) {
            // Só retorna erro se houver uma exceção real
            if (e.message?.contains("empty list") == true) {
                Result.success(emptyList())
            } else {
                Result.failure(e)
            }
        }
    }
    
    suspend fun updateIssue(issue: Issue): Result<Issue> = withContext(Dispatchers.IO) {
        try {
            SupabaseClient.supabase.postgrest["Issue"]
                .update(issue) {
                    filter {
                        eq("issue_id", issue.issue_id)
                    }
                }
            Result.success(issue)
        } catch (e: Exception) {
            Log.e("IssueRepository", "Error updating issue: ", e)
            Result.failure(e)
        }
    }
    
    suspend fun assignTechnicianToIssue(
        issueId: String, 
        technicianId: String,
        notificationText: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current issue to update
            val currentIssue = getIssueById(issueId).getOrNull() ?: return@withContext Result.failure(Exception("Issue not found"))
            
            // Create updated issue with new technician and state
            val updatedIssue = currentIssue.copy(
                id_technician = technicianId,
                state_id = 2  // Set state to "assigned"
            )
            
            // Update the issue using the existing updateIssue method
            val updateResult = updateIssue(updatedIssue)
            
            if (updateResult.isSuccess) {
                // Create notification for the assigned technician
                NotificationRepository().createNotification(
                    userId = technicianId,
                    issueId = issueId,
                    description = notificationText
                )
            }
            
            updateResult.map { Unit }
        } catch (e: Exception) {
            Log.e("IssueRepository", "Error assigning technician: ", e)
            Result.failure(e)
        }
    }

    suspend fun updateIssueReport(issueId: String, report: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current issue to update
            val currentIssue = getIssueById(issueId).getOrNull() ?: return@withContext Result.failure(Exception("Issue not found"))
            
            // Create updated issue with new report, state_id = 4 and ending date
            val updatedIssue = currentIssue.copy(
                report = report,
                state_id = 4,  // Set state to "Resolved"
                endingDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            )
            
            // Update the issue using the existing updateIssue method
            updateIssue(updatedIssue).map { Unit }
        } catch (e: Exception) {
            Log.e("IssueRepository", "Error updating issue report: ", e)
            Result.failure(e)
        }
    }

    suspend fun changeIssueStatus(
        issueId: String, 
        newStatus: String,
        notificationText: String
    ) = withContext(Dispatchers.IO) {
        try {
            // First get the current issue to update
            val currentIssue = getIssueById(issueId).getOrNull() ?: return@withContext Result.failure(Exception("Issue not found"))
            
            // Get all states to find the correct state_id
            val statesResult = StateIssueRepository().getIssueStates()
            if (statesResult.isFailure) {
                return@withContext Result.failure(Exception("Failed to get issue states"))
            }
            
            val states = statesResult.getOrNull() ?: return@withContext Result.failure(Exception("No issue states found"))
            val newState = states.find { it.state.lowercase() == newStatus.lowercase() }
            
            if (newState == null) {
                return@withContext Result.failure(Exception("Invalid state: $newStatus"))
            }

            // Set beginning date if the new state is under repair and it wasn't set before
            val beginningDate = if (newState.state.lowercase() in listOf("under repair", "em reparação", "em reparacao") && currentIssue.beginningDate == null) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            } else {
                currentIssue.beginningDate
            }

            // Set ending date if the new state is resolved (state_id 4)
            val endingDate = if (newState.state_id == 4) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
            } else {
                currentIssue.endingDate
            }
            
            // Create updated issue with new state and dates if applicable
            val updatedIssue = currentIssue.copy(
                state_id = newState.state_id,
                beginningDate = beginningDate,
                endingDate = endingDate
            )
            
            // Update the issue using the existing updateIssue method
            val updateResult = updateIssue(updatedIssue)
            
            if (updateResult.isSuccess) {
                // Create notification for the issue creator
                NotificationRepository().createNotification(
                    userId = currentIssue.id_user,
                    issueId = issueId,
                    description = notificationText
                )
            }
            
            updateResult.map { Unit }
        } catch (e: Exception) {
            Log.e("IssueRepository", "Error changing issue status: ", e)
            Result.failure(e)
        }
    }
}