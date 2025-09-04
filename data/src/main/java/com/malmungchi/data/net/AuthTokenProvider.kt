package com.malmungchi.data.net


import android.content.Context

/** 어디서든 현재 저장된 토큰을 얻기 위한 provider */
interface AuthTokenProvider {
    fun getToken(): String?
}

class SharedPrefsTokenProvider(private val context: Context) : AuthTokenProvider {
    private val pref by lazy {
        // MainApp에서 사용한 것과 동일한 prefs/keys
        context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    }
    override fun getToken(): String? = pref.getString("token", null)
}