package com.malmungchi.data.di

import android.content.Context
import com.malmungchi.core.repository.QuizRepository
import com.malmungchi.data.api.QuizApi
import com.malmungchi.data.implementation.repository.QuizRepositoryImpl
import com.malmungchi.data.net.RetrofitProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuizDataModule {

    @Provides
    @Singleton
    fun provideQuizApi(
        @ApplicationContext context: Context
    ): QuizApi = RetrofitProvider.getQuizApi(context)

    @Provides @Singleton
    fun provideQuizRepository(
        api: QuizApi
    ): QuizRepository = QuizRepositoryImpl(api)
}