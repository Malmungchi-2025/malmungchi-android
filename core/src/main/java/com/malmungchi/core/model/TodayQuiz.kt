package com.malmungchi.core.model


//퀴즈 요청,응답 dto
data class QuizGenerationRequest(val text: String, val studyId: Int)


data class QuizItem(
    val questionIndex: Int,
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
    // ↓ 서버가 userChoice, isCorrect로 내려줌 (camelCase)
    val userChoice: String? = null,
    val isCorrect: Boolean? = null
)

data class QuizAnswerRequest(
    val studyId: Int,
    val questionIndex: Int,
    val userChoice: String,
    val isCorrect: Boolean
)