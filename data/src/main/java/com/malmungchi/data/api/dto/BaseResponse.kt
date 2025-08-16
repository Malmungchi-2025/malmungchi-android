package com.malmungchi.data.api.dto


data class BaseResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val result: T? = null,
    val studyId: Int? = null
)