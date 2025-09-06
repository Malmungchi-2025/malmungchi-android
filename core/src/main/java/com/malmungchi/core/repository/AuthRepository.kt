package com.malmungchi.core.repository
import com.malmungchi.core.model.*

interface AuthRepository {
    suspend fun devRequestOtp(email: String): Boolean
    suspend fun devVerifyOtp(email: String, code: String): Boolean

    suspend fun register(email: String, password: String, name: String, nickname: String? = null): RegisterResponse
    suspend fun resendVerification(email: String): Boolean
    suspend fun login(email: String, password: String): LoginResponse
    suspend fun me(): UserDto?

    // ✅ 마이페이지 추가
    suspend fun getMyRecentVocabulary(limit: Int = 5): Pair<List<VocabularyDto>, NextCursor?>
    suspend fun getMyVocabulary(
        limit: Int = 20,
        lastId: Int? = null,
        lastCreatedAt: String? = null
    ): Pair<List<VocabularyDto>, NextCursor?>

    suspend fun toggleMyVocabularyLike(vocabId: Int, liked: Boolean): ToggleLikeResult
    suspend fun getMyLikedVocabulary(
        limit: Int = 20,
        lastId: Int? = null,
        lastCreatedAt: String? = null
    ): Pair<List<VocabularyDto>, NextCursor?>
}