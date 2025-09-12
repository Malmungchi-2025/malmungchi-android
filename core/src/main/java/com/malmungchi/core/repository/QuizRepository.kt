package com.malmungchi.core.repository

import com.malmungchi.core.model.quiz.QuizSet
import com.malmungchi.core.model.quiz.Submission


interface QuizRepository {
    suspend fun createBatch(categoryKor: String, len: Int? = 80): QuizSet
    suspend fun getBatch(batchId: Long): QuizSet
    suspend fun submit(
        batchId: Long,
        questionIndex: Int,
        submission: Submission
    ): Boolean? // isCorrect (nullable)
}