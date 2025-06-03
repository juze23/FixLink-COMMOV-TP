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
            Log.d("PriorityRepository", "Supabase query executed, attempting to decode response")
            val priorities = response.decodeList<Priority>()
            Log.d("PriorityRepository", "Successfully decoded priority list: ${priorities.size} items")
            Log.d("PriorityRepository", "Priority names: ${priorities.map { it.priority }}")
            Result.success(priorities)
        } catch (e: Exception) {
            Log.e("PriorityRepository", "Error fetching priority list: ${e.message}", e)
            Log.e("PriorityRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
}