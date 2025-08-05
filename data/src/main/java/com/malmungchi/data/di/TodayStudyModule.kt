package com.malmungchi.data.di


import com.malmungchi.data.api.TodayStudyApi
import com.malmungchi.data.implementation.repository.TodayStudyRepositoryImpl
import com.malmungchi.core.repository.TodayStudyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TodayStudyModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://malmungchi-server.onrender.com") // ✅ NetworkConfig 제거 → 직접 URL 사용
            .addConverterFactory(GsonConverterFactory.create()) // ✅ 정상 import
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
