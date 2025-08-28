package com.malmungchi.data.net

import android.content.Context
import com.google.gson.GsonBuilder
import com.malmungchi.data.api.TodayStudyApi
import com.malmungchi.data.api.AuthService
import com.malmungchi.data.BuildConfig              // ★ 라이브러리 모듈 BuildConfig 경로
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {

    @Volatile private var retrofit: Retrofit? = null

    /** 학습 API */
    fun getTodayStudyApi(
        context: Context,
        onUnauthorized: () -> Unit = {}
    ): TodayStudyApi = getRetrofit(context, onUnauthorized).create(TodayStudyApi::class.java)

    /** 인증 API */
    fun getAuthApi(
        context: Context,
        onUnauthorized: () -> Unit = {}
    ): AuthService = getRetrofit(context, onUnauthorized).create(AuthService::class.java)

    // ---- internal ----
    private fun getRetrofit(context: Context, onUnauthorized: () -> Unit): Retrofit {
        val cached = retrofit
        if (cached != null) return cached

        return synchronized(this) {
            retrofit ?: buildRetrofit(context, onUnauthorized).also { retrofit = it }
        }
    }

    // com.malmungchi.data.net.RetrofitProvider
    private fun buildRetrofit(context: Context, onUnauthorized: () -> Unit): Retrofit {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(SharedPrefsTokenProvider(context)))
            .addInterceptor(RetryAuthInterceptor(context))                 // ★ 리프레시 재시도 먼저
            .addInterceptor(UnauthorizedInterceptor(context, onUnauthorized)) // ★ 실패 시 세션 정리
            .addInterceptor(logger)
            .build()

        val gson = GsonBuilder().setLenient().create()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}