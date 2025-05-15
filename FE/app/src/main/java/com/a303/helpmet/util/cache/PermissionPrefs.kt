package com.a303.helpmet.util.cache

import android.content.Context

object PermissionPrefs {
    private const val PREF_FILE_NAME = "helpmet_prefs"
    private const val KEY_NOTI_PERMISSION_ASKED = "noti_permission_asked"

    fun setNotificationAsked(context: Context, value: Boolean) {
        val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTI_PERMISSION_ASKED, value).apply()
    }

    fun wasNotificationAsked(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTI_PERMISSION_ASKED, false)
    }

    fun clearNotificationAsked(context: Context) {
        val prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_NOTI_PERMISSION_ASKED).apply()
    }
}