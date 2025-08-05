package com.malmungchi.data.di.api

import com.squareup.moshi.Moshi
import com.malmungchi.data.BuildConfig
import com.malmungchi.data.api.ServerApi
import com.malmungchi.data.preference.AuthPreference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ServerApiModule {
    @Provides
    @Singleton
    fun provideServerApi(
        authPreference: AuthPreference,
        moshi: Moshi,
    ): ServerApi {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addNetworkInterceptor {
                val request = it.request()
                    .newBuilder()
                    .let { builder ->
                        authPreference.accessToken?.let { token ->
                            builder.addHeader("Authorization", "Bearer $token")
                        } ?: builder
                    }
                    .build()
                it.proceed(request)
            }
            .addInterceptor(logger)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.SERVER_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
            .create(ServerApi::class.java)
    }
}