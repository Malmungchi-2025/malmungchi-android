package com.malmungchi.data.di


import android.content.Context
import com.malmungchi.core.repository.LevelTestRepository
import com.malmungchi.core.repository.TodayStudyRepository
import com.malmungchi.data.api.AuthService
import com.malmungchi.data.api.LevelTestApi
import com.malmungchi.data.api.TodayStudyApi
import com.malmungchi.data.implementation.repository.LevelTestRepositoryImpl
import com.malmungchi.data.implementation.repository.TodayStudyRepositoryImpl
import com.malmungchi.data.net.RetrofitProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Provides
    @Singleton
    fun provideTodayStudyApi(
        @ApplicationContext context: Context
    ): TodayStudyApi {
        // ✅ 반드시 RetrofitProvider 경유 (Auth/Retry/401 인터셉터 공유)
        return RetrofitProvider.getTodayStudyApi(context.applicationContext)
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        @ApplicationContext context: Context
    ): AuthService {
        return RetrofitProvider.getAuthApi(context.applicationContext)
    }

    @Provides
    @Singleton
    fun provideTodayStudyRepository(
        api: TodayStudyApi
    ): TodayStudyRepository {
        return TodayStudyRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideLevelTestApi(
        @ApplicationContext context: Context
    ): LevelTestApi {
        return RetrofitProvider.getLevelTestApi(context.applicationContext)
    }

    @Provides
    @Singleton
    fun provideLevelTestRepository(
        api: LevelTestApi
    ): LevelTestRepository {
        return LevelTestRepositoryImpl(api)
    }
}