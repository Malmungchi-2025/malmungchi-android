package com.malmungchi.data.implementation.repository

import com.malmungchi.core.model.*
import com.malmungchi.core.repository.AuthRepository
import com.malmungchi.data.api.AuthService
import com.malmungchi.data.preference.AuthPreference
import com.malmungchi.data.session.SessionManager
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthService,
    private val authPref: AuthPreference,   // ★ 주입
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
                // ✅ 선택: 친구코드/아바타 캐시
                //SessionManager.updateFriendCode(user.friend_code)
                //SessionManager.updateAvatarName(user.avatar_name)
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

    //로그아웃 구현
    override suspend fun logoutLocal() {
        authPref.clear()
        SessionManager.clear()  // ★ 없으면 간단히 만들어주세요
    }

    override suspend fun updateAvatarName(avatarName: String): Boolean {
        val resp = api.updateAvatar(mapOf("avatarName" to avatarName))
        if (!resp.success) {
            // 서버에서 validation 실패(400)나 기타 오류 메시지가 올 수 있으니 예외로 올려주면 상위(UI)에서 토스트/다이얼로그 처리하기 좋음
            error(resp.message ?: "아바타 저장 실패")
        }
        // 서버 저장 성공 → 로컬 세션도 즉시 갱신(초기 화면 렌더에 사용)
        //SessionManager.updateAvatarName(avatarName)
        return true
    }

    //배지
    override suspend fun getMyBadges(): Map<String, Boolean> {
        val resp = api.getMyBadges()
        if (!resp.success) error(resp.result ?: "배지 조회 실패")
        return resp.result ?: emptyMap()
    }

    // ✅ 대표 배지 로컬 저장
    override suspend fun saveLocalRepresentativeBadge(key: String) {
        authPref.saveRepresentativeBadge(key)
    }

    // ✅ 대표 배지 로컬 불러오기
    override suspend fun getLocalRepresentativeBadge(): String? {
        return authPref.getRepresentativeBadge()
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