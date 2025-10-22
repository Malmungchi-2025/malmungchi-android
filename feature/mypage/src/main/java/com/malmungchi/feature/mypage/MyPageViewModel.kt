
package com.malmungchi.feature.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.model.NextCursor
import com.malmungchi.core.model.UserDto
import com.malmungchi.core.model.VocabularyDto
import com.malmungchi.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//배지 3개 보여줌.
data class BadgeUi(
    val imageResName: String,
    val title: String,
    val unlocked: Boolean
)


// ✅ 메모리 캐시 (앱 프로세스 살아있는 동안만 유지)
object MyPageCache {
    var cachedUi: MyPageUiState? = null
}

// ─────────────────────────────── UI STATE ───────────────────────────────
data class MyPageUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val user: UserDto? = null,
    val recentVocab: List<VocabularyDto> = emptyList(),
    val likedVocab: List<VocabularyDto> = emptyList(),
    val allVocab: List<VocabularyDto> = emptyList(),
    val allCursor: NextCursor? = null,
    val likedCursor: NextCursor? = null,
    val togglingId: Int? = null,
) {
    val userName: String
        get() = when {
            !user?.nickname.isNullOrBlank() -> user!!.nickname!!.trim()
            !user?.name.isNullOrBlank() -> user!!.name!!.trim()
            else -> "사용자"
        }

    val avatarName: String
        get() = user?.avatar_name?.takeIf { it.isNotBlank() } ?: "img_malchi"

    val levelInt: Int get() = user?.level ?: 0

    val levelLabel: String get() = when (levelInt) {
        0 -> "입문"
        1 -> "기초"
        2 -> "활용"
        3 -> "심화"
        else -> "고급"
    }

    val point: Int get() = user?.point ?: 0

    private fun nextStageLabelAndTarget(): Pair<String, Int>? = when (levelInt) {
        0, 1 -> "활용" to 1350
        2 -> "심화" to 2700
        3 -> "고급" to 4050
        else -> null
    }

    val nextProgress: Float get() {
        val t = nextStageLabelAndTarget() ?: return 1f
        val target = t.second.toFloat().coerceAtLeast(1f)
        return (point / target).coerceIn(0f, 1f)
    }

    val nextStageUi: NextStageUi? get() {
        val pair = nextStageLabelAndTarget() ?: return null
        val (label, target) = pair
        val remain = (target - point).coerceAtLeast(0)
        return NextStageUi(
            currentLabel = levelLabel,
            nextLabel = label,
            target = target,
            currentPoint = point,
            remain = remain,
            progress = nextProgress
        )
    }

}

data class NextStageUi(
    val currentLabel: String,
    val nextLabel: String,
    val target: Int,
    val currentPoint: Int,
    val remain: Int,
    val progress: Float
)

