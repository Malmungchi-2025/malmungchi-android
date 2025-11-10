package com.malmungchi.core.model


data class TodayQuote(
    val content: String,
    val studyId: Int,
    val level: Int? = null         // ✅ 응답 레벨 표시/동기화용(선택)
)

