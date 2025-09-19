//package com.malmungchi.data.di.perference
//
//import android.content.Context
//import com.malmungchi.data.api.AuthService
//import com.malmungchi.data.implementation.repository.AuthPreferenceImpl
//import com.malmungchi.data.network.AuthHeaderInterceptor
//import com.malmungchi.data.preference.AuthPreference
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import java.util.concurrent.TimeUnit
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object AuthPreferenceModule {
//    @Provides
//    @Singleton
//    fun provideAuthPreference(
//        @ApplicationContext context: Context
//    ): AuthPreference {
//        return AuthPreferenceImpl(context)
//    }
//
//}