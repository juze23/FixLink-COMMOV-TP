package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Issue_type
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IssueTypeRepository {
    suspend fun getIssueTypes(): Result<List<Issue_type>> = withContext(Dispatchers.IO) {
        try {
            Log.d("IssueTypeRepository", "Attempting to fetch issue types from Supabase")
            val response = SupabaseClient.supabase.postgrest["Issue_type"]
                .select()
            Log.d("IssueTypeRepository", "Supabase query executed, attempting to decode response")
            val types = response.decodeList<Issue_type>()
            Log.d("IssueTypeRepository", "Successfully decoded issue types: ${types.size} items")
            Log.d("IssueTypeRepository", "Issue types: ${types.map { it.type }}")
            Result.success(types)
        } catch (e: Exception) {
            Log.e("IssueTypeRepository", "Error fetching issue types: ${e.message}", e)
            Log.e("IssueTypeRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
} 