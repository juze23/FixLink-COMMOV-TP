package com.example.fixlink.data.preferences

import android.content.Context
import android.content.SharedPreferences

class ProfilePreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "ProfilePrefs"
        private const val KEY_PENDING_UPDATES = "pending_updates"
        private const val KEY_FIRSTNAME = "firstname"
        private const val KEY_LASTNAME = "lastname"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_USER_ID = "user_id"
    }

    fun savePendingProfileUpdate(
        userId: String,
        firstname: String,
        lastname: String,
        email: String,
        phone: String
    ) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_PENDING_UPDATES, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_FIRSTNAME, firstname)
            putString(KEY_LASTNAME, lastname)
            putString(KEY_EMAIL, email)
            putString(KEY_PHONE, phone)
            apply()
        }
    }

    fun hasPendingUpdates(): Boolean = sharedPreferences.getBoolean(KEY_PENDING_UPDATES, false)

    fun getPendingProfileUpdate(): ProfileUpdate? {
        if (!hasPendingUpdates()) return null

        return ProfileUpdate(
            userId = sharedPreferences.getString(KEY_USER_ID, "") ?: "",
            firstname = sharedPreferences.getString(KEY_FIRSTNAME, "") ?: "",
            lastname = sharedPreferences.getString(KEY_LASTNAME, "") ?: "",
            email = sharedPreferences.getString(KEY_EMAIL, "") ?: "",
            phone = sharedPreferences.getString(KEY_PHONE, "") ?: ""
        )
    }

    fun clearPendingUpdates() {
        sharedPreferences.edit().clear().apply()
    }

    data class ProfileUpdate(
        val userId: String,
        val firstname: String,
        val lastname: String,
        val email: String,
        val phone: String
    )
} 