// ─────────────────────────────── VIEWMODEL ───────────────────────────────
@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(MyPageUiState())
    val ui: StateFlow<MyPageUiState> = _ui

    //배지 3개 추가
    // 🔹 최근 획득한 배지 3개 저장용
    private val _recentBadges = MutableStateFlow<List<BadgeUi>>(emptyList())
    val recentBadges: StateFlow<List<BadgeUi>> = _recentBadges

    private var initialized = false // 최초 로드 여부

    // ✅ 최초 진입 시 캐시 복원 or 로드
    fun loadIfNeeded(forcePartialRefresh: Boolean = false) {
        // 1️⃣ 캐시가 있다면 우선 복원 (빠른 UI 표시용)
        MyPageCache.cachedUi?.let {
            _ui.value = it.copy(loading = false)
        }

        // 2️⃣ 이미 초기화된 상태라도,
        // 포인트나 단어 등은 일정 간격(또는 조건)에 따라 갱신해줄 수 있도록 설정
        if (initialized && !forcePartialRefresh) return

        // 3️⃣ 최초 or 강제 새로고침 시 서버 호출
        initialized = true
        load()
    }

    // ✅ 강제 새로고침 (예: 포인트 변경 후)
    fun refresh() {
        initialized = false
        load()
    }

    // ✅ 실제 서버 로드
    fun load() {
        android.util.Log.d("MyPageVM", "load() start")

        val old = _ui.value
        _ui.value = old.copy(loading = true, error = null)

        viewModelScope.launch {
            runCatching {
                val user = repo.me()
                val (recent, _) = repo.getMyRecentVocabulary(limit = 5)
                val (liked, likedCur) = repo.getMyLikedVocabulary(limit = 10)
                val (all, allCur) = repo.getMyVocabulary(limit = 20)
                Triple(user, Triple(recent, liked, likedCur), Pair(all, allCur))
            }.onSuccess { (user, r1, r2) ->
                val (recent, liked, likedCursor) = r1
                val (all, allCursor) = r2
                val newUi = _ui.value.copy(
                    loading = false,
                    user = user,
                    recentVocab = recent,
                    likedVocab = liked,
                    likedCursor = likedCursor,
                    allVocab = all,
                    allCursor = allCursor
                )
                _ui.value = newUi
                MyPageCache.cachedUi = newUi // ✅ 캐시 저장
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = e.message ?: "불러오기 실패"
                )
            }
        }
    }

    // ✅ 추가 로드 기능들 (기존 그대로)
    fun loadMoreAll() {
        val cur = _ui.value.allCursor ?: return
        viewModelScope.launch {
            runCatching {
                repo.getMyVocabulary(
                    limit = 20,
                    lastId = cur.lastId,
                    lastCreatedAt = cur.lastCreatedAt
                )
            }.onSuccess { (items, next) ->
                _ui.value = _ui.value.copy(
                    allVocab = _ui.value.allVocab + items,
                    allCursor = next
                )
                MyPageCache.cachedUi = _ui.value
            }
        }
    }

    fun loadMoreLiked() {
        val cur = _ui.value.likedCursor ?: return
        viewModelScope.launch {
            runCatching {
                repo.getMyLikedVocabulary(
                    limit = 20,
                    lastId = cur.lastId,
                    lastCreatedAt = cur.lastCreatedAt
                )
            }.onSuccess { (items, next) ->
                _ui.value = _ui.value.copy(
                    likedVocab = _ui.value.likedVocab + items,
                    likedCursor = next
                )
                MyPageCache.cachedUi = _ui.value
            }
        }
    }

    fun toggleLike(vocabId: Int, wantLike: Boolean) {
        val before = _ui.value
        val patch: (VocabularyDto) -> VocabularyDto = { v ->
            if (v.id == vocabId) v.copy(isLiked = wantLike) else v
        }

        _ui.value = before.copy(
            togglingId = vocabId,
            allVocab = before.allVocab.map(patch),
            likedVocab = if (wantLike) before.likedVocab else before.likedVocab.filterNot { it.id == vocabId }
        )

        viewModelScope.launch {
            runCatching { repo.toggleMyVocabularyLike(vocabId, wantLike) }
                .onSuccess { res ->
                    _ui.value = _ui.value.copy(togglingId = null)
                    if (wantLike) {
                        val inAll = _ui.value.allVocab.firstOrNull { it.id == res.id }
                        if (inAll != null && _ui.value.likedVocab.none { it.id == res.id }) {
                            _ui.value = _ui.value.copy(likedVocab = listOf(inAll) + _ui.value.likedVocab)
                        }
                    }
                    MyPageCache.cachedUi = _ui.value
                }
                .onFailure {
                    _ui.value = before.copy(togglingId = null)
                    MyPageCache.cachedUi = _ui.value
                }
        }
    }

    fun updateAvatar(avatarName: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true)

            runCatching {
                repo.updateAvatarName(avatarName)
            }.onSuccess {
                val oldUser = _ui.value.user
                if (oldUser != null) {
                    val newUi = _ui.value.copy(
                        loading = false,
                        user = oldUser.copy(avatar_name = avatarName)
                    )
                    _ui.value = newUi
                    MyPageCache.cachedUi = newUi // ✅ 캐시 즉시 반영
                } else {
                    _ui.value = _ui.value.copy(loading = false)
                }
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = e.message ?: "아바타 변경 실패"
                )
            }
        }
    }

    //배지 3개
    fun loadRecentBadges() {
        viewModelScope.launch {
            try {
                val badgeMap = repo.getMyBadges() // 🔹 서버에서 전체 배지 맵 받아오기

                val badgeList = badgeMap
                    .filter { it.value == true } // 해금된 배지만 필터링
                    .map { (key, _) ->
                        val title = when (key) {
                            "1_week_attendance" -> "일주일 출석"
                            "1_month_attendance" -> "한 달 출석"
                            "100_days_attendance" -> "100일 출석"
                            "first_lesson" -> "오늘의 학습 첫 학습 완료"
                            "five_lessons" -> "오늘의 학습 5회 학습 완료"
                            "first_quizmunch" -> "퀴즈뭉치 첫 학습 완료"
                            "five_quizzes" -> "퀴즈뭉치 5회 학습 완료"
                            "first_ai_chat" -> "AI 대화 첫 학습 완료"
                            "five_ai_chats" -> "AI 대화 5회 학습 완료"
                            "first_rank" -> "처음 1등 달성"
                            "rank_1month" -> "한 달 1등 유지"
                            "bonus_month" -> "보너스 배지"
                            "early_morning" -> "새벽 학습"
                            "five_logins_day" -> "하루 5회 학습"
                            else -> key
                        }

                        // ✅ 리소스 이름 변환 규칙
                        val mappedKey = when (key) {
                            "1_week_attendance" -> "img_badge_1week_attendance"
                            "1_month_attendance" -> "img_badge_1month_attendance"
                            "100_days_attendance" -> "img_badge_100days_attendance"
                            "rank_1week" -> "img_badge_rank_1week"
                            "rank_1month" -> "img_badge_rank_1month"
                            "bonus_month" -> "img_badge_bonus_month"
                            else -> "img_badge_${key}"
                        }

                        BadgeUi(mappedKey, title, true)
                    }
                    .takeLast(3)  // ✅ 최근 해금된 3개만
                    .reversed()   // 최신순으로

                _recentBadges.value = badgeList
            } catch (e: Exception) {
                _recentBadges.value = emptyList()
            }
        }
    }

    //배지 대표 이미지 저장, 관리
    fun setRepresentativeBadge(key: String) {
        viewModelScope.launch {
            runCatching {
                repo.saveLocalRepresentativeBadge(key)
            }.onSuccess {
                Log.d("BadgeVM", "대표 배지 로컬 저장 완료: $key")
            }.onFailure { e ->
                Log.e("BadgeVM", "대표 배지 저장 실패: ${e.message}")
            }
        }
    }
    //대표 배지 저장
    fun loadRepresentativeBadge(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val key = repo.getLocalRepresentativeBadge() // suspend 함수니까 코루틴에서 호출
            onResult(key)
        }
    }

}

// ─────────────────────────────── 로그아웃 VIEWMODEL ───────────────────────────────
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _navigateLogin = MutableSharedFlow<Unit>()
    val navigateLogin = _navigateLogin.asSharedFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logoutLocal()
            MyPageCache.cachedUi = null // ✅ 로그아웃 시 캐시 초기화
            _navigateLogin.emit(Unit)
        }
    }
}

