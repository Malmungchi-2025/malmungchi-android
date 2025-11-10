package com.malmungchi.core.repository

import com.malmungchi.core.model.LevelTestQuestion
import com.malmungchi.core.model.LevelTestSubmitAnswer
import com.malmungchi.core.model.LevelTestSubmitResult
import com.malmungchi.core.model.LevelsGenerateResult
import com.malmungchi.core.model.LevelsSubmitResult

interface LevelTestRepository {
    suspend fun generateLevelTest(stage: Int): Result<List<LevelTestQuestion>>
    suspend fun submitLevelTest(answers: List<LevelTestSubmitAnswer>): Result<LevelTestSubmitResult>

    // 🔵 (신규) 3문항 플로우(/api/gpt/levels/*)
    suspend fun levelsStart(stage: Int): Result<Unit>
    suspend fun levelsGenerate(stage: Int): Result<LevelsGenerateResult>
    suspend fun levelsSubmit(
        stage: Int,
        questions: List<LevelTestQuestion>,
        answers: List<Int>
    ): Result<LevelsSubmitResult>

}