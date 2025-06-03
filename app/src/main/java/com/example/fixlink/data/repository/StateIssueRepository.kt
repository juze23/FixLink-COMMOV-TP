package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Issue
import com.example.fixlink.data.entities.Issue_state
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StateIssueRepository {
    suspend fun getIssueStates(): Result<List<Issue_state>> = withContext(Dispatchers.IO) {
        try {
            Log.d("StateIssueRepository", "Attempting to fetch issue states from Supabase")
            val response = SupabaseClient.supabase.postgrest["StateIssue"]
                .select()
            Log.d("StateIssueRepository", "Supabase query executed, attempting to decode response")
            val states = response.decodeList<Issue_state>()
            Log.d("StateIssueRepository", "Successfully decoded issue states: ${states.size} items")
            Log.d("StateIssueRepository", "Issue states: ${states.map { it.state }}")
            Result.success(states)
        } catch (e: Exception) {
            Log.e("StateIssueRepository", "Error fetching issue states: ${e.message}", e)
            Log.e("StateIssueRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
} 