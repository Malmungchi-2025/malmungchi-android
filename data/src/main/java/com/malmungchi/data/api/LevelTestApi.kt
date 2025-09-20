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


    // 🔵 (신규) 3문항
    @POST("api/gpt/levels/start")
    suspend fun levelsStart(
        @Body body: LevelsStartRequest
    ): ApiResponse<Unit>

    @POST("api/gpt/levels/generate")
    suspend fun levelsGenerate(
        @Body body: LevelsGenerateRequest
    ): LevelsGenerateResponse

    @POST("/api/gpt/levels/submit")
    suspend fun levelsSubmit(
        @Body body: LevelsSubmitRequest
    ): LevelsSubmitResponseDto
}