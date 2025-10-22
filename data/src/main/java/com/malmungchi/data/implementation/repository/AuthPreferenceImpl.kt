package com.malmungchi.data.implementation.repository

import android.content.Context
import android.content.SharedPreferences
import com.malmungchi.data.preference.AuthPreference

class AuthPreferenceImpl(context: Context) : AuthPreference {

    private val prefs =
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE) // ★ 통일

    companion object {
        private const val KEY_ACCESS_TOKEN = "token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_REPRESENTATIVE_BADGE = "representative_badge" // ✅ 추가!
    }

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
            .remove(KEY_REPRESENTATIVE_BADGE) // ✅ 함께 초기화
            .apply()
    }

    // ✅ 대표 배지 저장
    override fun saveRepresentativeBadge(badgeKey: String) {
        prefs.edit().putString("representative_badge", badgeKey).apply()
    }

    // ✅ 대표 배지 불러오기
    override fun getRepresentativeBadge(): String? {
        return prefs.getString("representative_badge", null)
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