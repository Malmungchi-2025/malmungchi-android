package com.malmungchi.data.api.dto

data class QuoteResponse(
    val success: Boolean,
    val message: String? = null,
    val result: String? = null, // 본문
    val studyId: Int? = null    // 오늘 학습 ID
)