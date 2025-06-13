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

    suspend fun registerLocation(locationName: String): Result<Location> = withContext(Dispatchers.IO) {
        try {
            Log.d("LocationRepository", "Attempting to register new location: $locationName")
            
            val location = Location(
                name = locationName
            )
            
            // Insert into database
            SupabaseClient.supabase.postgrest["Location"]
                .insert(location)
            
            // Get the created location by name
            val response = SupabaseClient.supabase.postgrest["Location"]
                .select {
                    filter {
                        eq("name", locationName)
                    }
                }
                .decodeSingle<Location>()
            
            Log.d("LocationRepository", "Successfully registered location: ${response.name}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error registering location: ${e.message}", e)
            Log.e("LocationRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }

    suspend fun updateLocation(locationId: Int, newName: String): Result<Location> = withContext(Dispatchers.IO) {
        try {
            Log.d("LocationRepository", "Attempting to update location $locationId with new name: $newName")
            
            // Update the location in the database
            SupabaseClient.supabase.postgrest["Location"]
                .update({
                    set("name", newName)
                }) {
                    filter {
                        eq("location_id", locationId)
                    }
                }
            
            // Get the updated location
            val response = SupabaseClient.supabase.postgrest["Location"]
                .select {
                    filter {
                        eq("location_id", locationId)
                    }
                }
                .decodeSingle<Location>()
            
            Log.d("LocationRepository", "Successfully updated location: ${response.name}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error updating location: ${e.message}", e)
            Log.e("LocationRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }
}