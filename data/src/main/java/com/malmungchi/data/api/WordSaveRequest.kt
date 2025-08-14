package com.malmungchi.data.api


data class WordSaveRequest(
    val study_id: Int,
    val word: String,
    val meaning: String,
    val example: String?
)