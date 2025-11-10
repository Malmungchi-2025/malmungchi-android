package com.malmungchi.data.api.dto


import com.google.gson.annotations.SerializedName

// 이미 존재하는 DTO들 가정
data class GenerateLevelTestRequest(val stage: Int)

data class Question(
    val questionIndex: Int? = null,
    val question: String,
    val options: List<String>,
    val answer: String? = null,          // 구(15문항) 경로용
    val answerIndex: Int? = null,        // 🔵 신규(3문항) 경로용
    val explanation: String? = null      // 🔵 신규(3문항) 경로용
)

data class SubmitAnswer(val questionIndex: Int, val choice: String)
data class SubmitLevelTestRequest(val answers: List<SubmitAnswer>)

data class SubmitLevelTestResult(
    val correctCount: Int,
    val resultLevel: String,
    val message: String?
)

/** 서버의 /submit 응답 스키마와 1:1 매칭 */
data class LevelTestSubmitResponse(
    val success: Boolean,
    val correctCount: Int,
    val resultLevel: String,
    val message: String?
)

/** 공통 래퍼 (이미 다른 곳에서도 쓰고 있다면 생략) */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val result: T?
)
data class LevelsStartRequest(
    val stage: Int
)

data class LevelsGenerateRequest(
    val stage: Int
)

/** 서버 응답: { success, passage, questions[] } */
data class LevelsGenerateResponse(
    val success: Boolean,
    val passage: String?,
    val questions: List<Question>?,
    val message: String? = null
)

/** 서버 요청: { stage, questions(원본 그대로), answers([0..3]) } */
data class LevelsSubmitRequest(
    val stage: Int,
    val questions: List<Question>,
    val answers: List<Int>
)

data class LevelsSubmitResponseDto(
    val success: Boolean,
    val correctCount: Int,
    val resultLevel: String,
    val message: String?,
    val detail: List<LevelSubmitDetailDto>?
)

data class LevelSubmitDetailDto(
    val questionIndex: Int,
    val isCorrect: Boolean,
    val answerIndex: Int,
    val userChoice: Int,
    val explanation: String?
)

///* ─────────── 🔵 신규: /api/gpt/levels/* 전용 DTO ─────────── */
//
//
//
//
///**
// * ✅ (중요) /levels/generate 가 돌려주는 원문을 그대로 보관/재전송하려면
// * answerIndex, explanation 이 필요함. (옵션으로 추가)
// * 기존 15문항 경로(/level-test/*)도 영향 없이 동작.
//*/
// */
//
//
////
////// 이미 존재하는 DTO들 가정
////data class GenerateLevelTestRequest(val stage: Int)
////data class Question(
////    val questionIndex: Int? = null,
////    val question: String,
////    val options: List<String>,
////    val answer: String? = null
////)
////data class SubmitAnswer(val questionIndex: Int, val choice: String)
////data class SubmitLevelTestRequest(val answers: List<SubmitAnswer>)
////data class SubmitLevelTestResult(
////    val correctCount: Int,
////    val resultLevel: String,
////    val message: String?
////)
////
/////** 서버의 /submit 응답 스키마와 1:1 매칭 */
////data class LevelTestSubmitResponse(
////    val success: Boolean,
////    val correctCount: Int,
////    val resultLevel: String,
////    val message: String?
////)
//
//
//
////data class GenerateLevelTestRequest(
////    @SerializedName("stage") val stage: Int // 0~3
////)
////
////data class Question(
////    @SerializedName("questionIndex") val questionIndex: Int? = null,
////    @SerializedName("question") val question: String,
////    @SerializedName("options") val options: List<String>,
////    // 서버 저장용 필드라 클라에서 안 써도 됨
////    @SerializedName("answer") val answer: String? = null
////)
////
////// submit
////data class SubmitAnswer(
////    @SerializedName("questionIndex") val questionIndex: Int,
////    @SerializedName("choice") val choice: String
////)
////
////data class SubmitLevelTestRequest(
////    @SerializedName("answers") val answers: List<SubmitAnswer>
////)
////
////data class SubmitLevelTestResult(
////    @SerializedName("correctCount") val correctCount: Int,
////    @SerializedName("resultLevel") val resultLevel: String,
////    @SerializedName("message") val message: String?
////)
