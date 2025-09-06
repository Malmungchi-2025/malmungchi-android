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

    // ✅ 내 정보
    @GET("/api/auth/me")
    suspend fun me(): com.malmungchi.core.model.BaseResponse<UserDto>

    // ==== DEV OTP (서버에 방금 추가한 엔드포인트) ====
    @POST("/api/auth/dev/request-otp")
    suspend fun requestOtp(@Body body: ResendRequest): BaseResponse<Unit>

    @POST("/api/auth/dev/verify-otp")
    suspend fun verifyOtp(@Body body: Map<String, String>): BaseResponse<Unit>


    // ✅ 최신 저장 단어 5개 (includeId=1, includeLiked=1 권장)
    @GET("/api/auth/me/vocabulary/recent")
    suspend fun getMyRecentVocabulary(
        @Query("limit") limit: Int = 5,
        @Query("includeId") includeId: Int = 1,
        @Query("includeLiked") includeLiked: Int = 1
    ): VocabListResponse

    // ✅ 전체 단어 목록 (커서 기반: id 또는 created_at)
    @GET("/api/auth/me/vocabulary")
    suspend fun getMyVocabulary(
        @Query("limit") limit: Int = 20,
        @Query("lastId") lastId: Int? = null,
        @Query("lastCreatedAt") lastCreatedAt: String? = null,
        @Query("includeId") includeId: Int = 1,
        @Query("includeLiked") includeLiked: Int = 1
    ): VocabListResponse

    // ✅ 좋아요 토글
    @PATCH("/api/auth/me/vocabulary/{vocabId}/like")
    suspend fun toggleMyVocabularyLike(
        @Path("vocabId") vocabId: Int,
        @Body body: Map<String, Boolean> // { "liked": true/false }
    ): ToggleLikeResponse

    // ✅ 좋아요 목록 (커서 기반)
    @GET("/api/auth/me/vocabulary/liked")
    suspend fun getMyLikedVocabulary(
        @Query("limit") limit: Int = 20,
        @Query("lastId") lastId: Int? = null,
        @Query("lastCreatedAt") lastCreatedAt: String? = null,
        @Query("includeId") includeId: Int = 1,
        @Query("includeLiked") includeLiked: Int = 1
    ): VocabListResponse

    @POST("/api/auth/me/nickname-test/result")
    suspend fun saveNicknameUsersOnly(
        @Body body: NicknameUsersOnlyReq
    ): BaseResponse<SaveNicknameResult>
}