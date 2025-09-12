package com.malmungchi.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateBatchBody(
    val category: String,   // "취업준비" | "기초" | "활용" | "심화" | "고급"
    val len: Int? = 80
)

@JsonClass(generateAdapter = true)
data class SubmitBody(
    val batchId: Long,
    val questionIndex: Int,
    val payload: SubmitPayloadDto
)

@JsonClass(generateAdapter = true)
data class SubmitPayloadDto(
    val selectedOptionId: Int? = null,
    val selectedIsO: Boolean? = null,
    val textAnswer: String? = null
)

/* ===== 응답 바디 - 생성/조회 공통 ===== */

@JsonClass(generateAdapter = true)
data class BatchResultDto(
    val batchId: Long,
    val category: String,
    val total: Int,
    val steps: List<StepDto>
)

@JsonClass(generateAdapter = true)
data class BatchGetResultDto(
    val batchId: Long,
    val total: Int,
    val steps: List<StepDto>
)

@JsonClass(generateAdapter = true)
data class SubmitResultDto(
    val isCorrect: Boolean?
)

/* ===== 날짜 요약 (옵션) ===== */
@JsonClass(generateAdapter = true)
data class DailySummaryRowDto(
    val user_id: Int,
    val ymd: String,            // "YYYY-MM-DD"
    val category: String,       // JOB_PREP | BASIC | PRACTICE | DEEP | ADVANCED
    val answered: Int,
    val correct: Int,
    @Json(name = "accuracy_pct") val accuracyPct: Double
)

/* ===== 스텝 DTO: 서버가 type별로 필드가 달라서 union으로 수신 ===== */
@JsonClass(generateAdapter = true)
data class StepDto(
    val index: Int? = null,
    val type: String, // "MCQ" | "OX" | "SHORT"

    // MCQ
    val text: String? = null,
    @Json(name = "options") val options: List<QuizOptionDto>? = null,
    val correctOptionId: Int? = null,

    // OX
    val statement: String? = null,
    @Json(name = "answerIsO") val answerIsO: Boolean? = null,

    // SHORT
    val guide: String? = null,
    val sentence: String? = null,
    val underlineText: String? = null,
    val answerText: String? = null,

    // 공통
    val explanation: String? = null
)

@JsonClass(generateAdapter = true)
data class QuizOptionDto(
    val id: Int,
    val label: String
)