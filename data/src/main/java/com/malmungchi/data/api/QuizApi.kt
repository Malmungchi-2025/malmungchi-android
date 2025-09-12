package com.malmungchi.data.api

import com.malmungchi.data.api.dto.*
import retrofit2.http.*

interface QuizApi {

    /** 7문항 세트 생성 (항상 새 배치) */
    @POST("/api/gpt/quiz")
    suspend fun createBatch(
        @Body body: CreateBatchBody
    ): BaseResponse<BatchResultDto>

    /** 세트 조회 */
    @GET("/api/gpt/quiz/{batchId}")
    suspend fun getBatch(
        @Path("batchId") batchId: Long
    ): BaseResponse<BatchGetResultDto>

    /** 문항 단위 제출/채점 */
    @POST("/api/gpt/quiz/submit")
    suspend fun submit(
        @Body body: SubmitBody
    ): BaseResponse<SubmitResultDto>

    /** 날짜별 요약(옵션) */
    @GET("/api/gpt/summary/daily")
    suspend fun getDailySummary(
        @Query("date") date: String? = null
    ): BaseResponse<List<DailySummaryRowDto>>
}