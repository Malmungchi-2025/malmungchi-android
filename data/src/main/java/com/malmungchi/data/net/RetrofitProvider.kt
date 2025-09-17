package com.malmungchi.data.net

import android.content.Context
import com.google.gson.GsonBuilder
import com.malmungchi.data.api.TodayStudyApi
import com.malmungchi.data.api.AuthService
import com.malmungchi.data.BuildConfig              // ★ 라이브러리 모듈 BuildConfig 경로
import com.malmungchi.data.api.LevelTestApi
import com.malmungchi.data.api.QuizApi
import com.malmungchi.data.api.VoiceApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

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

    /** ✅ 레벨 테스트 API */
    fun getLevelTestApi(
        context: Context,
        onUnauthorized: () -> Unit = {}
    ): LevelTestApi = getRetrofit(context, onUnauthorized).create(LevelTestApi::class.java)

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
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // ★ 서버보다 길게
            .callTimeout(40, java.util.concurrent.TimeUnit.SECONDS) // ★ 전체 요청 상한(선택)
            .retryOnConnectionFailure(true)
            .pingInterval(15, java.util.concurrent.TimeUnit.SECONDS) // (옵션) 장대기 연결 유지
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

    fun getQuizApi(
        context: Context,
        onUnauthorized: () -> Unit = {}
    ): QuizApi = getRetrofit(context, onUnauthorized).create(QuizApi::class.java)

    /** Voice API */
    fun getVoiceApi(
        context: Context,
        onUnauthorized: () -> Unit = {}
    ): VoiceApi = getRetrofit(context, onUnauthorized).create(VoiceApi::class.java)

    // RetrofitProvider.kt



}