package com.malmungchi.data.mapper


import com.malmungchi.core.model.LevelTestQuestion
import com.malmungchi.core.model.LevelTestSubmitAnswer
import com.malmungchi.core.model.LevelTestSubmitResult
import com.malmungchi.data.api.dto.Question as DtoQuestion
import com.malmungchi.data.api.dto.SubmitAnswer as DtoSubmitAnswer
import com.malmungchi.data.api.dto.SubmitLevelTestResult as DtoSubmitResult

fun DtoQuestion.toCore(): LevelTestQuestion =
    LevelTestQuestion(
        questionIndex = questionIndex,
        question = question,
        options = options
    )

fun LevelTestSubmitAnswer.toDto(): DtoSubmitAnswer =
    DtoSubmitAnswer(
        questionIndex = questionIndex,
        choice = choice
    )

fun DtoSubmitResult.toCore(): LevelTestSubmitResult =
    LevelTestSubmitResult(
        correctCount = correctCount,
        resultLevel = resultLevel,
        message = message
    )