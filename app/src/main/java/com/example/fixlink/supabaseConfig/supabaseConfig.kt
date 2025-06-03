package com.example.fixlink.supabaseConfig

import com.example.fixlink.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient{
    val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.supabaseUrl,
        supabaseKey = BuildConfig.supabaseKey
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}