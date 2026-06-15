package com.example.savora

import android.content.Context

class SessionManager(context: Context) {

    private val prefs =
        context.getSharedPreferences("SavoraSession", Context.MODE_PRIVATE)

    fun saveUser(userId: Int) {
        prefs.edit().putInt("USER_ID", userId).apply()
    }

    fun getUserId(): Int {
        return prefs.getInt("USER_ID", -1)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}