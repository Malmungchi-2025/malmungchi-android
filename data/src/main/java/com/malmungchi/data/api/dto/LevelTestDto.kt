package com.malmungchi.data.api.dto


import com.google.gson.annotations.SerializedName


// 이미 존재하는 DTO들 가정
data class GenerateLevelTestRequest(val stage: Int)
data class Question(
    val questionIndex: Int? = null,
    val question: String,
    val options: List<String>,
    val answer: String? = null
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
//data class GenerateLevelTestRequest(
//    @SerializedName("stage") val stage: Int // 0~3
//)
//
//data class Question(
//    @SerializedName("questionIndex") val questionIndex: Int? = null,
//    @SerializedName("question") val question: String,
//    @SerializedName("options") val options: List<String>,
//    // 서버 저장용 필드라 클라에서 안 써도 됨
//    @SerializedName("answer") val answer: String? = null
//)
//
//// submit
//data class SubmitAnswer(
//    @SerializedName("questionIndex") val questionIndex: Int,
//    @SerializedName("choice") val choice: String
//)
//
//data class SubmitLevelTestRequest(
//    @SerializedName("answers") val answers: List<SubmitAnswer>
//)
//
//data class SubmitLevelTestResult(
//    @SerializedName("correctCount") val correctCount: Int,
//    @SerializedName("resultLevel") val resultLevel: String,
//    @SerializedName("message") val message: String?
//)

