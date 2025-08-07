package com.malmungchi.core.model

//퀴즈 요청,응답 dto
data class QuizGenerationRequest(val text: String, val studyId: Int)

data class QuizItem(
    val questionIndex: Int,
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String
)

data class QuizAnswerRequest(
    val studyId: Int,
    val questionIndex: Int,
    val userChoice: String,
    val isCorrect: Boolean
)