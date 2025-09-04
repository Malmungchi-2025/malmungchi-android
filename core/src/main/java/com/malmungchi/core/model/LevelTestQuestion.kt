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
