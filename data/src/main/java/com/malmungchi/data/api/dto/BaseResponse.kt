package com.malmungchi.data.api.dto

data class BaseResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val result: T? = null,
    val studyId: Int? = null // generate-quote 전용으로 studyId가 함께 옴
)