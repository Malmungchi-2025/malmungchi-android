package com.malmungchi.data.api.dto

data class QuoteResponse(
    val success: Boolean,
    val message: String? = null,
    val result: String? = null,
    val studyId: Int? = null,
    val level: Int? = null        // ✅ 서버가 내려주는 레벨(기존/신규 모두)
)