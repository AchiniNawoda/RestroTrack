package com.apptora.restrotrack.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {

    private const val PREF_NAME = "restrotrack_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        getPreferences(context)
            .edit()
            .putBoolean(KEY_IS_LOGGED_IN, loggedIn)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context)
            .getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logout(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}