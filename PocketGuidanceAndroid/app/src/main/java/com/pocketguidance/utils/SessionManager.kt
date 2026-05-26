package com.pocketguidance.utils

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "pg_session"
    private const val KEY_USER_ID = "userId"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(context: Context, userId: Long, username: String, email: String) {
        prefs(context).edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .apply()
    }

    fun getUserId(context: Context): Long = prefs(context).getLong(KEY_USER_ID, -1L)

    fun getUsername(context: Context): String = prefs(context).getString(KEY_USERNAME, "") ?: ""

    fun getEmail(context: Context): String = prefs(context).getString(KEY_EMAIL, "") ?: ""

    fun isLoggedIn(context: Context): Boolean = getUserId(context) != -1L

    fun clearSession(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
