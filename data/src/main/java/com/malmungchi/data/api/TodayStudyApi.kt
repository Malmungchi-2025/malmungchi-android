package com.malmungchi.data.api

import com.malmungchi.core.model.WordItem
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TodayStudyApi {
    @POST("/api/gpt/generate-quote")
    suspend fun generateQuote(
        @Header("Authorization") token: String
    ): ApiResponse<String>

    @POST("/api/vocabulary/search")
    suspend fun searchWord(
        @Header("Authorization") token: String,
        @Body request: WordRequest
    ): ApiResponse<WordItem>

    @POST("/api/vocabulary")
    suspend fun saveWord(
        @Header("Authorization") token: String,
        @Body request: WordSaveRequest
    ): ApiResponse<Unit>

    @GET("/api/vocabulary/{studyId}")
    suspend fun getVocabularyList(
        @Header("Authorization") token: String,
        @Path("studyId") studyId: Int
    ): ApiResponse<List<WordItem>>
}

// ✅ 공통 응답 모델
data class ApiResponse<T>(
    val success: Boolean,
    val result: T?,
    val studyId: Int?,
    val message: String?
)

// ✅ Request DTO
data class WordRequest(val word: String)
data class WordSaveRequest(val study_id: Int, val word: String, val meaning: String, val example: String?)
