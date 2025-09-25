package com.malmungchi.feature.mypage


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
            !user?.name.isNullOrBlank()     -> user!!.name!!.trim()
            else                            -> "사용자"
        }

    val avatarName: String
        get() = user?.avatar_name?.takeIf { it.isNotBlank() } ?: "img_malchi"

    /** 0->입문, 1->기초, 2->활용, 3->심화, 4+->고급 */
    val levelInt: Int get() = user?.level ?: 0

    val levelLabel: String get() = when (levelInt) {
        0 -> "입문"
        1 -> "기초"
        2 -> "활용"
        3 -> "심화"
        else -> "고급"
    }

    /** 진행도 바(0..1)는 '다음 단계 타깃' 대비 현재 포인트 비율로 계산 */
    val point: Int get() = user?.point ?: 0

    /** 다음 단계 라벨/타깃 포인트 계산 */
    private fun nextStageLabelAndTarget(): Pair<String, Int>? = when (levelInt) {
        0, 1 -> "활용" to 1350
        2    -> "심화" to 2700
        3    -> "고급" to 4050
        else -> null
    }

    /** 다음 단계가 있으면 그 대비 진행률, 없으면 1f */
    val nextProgress: Float get() {
        val t = nextStageLabelAndTarget() ?: return 1f
        val target = t.second.toFloat().coerceAtLeast(1f)
        return (point / target).coerceIn(0f, 1f)
    }

    /** 다음 단계로 표기되는 텍스트용 데이터 */
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
//data class MyPageUiState(
//    val loading: Boolean = false,
//    val error: String? = null,
//    val user: UserDto? = null,
//    val recentVocab: List<VocabularyDto> = emptyList(),
//    val likedVocab: List<VocabularyDto> = emptyList(),
//    val allVocab: List<VocabularyDto> = emptyList(),
//    val allCursor: NextCursor? = null,
//    val likedCursor: NextCursor? = null,
//    val togglingId: Int? = null
//) {
//    val userName: String
//        get() = when {
//            !user?.nickname.isNullOrBlank() -> user!!.nickname!!.trim()
//            !user?.name.isNullOrBlank()     -> user!!.name!!.trim()
//            else                            -> "사용자"
//        }
//
//    /** 0->입문, 1->기초, 2->활용, 3->심화, else->고급 */
//    val levelLabel: String get() = when (user?.level) {
//        0 -> "입문"
//        1 -> "기초"
//        2 -> "활용"
//        3 -> "심화"
//        else -> "고급"
//    }
//
//    /** 진행도는 0..4(이상)을 0..1 로 환산 (4 이상이면 1.0 고정) */
//    val levelProgress: Float get() = ((user?.level ?: 0).coerceAtLeast(0) / 4f).coerceIn(0f, 1f)
//}

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(MyPageUiState())
    val ui: StateFlow<MyPageUiState> = _ui

    init {
        android.util.Log.d("MyPageVM", "init -> load()")
        load()
    }


    fun load() {
        android.util.Log.d("MyPageVM", "load() start")
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                val user = repo.me()
                val (recent, _) = repo.getMyRecentVocabulary(limit = 5)
                val (liked, likedCur) = repo.getMyLikedVocabulary(limit = 10)

                // 첫 페이지 로드
                val (all, allCur) = repo.getMyVocabulary(limit = 20)
                Triple(user, Triple(recent, liked, likedCur), Pair(all, allCur))
            }.onSuccess { (user, r1, r2) ->
                val (recent, liked, likedCursor) = r1
                val (all, allCursor) = r2
                _ui.value = _ui.value.copy(
                    loading = false,
                    user = user,
                    recentVocab = recent,
                    likedVocab = liked,
                    likedCursor = likedCursor,
                    allVocab = all,
                    allCursor = allCursor
                )
            }.onFailure { e ->
                _ui.value = _ui.value.copy(
                    loading = false,
                    error = e.message ?: "불러오기 실패"
                )
            }
        }
    }

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
            }
        }
    }

    fun toggleLike(vocabId: Int, wantLike: Boolean) {
        // 낙관적 업데이트
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
                    // 서버 정답으로 재동기화 (아이디만 오므로 현재 상태 유지)
                    _ui.value = _ui.value.copy(togglingId = null)
                    if (wantLike) {
                        val inAll = _ui.value.allVocab.firstOrNull { it.id == res.id }
                        if (inAll != null && _ui.value.likedVocab.none { it.id == res.id }) {
                            _ui.value = _ui.value.copy(likedVocab = listOf(inAll) + _ui.value.likedVocab)
                        }
                    }
                }
                .onFailure {
                    // 롤백
                    _ui.value = before.copy(togglingId = null)
                }
        }
    }
}

//로그아웃 구현
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _navigateLogin = MutableSharedFlow<Unit>()
    val navigateLogin = _navigateLogin.asSharedFlow()

    fun logout() {
        viewModelScope.launch {
            authRepository.logoutLocal()
            _navigateLogin.emit(Unit)
        }
    }
}