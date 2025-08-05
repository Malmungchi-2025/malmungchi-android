package com.malmungchi.data.implementation.repository

import android.content.Context
import android.content.SharedPreferences
import com.malmungchi.data.preference.AuthPreference

class AuthPreferenceImpl(context: Context) : AuthPreference {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    override var accessToken: String?
        get() = prefs.getString("access_token", null)
        set(value) = prefs.edit().putString("access_token", value).apply()

    override var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(value) = prefs.edit().putString("refresh_token", value).apply()
}