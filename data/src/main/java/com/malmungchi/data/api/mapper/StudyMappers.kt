package com.malmungchi.data.api.mapper

import com.malmungchi.core.model.QuizItem
import com.malmungchi.core.model.StudyBundle
import com.malmungchi.core.model.WordItem
import com.malmungchi.data.api.dto.QuizDto
import com.malmungchi.data.api.dto.StudyBundleDto
import com.malmungchi.data.api.dto.VocabDto

fun StudyBundleDto.toDomain(): StudyBundle =
    StudyBundle(
        studyId = studyId,
        date = date,
        content = content,
        handwriting = handwriting,
        vocabulary = vocabulary.map { it.toDomain() },
        quizzes = quizzes.map { it.toDomain() }
    )

fun VocabDto.toDomain(): WordItem =
    WordItem(
        word = word,
        meaning = meaning,
        example = example
    )

/**
 * core의 QuizItem 생성자에 `type` 파라미터가 없고,
 * 일부 필드가 non-null 인 경우를 안전하게 매핑
 */
fun QuizDto.toDomain(): QuizItem =
    QuizItem(
        questionIndex = questionIndex,
        question = question,
        options = options,
        answer = answer,
        explanation = explanation ?: "",   // 🔧 non-null 보정
        userChoice = userChoice,
        isCorrect = isCorrect
    )