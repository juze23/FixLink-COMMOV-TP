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
            val response = SupabaseClient.supabase.postgrest["Issue_state"]
                .select()
            val states = response.decodeList<Issue_state>()
            Result.success(states)
        } catch (e: Exception) {

            Result.failure(e)
        }
    }
} 