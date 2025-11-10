package com.malmungchi.data.implementation.preference


import android.content.Context
import com.malmungchi.data.implementation.repository.AuthPreferenceImpl
import com.malmungchi.data.preference.AuthPreference                    // ✅ 패키지 경로 수정
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthPreferenceModule {
    @Provides
    @Singleton
    fun provideAuthPreference(
        @ApplicationContext context: Context
    ): AuthPreference {
        return AuthPreferenceImpl(context)
    }
}