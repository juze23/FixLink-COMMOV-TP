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

class IssueRepository {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun createIssue(
        userId: String,
        equipmentId: Int,
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
    
    suspend fun assignTechnicianToIssue(issueId: String, technicianId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current issue to update
            val currentIssue = getIssueById(issueId).getOrNull() ?: return@withContext Result.failure(Exception("Issue not found"))
            
            // Create updated issue with new technician and state
            val updatedIssue = currentIssue.copy(
                id_technician = technicianId,
                state_id = 2  // Set state to "assigned"
            )
            
            // Update the issue using the existing updateIssue method
            updateIssue(updatedIssue).map { Unit }
        } catch (e: Exception) {
            Log.e("IssueRepository", "Error assigning technician: ", e)
            Result.failure(e)
        }
    }

    suspend fun updateIssueReport(issueId: String, report: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // First get the current issue to update
            val currentIssue = getIssueById(issueId).getOrNull() ?: return@withContext Result.failure(Exception("Issue not found"))
            
            // Create updated issue with new report and state_id = 4
            val updatedIssue = currentIssue.copy(
                report = report,
                state_id = 4  // Set state to "Reported"
            )
            
            // Update the issue using the existing updateIssue method
            updateIssue(updatedIssue).map { Unit }
        } catch (e: Exception) {
            Log.e("IssueRepository", "Error updating issue report: ", e)
            Result.failure(e)
        }
    }
}