package com.malmungchi.data.di


import com.malmungchi.data.preference.AuthPreference
import com.malmungchi.data.implementation.repository.AuthPreferenceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {


}