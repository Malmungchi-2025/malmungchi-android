package com.malmungchi.data.api

import com.malmungchi.data.api.dto.*
import retrofit2.http.Body
import retrofit2.http.POST

interface LevelTestApi {

    @POST("api/gpt/level-test/generate")
    suspend fun generateLevelTest(
        @Body body: GenerateLevelTestRequest
    ): ApiResponse<List<Question>>

    @POST("api/gpt/level-test/submit")
    suspend fun submitLevelTest(
        @Body body: SubmitLevelTestRequest
    ): LevelTestSubmitResponse

//    @POST("api/gpt/level-test/submit")
//    suspend fun submitLevelTest(
//        @Body body: SubmitLevelTestRequest
//    ): ApiResponse<SubmitLevelTestResult>
}