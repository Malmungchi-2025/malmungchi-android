package com.malmungchi.core.model

data class StudyBundle(
    val studyId: Int,
    val date: String,          // "YYYY-MM-DD"
    val content: String,
    val handwriting: String,
    val vocabulary: List<WordItem>,
    val quizzes: List<QuizItem>
)