package com.malmungchi.data.api

import com.malmungchi.core.model.QuizAnswerRequest
import com.malmungchi.core.model.QuizGenerationRequest
import com.malmungchi.core.model.QuizItem
import com.malmungchi.core.model.WordItem
import com.malmungchi.data.api.dto.BaseResponse
import com.malmungchi.data.api.dto.QuoteResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TodayStudyApi {

    // ✅ 오늘의 글감 (서버가 studyId도 top-level로 내려줌)
    @POST("/api/gpt/generate-quote")
    suspend fun generateQuote(): QuoteResponse

    // ✅ 단어 검색 (GPT, 저장 X)
    @POST("/api/vocabulary/search")
    suspend fun searchWord(@Body request: WordRequest): BaseResponse<WordItem>

    // ✅ 단어 저장 (오늘 study_id에 귀속)
    @POST("/api/vocabulary")
    suspend fun saveWord(@Body request: WordSaveRequest): BaseResponse<Unit>

    // ✅ 단어 목록 조회 (특정 studyId 기준)
    // 서버가 ?today=1도 받게 해두었으므로 필요 시 @Query로 today=1 보낼 수 있게 열어둠
    @GET("/api/vocabulary/{studyId}")
    suspend fun getVocabularyList(
        @Path("studyId") studyId: Int,
        @Query("today") today: String? = null // null이면 무시, "1"이면 오늘 걸로 강제
    ): BaseResponse<List<WordItem>>

    // ✅ 필사 저장/조회
    @POST("/api/study/handwriting")
    suspend fun saveHandwriting(@Body request: HandwritingRequest): BaseResponse<Unit>

    @GET("/api/study/handwriting/{studyId}")
    suspend fun getHandwriting(@Path("studyId") studyId: Int): BaseResponse<String>

    // ✅ 퀴즈
    @POST("/api/gpt/generate-quiz")
    suspend fun generateQuiz(@Body request: QuizGenerationRequest): BaseResponse<List<QuizItem>>

    @GET("/api/gpt/quiz/{studyId}")   // 🔧 서버 경로와 일치
    suspend fun getQuizList(@Path("studyId") id: Int): BaseResponse<List<QuizItem>>

    @POST("/api/gpt/quiz/answer")     // 🔧 서버 경로와 일치
    suspend fun saveQuizAnswer(@Body req: QuizAnswerRequest): BaseResponse<Unit>

    // 요청 바디들
    data class HandwritingRequest(val study_id: Int, val content: String)
}



//interface TodayStudyApi {
//    @POST("/api/gpt/generate-quote")
//    suspend fun generateQuote(): ApiResponse<String>// 서버가 studyId도 함께 내려줌(제네릭 result는 text)
//
//    @POST("/api/vocabulary/search")
//    suspend fun searchWord(@Body request: WordRequest): ApiResponse<WordItem>
//
//    @POST("/api/vocabulary")
//    suspend fun saveWord(@Body request: WordSaveRequest): ApiResponse<Unit>
//
//    @GET("/api/vocabulary/{studyId}")
//    suspend fun getVocabularyList(@Path("studyId") studyId: Int): ApiResponse<List<WordItem>>
//
//    @POST("/api/study/handwriting")
//    suspend fun saveHandwriting(@Body request: HandwritingRequest): ApiResponse<Unit>
//
//    @GET("/api/study/handwriting/{studyId}")
//    suspend fun getHandwriting(@Path("studyId") studyId: Int): ApiResponse<String>
//
//    @POST("/api/gpt/generate-quiz")
//    suspend fun generateQuiz(@Body request: QuizGenerationRequest): ApiResponse<List<QuizItem>>
//
//    @GET("/api/gpt/quiz/{studyId}")               // 🔧 gpt 추가
//    suspend fun getQuizList(@Path("studyId") id: Int): BaseResponse<List<QuizItem>>
//
//    @POST("/api/gpt/quiz/answer")                  // 🔧 gpt 추가
//    suspend fun saveQuizAnswer(@Body req: QuizAnswerRequest): BaseResponse<Unit>
//
//    data class HandwritingRequest(val study_id: Int, val content: String)
//}

//interface TodayStudyApi {
//    @POST("/api/gpt/generate-quote")
//    suspend fun generateQuote(
//        @Header("Authorization") token: String
//    ): ApiResponse<String>
//
//    // ✅ gpt prefix 제거
//    @POST("/api/vocabulary/search")
//    suspend fun searchWord(
//        @Header("Authorization") token: String,
//        @Body request: WordRequest
//    ): ApiResponse<WordItem> // <- 서버가 단일 객체 주면 WordItem, 배열이면 List<WordItem>
//
//    // ✅ gpt prefix 제거
//    @POST("/api/vocabulary")
//    suspend fun saveWord(
//        @Header("Authorization") token: String,
//        @Body request: WordSaveRequest
//    ): ApiResponse<Unit>
//
//    // ✅ gpt prefix 제거
//    @GET("/api/vocabulary/{studyId}")
//    suspend fun getVocabularyList(
//        @Header("Authorization") token: String,
//        @Path("studyId") studyId: Int
//    ): ApiResponse<List<WordItem>>
//
//    @POST("/api/study/handwriting")
//    suspend fun saveHandwriting(
//        @Header("Authorization") token: String,
//        @Body request: HandwritingRequest
//    ): ApiResponse<Unit>
//
//    @GET("/api/study/handwriting/{studyId}")
//    suspend fun getHandwriting(
//        @Header("Authorization") token: String,
//        @Path("studyId") studyId: Int
//    ): ApiResponse<String>
//
//    @POST("/api/gpt/generate-quiz")
//    suspend fun generateQuiz(
//        @Header("Authorization") token: String,
//        @Body request: QuizGenerationRequest
//    ): ApiResponse<List<QuizItem>>
//
//    // ✅ gpt prefix 제거
//    @GET("/api/quiz/{studyId}")
//    suspend fun getQuizList(
//        @Header("Authorization") token: String,
//        @Path("studyId") studyId: Int
//    ): ApiResponse<List<QuizItem>>
//
//    // ✅ gpt prefix 제거
//    @POST("/api/quiz/answer")
//    suspend fun saveQuizAnswer(
//        @Header("Authorization") token: String,
//        @Body request: QuizAnswerRequest
//    ): ApiResponse<Unit>
//
//    data class HandwritingRequest(val study_id: Int, val content: String)
//}

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


