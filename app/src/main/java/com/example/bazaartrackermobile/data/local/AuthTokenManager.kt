package com.example.bazaartrackermobile.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AuthTokenManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "bazaar_tracker_prefs"
        private const val KEY_TOKEN = "jwt_token"
    }

    fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun clearToken() {
        prefs.edit { remove(KEY_TOKEN) }
    }
}