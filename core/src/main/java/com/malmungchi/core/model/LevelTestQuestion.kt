package com.malmungchi.core.model

data class LevelTestQuestion(
    val questionIndex: Int?,   // 1~15 (서버에 없을 수도 있으니 nullable)
    val question: String,
    val options: List<String>
    // answer는 클라에서 필요 없음 (서버 채점)
)

data class LevelTestSubmitAnswer(
    val questionIndex: Int,
    val choice: String
)

data class LevelTestSubmitResult(
    val correctCount: Int,
    val resultLevel: String,
    val message: String?
)


data class LevelsSubmitResult(
    val correctCount: Int,
    val resultLevel: String,
    val detail: List<LevelSubmitDetail>? = null  // 👈 추가
)

data class LevelSubmitDetail(
    val questionIndex: Int,
    val isCorrect: Boolean,
    val answerIndex: Int,
    val userChoice: Int,
    val explanation: String?
)


/** 🔵 신규 3문항 generate 응답용 (passage 포함) */
data class LevelsGenerateResult(
    val passage: String,
    val questions: List<LevelTestQuestion>
)


