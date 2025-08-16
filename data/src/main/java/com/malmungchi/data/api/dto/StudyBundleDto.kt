package com.malmungchi.data.api.dto

data class StudyBundleDto(
    val studyId: Int,
    val date: String,             // "YYYY-MM-DD"
    val content: String,
    val handwriting: String,
    val vocabulary: List<VocabDto>,
    val quizzes: List<QuizDto>
)

data class VocabDto(
    val word: String,
    val meaning: String,
    val example: String?
)

data class QuizDto(
    val questionIndex: Int,
    val type: String?,            // 서버에서 type 내려옴 (null 가능)
    val question: String,
    val options: List<String>,    // 서버는 jsonb 배열 -> List<String>으로 매핑
    val answer: String,
    val explanation: String?,
    val userChoice: String?,      // 사용자가 고른 보기 (없을 수 있음)
    val isCorrect: Boolean?       // 정답 여부 (없을 수 있음)
)