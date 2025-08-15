package com.malmungchi.data.api


import com.malmungchi.core.model.*
import com.malmungchi.data.api.dto.BaseResponse
import retrofit2.http.*

interface AuthService {
    @POST("/api/auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @GET("/api/auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): BaseResponse<UserDto> // 필요 시 사용

    @POST("/api/auth/resend-verification")
    suspend fun resendVerification(@Body body: ResendRequest): BaseResponse<Unit>

    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    // ✅ 변경:
    @GET("/api/auth/me")
    suspend fun me(): BaseResponse<UserDto>

    // ==== DEV OTP (서버에 방금 추가한 엔드포인트) ====
    @POST("/api/auth/dev/request-otp")
    suspend fun requestOtp(@Body body: ResendRequest): BaseResponse<Unit>

    @POST("/api/auth/dev/verify-otp")
    suspend fun verifyOtp(@Body body: Map<String, String>): BaseResponse<Unit>
}