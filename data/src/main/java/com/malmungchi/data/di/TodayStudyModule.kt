package com.malmungchi.data.di


import com.malmungchi.data.api.TodayStudyApi
import com.malmungchi.data.implementation.repository.TodayStudyRepositoryImpl
import com.malmungchi.core.repository.TodayStudyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object TodayStudyModule {
    // ✅ 비움: TodayStudyApi/Repository는 NetworkModule에서만 제공
}
//@Module
//@InstallIn(SingletonComponent::class)
//object TodayStudyModule {
//
//    // ✅ 새로 OkHttpClient/Retrofit 만들지 말고,
//    //    이미 AuthModule에서 @Provides 한 Retrofit을 주입받아 재사용
//    @Provides
//    @Singleton
//    fun provideTodayStudyApi(retrofit: Retrofit): TodayStudyApi =
//        retrofit.create(TodayStudyApi::class.java)
//
//    @Provides
//    @Singleton
//    fun provideTodayStudyRepository(api: TodayStudyApi): TodayStudyRepository =
//        TodayStudyRepositoryImpl(api)
//}