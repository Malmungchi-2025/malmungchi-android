package com.malmungchi.data.implementation.repository

import com.malmungchi.core.model.quiz.McqStep
import com.malmungchi.core.model.quiz.OxStep
import com.malmungchi.core.model.quiz.QuizCategory
import com.malmungchi.core.model.quiz.QuizOption
import com.malmungchi.core.model.quiz.QuizSet
import com.malmungchi.core.model.quiz.QuizStep
import com.malmungchi.core.model.quiz.ShortStep
import com.malmungchi.core.model.quiz.Submission
import com.malmungchi.core.repository.QuizRepository
import com.malmungchi.data.api.QuizApi
import com.malmungchi.data.api.dto.*


class QuizRepositoryImpl(
    private val api: QuizApi
) : QuizRepository {

    override suspend fun createBatch(categoryKor: String, len: Int?): QuizSet {
        val res = api.createBatch(CreateBatchBody(categoryKor, len))
        require(res.success && res.result != null) { res.message ?: "생성 실패" }
        return res.result!!.toDomain()
    }

    override suspend fun getBatch(batchId: Long): QuizSet {
        val res = api.getBatch(batchId)
        require(res.success && res.result != null) { res.message ?: "조회 실패" }
        return res.result!!.toDomain()
    }

    override suspend fun submit(
        batchId: Long,
        questionIndex: Int,
        submission: Submission
    ): Boolean? {
        val payload = SubmitPayloadDto(
            selectedOptionId = submission.selectedOptionId,
            selectedIsO = submission.selectedIsO,
            textAnswer = submission.text
        )
        val res = api.submit(SubmitBody(batchId, questionIndex, payload))
        require(res.success && res.result != null) { res.message ?: "제출 실패" }
        return res.result!!.isCorrect
    }
}

/* ===== Mapper: DTO -> Domain ===== */

private fun BatchResultDto.toDomain(): QuizSet = QuizSet(
    id = batchId.toString(),
    category = toCategory(category),
    steps = steps.mapIndexed { i, s -> s.toDomain(i) }
)

private fun BatchGetResultDto.toDomain(): QuizSet = QuizSet(
    id = batchId.toString(),
    category = QuizCategory.BASIC, // 서버 응답엔 category 생략될 수 있어 기본값
    steps = steps.mapIndexed { i, s -> s.toDomain(i) }
)

private fun StepDto.toDomain(idx: Int): QuizStep = when (type.uppercase()) {
    "MCQ" -> McqStep(
        id = (index ?: (idx + 1)).toString(),
        text = text.orEmpty(),
        options = (options ?: emptyList()).map { QuizOption(it.id, it.label) },
        correctOptionId = correctOptionId,
        explanation = explanation                 // ★ 전달
    )
    "OX" -> OxStep(
        id = (index ?: (idx + 1)).toString(),
        statement = statement.orEmpty(),
        answerIsO = answerIsO,
        explanation = explanation                 // ★ 전달
    )
    else -> ShortStep(
        id = (index ?: (idx + 1)).toString(),
        guide = guide.orEmpty(),
        sentence = sentence.orEmpty(),
        underlineText = underlineText,
        answerText = answerText,
        explanation = explanation                 // ★ 전달
    )
}

private fun toCategory(code: String): QuizCategory = when (code) {
    "JOB_PREP" -> QuizCategory.JOB_PREP
    "BASIC" -> QuizCategory.BASIC
    "PRACTICE" -> QuizCategory.PRACTICE
    "DEEP" -> QuizCategory.DEEP
    "ADVANCED" -> QuizCategory.ADVANCED
    else -> QuizCategory.BASIC
}