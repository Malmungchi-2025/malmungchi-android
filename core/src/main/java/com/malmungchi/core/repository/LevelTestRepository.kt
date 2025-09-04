package com.malmungchi.core.repository

import com.malmungchi.core.model.LevelTestQuestion
import com.malmungchi.core.model.LevelTestSubmitAnswer
import com.malmungchi.core.model.LevelTestSubmitResult

interface LevelTestRepository {
    suspend fun generateLevelTest(stage: Int): Result<List<LevelTestQuestion>>
    suspend fun submitLevelTest(answers: List<LevelTestSubmitAnswer>): Result<LevelTestSubmitResult>
}