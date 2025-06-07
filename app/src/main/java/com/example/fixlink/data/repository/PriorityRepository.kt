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
            val response = SupabaseClient.supabase.postgrest["Priority"]
                .select()
            val priorities = response.decodeList<Priority>()
            Result.success(priorities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}