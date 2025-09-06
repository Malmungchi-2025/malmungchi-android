package com.malmungchi.core.model

data class BaseResponse<T>(
    val success: Boolean,
    val message: String?,
    val user: T? = null,      // 어떤 API는 user 루트 사용
    val result: T? = null,    // 어떤 API는 result 루트 사용
    val token: String? = null // 로그인 응답 등
)
//data class BaseResponse<T>(
//    val success: Boolean,
//    val message: String?,
//    val user: T? = null,         // 일부 API는 user 루트에 담아옴
//    val token: String? = null    // 로그인 응답에 포함
//)

data class UserDto(
    val id: Int,
    val email: String,
    val name: String,
    val nickname: String?,
    val is_verified: Boolean,
    val level: Int ? = null         // ✅ 서버가 내려주는 1~4
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
    val user: UserDto?,            // ✅ user.level 포함
    val message: String? = null
)

data class VocabularyDto(
    val id: Int? = null,            // includeId=1일 때만 옴
    val word: String,
    val meaning: String,
    val example: String?,
    val isLiked: Boolean? = null    // includeLiked=1일 때만 옴
)

data class NextCursor(
    val lastId: Int? = null,
    val lastCreatedAt: String? = null
)

data class VocabListResponse(
    val success: Boolean,
    val message: String?,
    val result: List<VocabularyDto>?,
    val nextCursor: NextCursor? = null
)

data class ToggleLikeResult(
    val id: Int,
    val isLiked: Boolean
)

data class ToggleLikeResponse(
    val success: Boolean,
    val message: String?,
    val result: ToggleLikeResult?
)


data class ResendRequest(val email: String)