package com.malmungchi.data.di


import com.malmungchi.data.api.TodayStudyApi
import com.malmungchi.data.implementation.repository.TodayStudyRepositoryImpl
import com.malmungchi.core.repository.TodayStudyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TodayStudyModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(0, TimeUnit.SECONDS) // ✅ 연결 무제한 대기
            .readTimeout(0, TimeUnit.SECONDS)    // ✅ 응답 무제한 대기
            .writeTimeout(0, TimeUnit.SECONDS)   // ✅ 요청 전송 무제한 대기
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://malmungchi-server.onrender.com")
            .client(client) // ✅ 커스텀 클라이언트 적용
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTodayStudyApi(retrofit: Retrofit): TodayStudyApi =
        retrofit.create(TodayStudyApi::class.java)

    @Provides
    @Singleton
    fun provideTodayStudyRepository(api: TodayStudyApi): TodayStudyRepository =
        TodayStudyRepositoryImpl(api)
}
