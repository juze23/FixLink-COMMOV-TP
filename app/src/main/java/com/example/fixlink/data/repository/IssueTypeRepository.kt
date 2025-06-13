package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Issue_type
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IssueTypeRepository {
    private val TAG = "IssueTypeRepository"
    private val supabase = SupabaseClient.supabase

    suspend fun getIssueTypes(): Result<List<Issue_type>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to fetch issue types from Supabase")
            val response = supabase.postgrest["Issue_type"]
                .select()
            Log.d(TAG, "Supabase query executed, attempting to decode response")
            val types = response.decodeList<Issue_type>()
            Log.d(TAG, "Successfully decoded issue types: ${types.size} items")
            Log.d(TAG, "Issue types: ${types.map { it.type }}")
            Result.success(types)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching issue types: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun addIssueType(typeName: String): Result<Issue_type> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to add new issue type: $typeName")
            val newType = Issue_type(
                type = typeName
            )
            
            supabase.postgrest["Issue_type"]
                .insert(newType)
            
            // Get the created type by name
            val response = supabase.postgrest["Issue_type"]
                .select {
                    filter {
                        eq("type", typeName)
                    }
                }
                .decodeSingle<Issue_type>()
            
            Log.d(TAG, "Successfully added new issue type: $typeName with ID: ${response.type_id}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding issue type: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun updateType(typeId: Int, newName: String): Result<Issue_type> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Updating issue type: id=$typeId, newName=$newName")
            
            // Update the type in the database
            supabase.postgrest["Issue_type"]
                .update({
                    set("type", newName)
                }) {
                    filter {
                        eq("type_id", typeId)
                    }
                }
            
            // Get the updated type
            val result = supabase.postgrest["Issue_type"]
                .select {
                    filter {
                        eq("type_id", typeId)
                    }
                }
                .decodeSingle<Issue_type>()

            Log.d(TAG, "Successfully updated issue type: $result")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating issue type", e)
            Result.failure(e)
        }
    }
} 