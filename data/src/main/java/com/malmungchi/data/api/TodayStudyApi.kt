package com.malmungchi.data.api

import com.malmungchi.core.model.QuizAnswerRequest
import com.malmungchi.core.model.QuizGenerationRequest
import com.malmungchi.core.model.QuizItem
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

    // ✅ gpt prefix 제거
    @POST("/api/vocabulary/search")
    suspend fun searchWord(
        @Header("Authorization") token: String,
        @Body request: WordRequest
    ): ApiResponse<WordItem> // <- 서버가 단일 객체 주면 WordItem, 배열이면 List<WordItem>

    // ✅ gpt prefix 제거
    @POST("/api/vocabulary")
    suspend fun saveWord(
        @Header("Authorization") token: String,
        @Body request: WordSaveRequest
    ): ApiResponse<Unit>

    // ✅ gpt prefix 제거
    @GET("/api/vocabulary/{studyId}")
    suspend fun getVocabularyList(
        @Header("Authorization") token: String,
        @Path("studyId") studyId: Int
    ): ApiResponse<List<WordItem>>

    @POST("/api/study/handwriting")
    suspend fun saveHandwriting(
        @Header("Authorization") token: String,
        @Body request: HandwritingRequest
    ): ApiResponse<Unit>

    @GET("/api/study/handwriting/{studyId}")
    suspend fun getHandwriting(
        @Header("Authorization") token: String,
        @Path("studyId") studyId: Int
    ): ApiResponse<String>

    @POST("/api/gpt/generate-quiz")
    suspend fun generateQuiz(
        @Header("Authorization") token: String,
        @Body request: QuizGenerationRequest
    ): ApiResponse<List<QuizItem>>

    // ✅ gpt prefix 제거
    @GET("/api/quiz/{studyId}")
    suspend fun getQuizList(
        @Header("Authorization") token: String,
        @Path("studyId") studyId: Int
    ): ApiResponse<List<QuizItem>>

    // ✅ gpt prefix 제거
    @POST("/api/quiz/answer")
    suspend fun saveQuizAnswer(
        @Header("Authorization") token: String,
        @Body request: QuizAnswerRequest
    ): ApiResponse<Unit>

    data class HandwritingRequest(val study_id: Int, val content: String)
}

//
//interface TodayStudyApi {
//    @POST("/api/gpt/generate-quote")
//    suspend fun generateQuote(
//        @Header("Authorization") token: String
//    ): ApiResponse<String>
//
//    @POST("/api/gpt/vocabulary/search")
//    suspend fun searchWord(
//        @Header("Authorization") token: String,
//        @Body request: WordRequest
//    ): ApiResponse<List<WordItem>>
//
//    @POST("/api/gpt/vocabulary")
//    suspend fun saveWord(
//        @Header("Authorization") token: String,
//        @Body request: WordSaveRequest
//    ): ApiResponse<Unit>
//
//    @GET("/api/gpt/vocabulary/{studyId}")
//    suspend fun getVocabularyList(
//        @Header("Authorization") token: String,
//        @Path("studyId") studyId: Int
//    ): ApiResponse<List<WordItem>>
//
//    //필사한 내용 저장
//    @POST("/api/study/handwriting")
//    suspend fun saveHandwriting(
//        @Header("Authorization") token: String,
//        @Body request: HandwritingRequest
//    ): ApiResponse<Unit>
//
//    //필사한 내용 불러오기
//    // TodayStudyApi.kt
//    @GET("/api/study/handwriting/{studyId}")
//    suspend fun getHandwriting(
//        @Header("Authorization") token: String,
//        @Path("studyId") studyId: Int
//    ): ApiResponse<String>
//
//    //퀴즈 부분
//    @POST("/api/gpt/generate-quiz")
//    suspend fun generateQuiz(
//        @Header("Authorization") token: String,
//        @Body request: QuizGenerationRequest
//    ): ApiResponse<List<QuizItem>>
//
//    @GET("/api/gpt/quiz/{studyId}")
//    suspend fun getQuizList(
//        @Header("Authorization") token: String,
//        @Path("studyId") studyId: Int
//    ): ApiResponse<List<QuizItem>>
//
//    @POST("/api/gpt/quiz/answer")
//    suspend fun saveQuizAnswer(
//        @Header("Authorization") token: String,
//        @Body request: QuizAnswerRequest
//    ): ApiResponse<Unit>
//
//    data class HandwritingRequest(val study_id: Int, val content: String)
//}
//
//// ✅ 공통 응답 모델
//data class ApiResponse<T>(
//    val success: Boolean,
//    val result: T?,
//    val studyId: Int?,
//    val message: String?
//)
//
//// ✅ Request DTO
//data class WordRequest(val word: String)
//data class WordSaveRequest(val study_id: Int, val word: String, val meaning: String, val example: String?)


