package com.malmungchi.data.implementation.repository

import com.malmungchi.core.model.LevelSubmitDetail
import com.malmungchi.core.repository.LevelTestRepository
import com.malmungchi.core.model.LevelTestQuestion
import com.malmungchi.core.model.LevelTestSubmitAnswer
import com.malmungchi.core.model.LevelTestSubmitResult
import com.malmungchi.core.model.LevelsSubmitResult
import com.malmungchi.data.api.LevelTestApi
import com.malmungchi.data.api.dto.GenerateLevelTestRequest
import com.malmungchi.data.api.dto.LevelsGenerateRequest
import com.malmungchi.data.api.dto.LevelsStartRequest
import com.malmungchi.data.api.dto.LevelsSubmitRequest
import com.malmungchi.data.api.dto.Question
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
                resultLevel = res.resultLevel,
                message = res.message ?: "레벨 테스트 채점 완료"
            )
        }

    // 간단 메모리 캐시: stage -> 원본 질문(정답Index/해설 포함)
    private val lastGeneratedWire: MutableMap<Int, List<Question>> = mutableMapOf()

    override suspend fun levelsStart(stage: Int): Result<Unit> =
        runCatching {
            val res = api.levelsStart(LevelsStartRequest(stage))
            if (!res.success) throw IllegalStateException(res.message ?: "시작 실패")
            // 시작 시 이전 캐시도 비워서 안전하게
            lastGeneratedWire.remove(stage)
            Unit
        }


    override suspend fun levelsGenerate(stage: Int): Result<com.malmungchi.core.model.LevelsGenerateResult> =
        runCatching {
            val res = api.levelsGenerate(LevelsGenerateRequest(stage))
            if (!res.success) throw IllegalStateException(res.message ?: "문제 생성 실패")

            val wire = res.questions ?: emptyList()
            // ⛳ 서버 원본을 캐시에 보관 (submit 때 그대로 보냄)
            lastGeneratedWire[stage] = wire

            com.malmungchi.core.model.LevelsGenerateResult(
                passage = res.passage.orEmpty(),
                questions = wire.map { it.toCore() }   // UI는 핵심 필드만
            )
        }
    override suspend fun levelsSubmit(
        stage: Int,
        questions: List<LevelTestQuestion>,
        answers: List<Int>
    ): Result<LevelsSubmitResult> =
        runCatching {
            val wire = lastGeneratedWire[stage]
                ?: throw IllegalStateException("생성 이력이 없습니다. 먼저 levelsGenerate(stage=$stage)를 호출하세요.")

            val res = api.levelsSubmit(
                LevelsSubmitRequest(
                    stage = stage,
                    questions = wire,   // generate때 받은 원본
                    answers = answers
                )
            )
            if (!res.success) throw IllegalStateException("레벨 테스트 제출 실패")

            // (선택) 캐시 정리
            lastGeneratedWire.remove(stage)

            // ⬇️ 서버 DTO → core 모델로 매핑
            LevelsSubmitResult(
                correctCount = res.correctCount,
                resultLevel  = res.resultLevel,
                detail = res.detail?.map { d ->
                    LevelSubmitDetail(
                        questionIndex = d.questionIndex,
                        isCorrect     = d.isCorrect,
                        answerIndex   = d.answerIndex,
                        userChoice    = d.userChoice,
                        explanation   = d.explanation
                    )
                }
            )
        }





}