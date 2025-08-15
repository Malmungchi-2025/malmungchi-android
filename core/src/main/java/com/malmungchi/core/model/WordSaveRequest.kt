package com.malmungchi.core.model

import com.google.gson.annotations.SerializedName

// 단어 저장 요청(서버는 study_id 스네이크 케이스 요구)
data class WordSaveRequest(
    @JvmField val study_id: Int,
    @JvmField val word: String,
    @JvmField val meaning: String,
    @JvmField val example: String? = null
)

data class HandwritingRequest(
    @JvmField val study_id: Int,
    @JvmField val content: String)



data class WordRequest(val word: String)