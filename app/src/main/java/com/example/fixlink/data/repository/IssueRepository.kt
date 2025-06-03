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
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.client.utils.EmptyContent.headers
import kotlinx.atomicfu.TraceBase.None.append
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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
            // Upload image if provided
            val imagePath = if (imageUri != null) {
                uploadImage(imageUri, context)
            } else {
                null
            }
            
            // Get current timestamp
            val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
            
            // Create issue object
            val issue = Issue(
                issue_id = UUID.randomUUID().toString(),
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
    
    private suspend fun uploadImage(imageUri: Uri, context: Context): String? {
        return try {
            // Generate a unique filename for the image
            val fileName = "issue_${System.currentTimeMillis()}.jpg"
            
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
}