package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Priority
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PriorityRepository {
    suspend fun getPriorityList(): Result<List<Priority>> = withContext(Dispatchers.IO) {
        try {
            Log.d("PriorityRepository", "Attempting to fetch priority list from Supabase")
            val response = SupabaseClient.supabase.postgrest["Priority"]
                .select()
            val priorities = response.decodeList<Priority>()
            Log.d("PriorityRepository", "Successfully decoded priority list: ${priorities.size} items")
            Result.success(priorities)
        } catch (e: Exception) {
            Log.e("PriorityRepository", "Error fetching priority list: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updatePriority(priorityId: Int, newName: String): Result<Priority> = withContext(Dispatchers.IO) {
        try {
            Log.d("PriorityRepository", "Attempting to update priority $priorityId with new name: $newName")
            
            // Update the priority in the database
            SupabaseClient.supabase.postgrest["Priority"]
                .update({
                    set("priority", newName)
                }) {
                    filter {
                        eq("priority_id", priorityId)
                    }
                }
            
            // Get the updated priority
            val response = SupabaseClient.supabase.postgrest["Priority"]
                .select {
                    filter {
                        eq("priority_id", priorityId)
                    }
                }
                .decodeSingle<Priority>()
            
            Log.d("PriorityRepository", "Successfully updated priority: ${response.priority}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("PriorityRepository", "Error updating priority: ${e.message}", e)
            Log.e("PriorityRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
}