package com.example.fixlink.data.repository

import android.util.Log
import com.example.fixlink.data.entities.Equipment
import com.example.fixlink.supabaseConfig.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EquipmentRepository {
    suspend fun getEquipmentList(): Result<List<Equipment>> = withContext(Dispatchers.IO) {
        try {
            Log.d("EquipmentRepository", "Attempting to fetch equipment list from Supabase")
            val response = SupabaseClient.supabase.postgrest["Equipment"]
                .select {
                    filter {
                        eq("active", true)
                    }
                }
            Log.d("EquipmentRepository", "Supabase query executed, attempting to decode response")
            val equipment = response.decodeList<Equipment>()
            Log.d("EquipmentRepository", "Successfully decoded equipment list: ${equipment.size} items")
            Log.d("EquipmentRepository", "Equipment names: ${equipment.map { it.name }}")
            Result.success(equipment)
        } catch (e: Exception) {
            Log.e("EquipmentRepository", "Error fetching equipment list: ${e.message}", e)
            Log.e("EquipmentRepository", "Error stack trace: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }




}