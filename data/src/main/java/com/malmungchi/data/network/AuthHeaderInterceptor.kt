package com.malmungchi.data.network

import com.malmungchi.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val hasAuth = original.header("Authorization") != null
        val hasUid  = original.header("X-User-Id") != null

        val builder = original.newBuilder()

        // 토큰이 있고 기존에 Authorization이 없으면 자동 추가
        SessionManager.token?.let { t ->
            if (!hasAuth) builder.addHeader("Authorization", "Bearer $t")
        }

        // userId가 있고 기존 X-User-Id가 없으면 자동 추가
        SessionManager.userId?.let { uid ->
            if (!hasUid) builder.addHeader("X-User-Id", uid.toString())
        }

        return chain.proceed(builder.build())
    }
}