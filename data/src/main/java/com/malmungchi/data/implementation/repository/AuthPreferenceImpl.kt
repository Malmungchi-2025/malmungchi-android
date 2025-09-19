package com.malmungchi.data.implementation.repository

import android.content.Context
import android.content.SharedPreferences
import com.malmungchi.data.preference.AuthPreference

class AuthPreferenceImpl(context: Context) : AuthPreference {

    private val prefs =
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE) // ★ 통일

    override var accessToken: String?
        get() = prefs.getString("token", null)                // ★ 통일
        set(value) {
            prefs.edit().apply {
                if (value == null) remove("token") else putString("token", value)
            }.apply()
        }

    override var refreshToken: String?
        get() = prefs.getString("refresh_token", null)        // ★ 통일
        set(value) {
            prefs.edit().apply {
                if (value == null) remove("refresh_token") else putString("refresh_token", value)
            }.apply()
        }

    override fun clear() {
        prefs.edit()
            .remove("token")
            .remove("refresh_token")
            .remove("user_id")
            .apply()
    }
}

//class AuthPreferenceImpl(context: Context) : AuthPreference {
//
//    private val prefs: SharedPreferences =
//        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
//
//    override var accessToken: String?
//        get() = prefs.getString("access_token", null)
//        set(value) = prefs.edit().putString("access_token", value).apply()
//
//    override var refreshToken: String?
//        get() = prefs.getString("refresh_token", null)
//        set(value) = prefs.edit().putString("refresh_token", value).apply()
//
//    override fun clear() {
//        prefs.edit()
//            .remove("token")
//            .remove("refresh_token")
//            .remove("user_id")
//            .apply()
//    } //로그아웃 구현
//}