package com.malmungchi.core.network.api

import retrofit2.Call
import retrofit2.http.*

data class GptRequest(val prompt: String)
data class GptResponse(val result: String)

data class CheckTodayStudyResponse(val exists: Boolean, val study_id: Int?, val content: String?)
data class SaveTodayStudyRequest(val user_id: Int?, val content: String)
data class SaveTodayStudyResponse(val success: Boolean, val study_id: Int?)

interface StudyApi {

    @POST("/gpt")
    fun getGptResponse(@Body request: GptRequest): Call<GptResponse>

    @GET("/today-study/check")
    fun checkTodayStudy(): Call<CheckTodayStudyResponse>

    @POST("/save-today-study")
    fun saveTodayStudy(@Body request: SaveTodayStudyRequest): Call<SaveTodayStudyResponse>

    @GET("/get-today-study/{study_id}")
    fun getTodayStudy(@Path("study_id") studyId: Int): Call<CheckTodayStudyResponse>

    @POST("/save-word")
    fun saveWord(@Body request: SaveWordRequest): Call<SaveWordResponse>
}
data class SaveWordRequest(
    val word: String,
    val meaning: String,
    val example: String,
    val study_id: Int
)

data class SaveWordResponse(
    val success: Boolean,
    val saved_from_study_id: Int?,
    val message: String?
)
