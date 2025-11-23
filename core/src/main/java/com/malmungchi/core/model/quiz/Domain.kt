package com.malmungchi.core.model.quiz

enum class QuizCategory(val displayName: String) {
    JOB_PREP("취업 준비"), BASIC("기초"), PRACTICE("활용"), DEEP("심화"), ADVANCED("고급")
}
enum class QuizType { MCQ, OX, SHORT }

data class QuizSet(val id: String, val category: QuizCategory, val steps: List<QuizStep>) {
    val total get() = steps.size
}

sealed interface QuizStep { val id: String; val type: QuizType }
data class QuizOption(val id: Int, val label: String)

data class McqStep(
    override val id: String,
    val text: String,
    val options: List<QuizOption>,
    val correctOptionId: Int?,
    val explanation: String? = null,
    // 새로 추가된 필드 — 특정 단어 밑줄 강조용
    val underline: String? = null
) : QuizStep { override val type = QuizType.MCQ }

data class OxStep(
    override val id: String,
    val statement: String,
    val answerIsO: Boolean?,
    val explanation: String? = null
) : QuizStep { override val type = QuizType.OX }

data class ShortStep(
    override val id: String,
    val guide: String,
    val sentence: String,
    val underlineText: String?,
    val answerText: String?,
    val explanation: String? = null
) : QuizStep { override val type = QuizType.SHORT }

/* 제출 페이로드 */
data class Submission(
    val questionId: String,
    val type: QuizType,
    val selectedOptionId: Int? = null,
    val selectedIsO: Boolean? = null,
    val text: String? = null
)

