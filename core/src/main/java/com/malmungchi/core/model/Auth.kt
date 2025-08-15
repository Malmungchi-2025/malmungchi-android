package com.malmungchi.core.model


data class BaseResponse<T>(
    val success: Boolean,
    val message: String?,
    val user: T? = null,         // 일부 API는 user 루트에 담아옴
    val token: String? = null    // 로그인 응답에 포함
)

data class UserDto(
    val id: Long,             // ← 서버/DB id가 int라면 Long도 OK (안전)
    val email: String,
    val name: String,
    val nickname: String?,
    val is_verified: Boolean
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val nickname: String? = null
)
data class RegisterResponse(
    val success: Boolean,
    val message: String?,
    val user: UserDto?
)



data class LoginRequest(val email: String, val password: String)
data class LoginResponse(
    val success: Boolean,
    val token: String?,
    val user: UserDto?,       // 서버가 user 포함해 주는 형태
    val message: String? = null
)


data class ResendRequest(val email: String)