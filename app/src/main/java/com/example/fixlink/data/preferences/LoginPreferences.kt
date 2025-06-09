package com.example.fixlink.data.preferences

import android.content.Context
import android.content.SharedPreferences

class LoginPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "LoginPrefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_REMEMBER_ME = "remember_me"
    }

    fun saveLoginState(userId: String, email: String, userType: Int, rememberMe: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_EMAIL, email)
            putInt(KEY_USER_TYPE, userType)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            apply()
        }
    }

    fun clearLoginState() {
        sharedPreferences.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun shouldRememberMe(): Boolean = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)

    fun getUserId(): String? = sharedPreferences.getString(KEY_USER_ID, null)

    fun getUserEmail(): String? = sharedPreferences.getString(KEY_USER_EMAIL, null)

    fun getUserType(): Int = sharedPreferences.getInt(KEY_USER_TYPE, -1)
} 