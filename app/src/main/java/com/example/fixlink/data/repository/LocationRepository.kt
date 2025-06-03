package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Location
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository {
    suspend fun getLocationList(): Result<List<Location>> = withContext(Dispatchers.IO) {
        try {
            Log.d("LocationRepository", "Attempting to fetch location list from Supabase")
            val response = SupabaseClient.supabase.postgrest["Location"]
                .select()
            Log.d("LocationRepository", "Supabase query executed, attempting to decode response")
            val locations = response.decodeList<Location>()
            Log.d("LocationRepository", "Successfully decoded location list: ${locations.size} items")
            Log.d("LocationRepository", "Location names: ${locations.map { it.name }}")
            Result.success(locations)
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error fetching location list: ${e.message}", e)
            Log.e("LocationRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
}