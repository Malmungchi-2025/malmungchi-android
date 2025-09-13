package com.malmungchi.data.implementation.repository

import com.malmungchi.core.model.*
import com.malmungchi.core.repository.AuthRepository
import com.malmungchi.data.api.AuthService
import com.malmungchi.data.session.SessionManager
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthService
) : AuthRepository {

    override suspend fun devRequestOtp(email: String): Boolean {
        val r = api.requestOtp(ResendRequest(email))
        return r.success
    }

    override suspend fun devVerifyOtp(email: String, code: String): Boolean {
        val r = api.verifyOtp(mapOf("email" to email, "code" to code))
        return r.success
    }

    override suspend fun register(email: String, password: String, name: String, nickname: String?): RegisterResponse {
        return api.register(RegisterRequest(email, password, name, nickname))
    }

    override suspend fun resendVerification(email: String): Boolean {
        val r = api.resendVerification(ResendRequest(email))
        return r.success
    }

    override suspend fun login(email: String, password: String): LoginResponse {
        val res = api.login(LoginRequest(email, password))
        if (res.success) {
            // ❗️스마트 캐스트 회피: 로컬 변수에 담아서 사용
            val user: UserDto? = res.user
            if (user != null) {
                SessionManager.set(
                    userId = user.id,
                    token  = res.token.orEmpty(),
                    level  = user.level               // ✅ 새 파라미터
                )
            }
        }
        return res
    }

    override suspend fun me(): UserDto? {
        val r = api.me()
        return if (r.success) {
            val user = r.result
            if (user != null) {
                SessionManager.updateLevel(user.level)   // ✅ 새 메서드
                SessionManager.updateNicknameTitle(user.nickname_title)
            }
            user
        } else null
    }

    // ===== 마이페이지 =====

    override suspend fun getMyRecentVocabulary(limit: Int): Pair<List<VocabularyDto>, NextCursor?> {
        val r = api.getMyRecentVocabulary(limit = limit, includeId = 1, includeLiked = 1)
        if (!r.success) return emptyList<VocabularyDto>() to null
        return (r.result ?: emptyList()) to r.nextCursor
    }

    override suspend fun getMyVocabulary(
        limit: Int,
        lastId: Int?,
        lastCreatedAt: String?
    ): Pair<List<VocabularyDto>, NextCursor?> {
        val r = api.getMyVocabulary(
            limit = limit,
            lastId = lastId,
            lastCreatedAt = lastCreatedAt,
            includeId = 1,
            includeLiked = 1
        )
        if (!r.success) return emptyList<VocabularyDto>() to null
        return (r.result ?: emptyList()) to r.nextCursor
    }

    override suspend fun toggleMyVocabularyLike(vocabId: Int, liked: Boolean): ToggleLikeResult {
        val r = api.toggleMyVocabularyLike(vocabId, mapOf("liked" to liked))
        val res = r.result ?: error(r.message ?: "토글 실패")
        return res
    }

    override suspend fun getMyLikedVocabulary(
        limit: Int,
        lastId: Int?,
        lastCreatedAt: String?
    ): Pair<List<VocabularyDto>, NextCursor?> {
        val r = api.getMyLikedVocabulary(
            limit = limit,
            lastId = lastId,
            lastCreatedAt = lastCreatedAt,
            includeId = 1,
            includeLiked = 1
        )
        if (!r.success) return emptyList<VocabularyDto>() to null
        return (r.result ?: emptyList()) to r.nextCursor
    }

    override suspend fun saveNicknameResult(
        nicknameTitle: String?,
        vocabCorrect: Int,
        readingCorrect: Int
    ): SaveNicknameResult {
        val resp = api.saveNicknameUsersOnly(
            NicknameUsersOnlyReq(
                nicknameTitle = nicknameTitle,
                vocabCorrect = vocabCorrect,
                readingCorrect = readingCorrect
            )
        )
        if (!resp.success) error(resp.message ?: "별명 저장 실패")
        return resp.result ?: error("빈 응답")
    }
}
//class AuthRepositoryImpl @Inject constructor(   // 👈 @Inject 추가
//    private val api: AuthService
//) : AuthRepository {
//
//    override suspend fun devRequestOtp(email: String): Boolean {
//        val r = api.requestOtp(ResendRequest(email))
//        return r.success
//    }
//
//    override suspend fun devVerifyOtp(email: String, code: String): Boolean {
//        val r = api.verifyOtp(mapOf("email" to email, "code" to code))
//        return r.success
//    }
//
//    override suspend fun register(email: String, password: String, name: String, nickname: String?): RegisterResponse {
//        return api.register(RegisterRequest(email, password, name, nickname))
//    }
//
//    override suspend fun resendVerification(email: String): Boolean {
//        val r = api.resendVerification(ResendRequest(email))
//        return r.success
//    }
//
//    override suspend fun login(email: String, password: String): LoginResponse {
//        return api.login(LoginRequest(email, password))
//    }
//
//    override suspend fun me(): UserDto? {
//        val r = api.me()
//        return if (r.success) r.result else null
//    }
//}