package com.malmungchi.data.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.malmungchi.data.serializer.LocalDateSerializer
import com.malmungchi.data.serializer.LocalDateTimeSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Singleton

@Suppress("NewApi")
@Module
@InstallIn(SingletonComponent::class)
object GsonModule {
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .registerTypeAdapter(java.time.LocalDate::class.java, LocalDateSerializer())
            .registerTypeAdapter(java.time.LocalDateTime::class.java, LocalDateTimeSerializer())
            .create()
    }
}