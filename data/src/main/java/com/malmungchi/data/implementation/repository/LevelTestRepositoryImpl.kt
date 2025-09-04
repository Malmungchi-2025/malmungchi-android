package com.malmungchi.data.implementation.repository

import com.malmungchi.core.repository.LevelTestRepository
import com.malmungchi.core.model.LevelTestQuestion
import com.malmungchi.core.model.LevelTestSubmitAnswer
import com.malmungchi.core.model.LevelTestSubmitResult
import com.malmungchi.data.api.LevelTestApi
import com.malmungchi.data.api.dto.GenerateLevelTestRequest
import com.malmungchi.data.api.dto.SubmitLevelTestRequest
import com.malmungchi.data.mapper.toCore
import com.malmungchi.data.mapper.toDto

class LevelTestRepositoryImpl(
    private val api: LevelTestApi
) : LevelTestRepository {

    override suspend fun generateLevelTest(stage: Int): Result<List<LevelTestQuestion>> =
        runCatching {
            val res = api.generateLevelTest(GenerateLevelTestRequest(stage))
            if (!res.success) throw IllegalStateException(res.message ?: "레벨 테스트 생성 실패")
            res.result?.map { it.toCore() } ?: emptyList()
        }

    override suspend fun submitLevelTest(answers: List<LevelTestSubmitAnswer>): Result<LevelTestSubmitResult> =
        runCatching {
            val res = api.submitLevelTest(
                SubmitLevelTestRequest(answers.map { it.toDto() })
            )
            if (!res.success) throw IllegalStateException("레벨 테스트 제출 실패")
            // 서버: { success, correctCount, resultLevel, message }
            LevelTestSubmitResult(
                correctCount = res.correctCount,
                resultLevel  = res.resultLevel,
                message      = res.message ?: "레벨 테스트 채점 완료"
            )
        }
}