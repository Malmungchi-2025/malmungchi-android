package com.malmungchi.core.network

import com.malmungchi.core.network.api.StudyApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000")  // ✅ 로컬 서버
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val studyApi: StudyApi = retrofit.create(StudyApi::class.java)
}