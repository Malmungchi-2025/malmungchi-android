package com.malmungchi.data.api

import retrofit2.http.GET

interface ServerApi {
    @GET("health")
    suspend fun checkHealth(): String
}