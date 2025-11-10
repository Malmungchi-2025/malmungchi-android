package com.malmungchi.data.api

// 백엔드 공통 응답 포맷과 맞춤
data class ApiResponse<T>(
    val success: Boolean,
    val result: T?,
    val studyId: Int?,     // generate-quote에서 같이 내려옴
    val message: String?
)