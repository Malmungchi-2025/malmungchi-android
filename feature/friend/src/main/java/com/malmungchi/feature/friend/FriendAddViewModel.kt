package com.malmungchi.feature.friend

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.model.AddFriendResult
import com.malmungchi.core.model.FriendSummary
import com.malmungchi.core.model.UserDto
import com.malmungchi.core.repository.AuthRepository
import com.malmungchi.core.repository.FriendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FriendAddUiState(
    val myCode: String = "",
    val foundFriend: FriendUi? = null,
    val isAdded: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,

    // 랭킹 탭/로딩/리스트
    val rankTab: RankTab = RankTab.FRIEND,
    val rankLoading: Boolean = false,
    val friends: List<FriendRank> = emptyList(),
    val all: List<FriendRank> = emptyList()
)

enum class RankTab { FRIEND, ALL }

// ───────────────── UI Models ─────────────────
data class FriendUi(
    val code: String,
    val name: String,
    @DrawableRes val avatarRes: Int? = null
)

data class FriendRank(
    val rank: Int,
    val name: String,
    val points: Int,
    val isMe: Boolean = false,
    @DrawableRes val avatarRes: Int? = null
)

// UserDto -> FriendSummary 변환 (내 정보를 friends 리스트에 주입하기 위함)
private fun UserDto.toFriendSummary() = FriendSummary(
    id = id,
    name = name,
    avatar_name = avatar_name,
    point = point,
    friend_code = friend_code
)

// 서버 → UI 변환 (항상 본인 포함)
// me가 null이 아니고 raw에 meId가 없으면 me를 추가한 뒤 정렬/랭킹 부여
fun toRankList(
    raw: List<FriendSummary>,
    meId: Int?,
    me: FriendSummary? = null
): List<FriendRank> {
    val ensured: List<FriendSummary> =
        if (meId != null && raw.none { it.id == meId }) {
            if (me != null) raw + me else raw
        } else raw

    return ensured.sortedByDescending { it.point ?: 0 }
        .mapIndexed { idx, f ->
            FriendRank(
                rank = idx + 1,
                name = f.name ?: "친구",
                points = f.point ?: 0,
                isMe = (meId != null && f.id == meId),
                avatarRes = mapAvatarNameToRes(f.avatar_name)
            )
        }
}

@HiltViewModel
class FriendAddViewModel @Inject constructor(
    private val authRepo: AuthRepository,      // 내 코드(me) 조회
    private val friendRepo: FriendRepository   // 친구추가/랭킹 API
) : ViewModel() {

    private val _ui = MutableStateFlow(FriendAddUiState())
    val ui: StateFlow<FriendAddUiState> = _ui.asStateFlow()

    // 내 정보 캐시 (friends에 항상 포함시키기 위해)
    private var meSummary: FriendSummary? = null

    init {
        viewModelScope.launch {
            runCatching { authRepo.me() }
                .onSuccess { meDto ->
                    _ui.value = _ui.value.copy(myCode = meDto?.friend_code.orEmpty())
                    meSummary = meDto?.toFriendSummary()
                }
            // ✅ 기본 탭(FRIEND) 랭킹 1회 로드
            ensureLoaded(RankTab.FRIEND)
        }
    }

    /** “검색” 버튼: 서버는 실제로 ‘추가’까지 수행 */
    fun searchAndAdd(code: String) {
        val trimmed = code.trim().uppercase()
        if (trimmed.length != 7) {
            _ui.value = _ui.value.copy(error = "코드는 7자리여야 합니다.")
            return
        }
        _ui.value = _ui.value.copy(loading = true, error = null)

        viewModelScope.launch {
            runCatching { friendRepo.addFriendByCode(trimmed) }
                .onSuccess { res: AddFriendResult ->
                    val friend = res.friend
                    val avatar = mapAvatarNameToRes(friend.avatar_name)
                    _ui.value = _ui.value.copy(
                        foundFriend = FriendUi(
                            code = friend.friend_code,
                            name = friend.name ?: "친구",
                            avatarRes = avatar
                        ),
                        isAdded = true,
                        loading = false,
                        error = null
                    )
                    // 친구 추가했으니 친구 랭킹 갱신
                    loadFriendsRanking(force = true)
                }
                .onFailure { e ->
                    _ui.value = _ui.value.copy(loading = false, error = e.message ?: "친구 추가 실패")
                }
        }
    }

    fun clearError() {
        if (_ui.value.error != null) _ui.value = _ui.value.copy(error = null)
    }

    // ---------- 랭킹(친구/전체) ----------
    fun switchTab(tab: RankTab) {
        if (_ui.value.rankTab == tab) return
        _ui.value = _ui.value.copy(rankTab = tab)
        ensureLoaded(tab)
    }

    fun refresh() {
        when (_ui.value.rankTab) {
            RankTab.FRIEND -> loadFriendsRanking(force = true)
            RankTab.ALL    -> loadGlobalRanking(force = true)
        }
    }

    private fun ensureLoaded(tab: RankTab) {
        val needLoad = when (tab) {
            RankTab.FRIEND -> _ui.value.friends.isEmpty()
            RankTab.ALL    -> _ui.value.all.isEmpty()
        }
        if (needLoad) {
            when (tab) {
                RankTab.FRIEND -> loadFriendsRanking()
                RankTab.ALL    -> loadGlobalRanking()
            }
        }
    }

    private fun loadFriendsRanking(force: Boolean = false) {
        if (!force && _ui.value.friends.isNotEmpty()) return
        _ui.value = _ui.value.copy(rankLoading = true, error = null)
        viewModelScope.launch {
            runCatching { friendRepo.getFriendsRanking() }
                .onSuccess { (meId, items) ->
                    // meSummary가 없으면 한 번 더 시도해서 채워둠
                    if (meSummary == null) {
                        meSummary = runCatching { authRepo.me() }.getOrNull()?.toFriendSummary()
                    }
                    _ui.value = _ui.value.copy(
                        friends = toRankList(items, meId, meSummary),
                        rankLoading = false
                    )
                }
                .onFailure { e ->
                    _ui.value = _ui.value.copy(rankLoading = false, error = e.message ?: "친구 랭킹 조회 실패")
                }
        }
    }

    private fun loadGlobalRanking(force: Boolean = false) {
        if (!force && _ui.value.all.isNotEmpty()) return
        _ui.value = _ui.value.copy(rankLoading = true, error = null)
        viewModelScope.launch {
            runCatching { friendRepo.getGlobalRanking() }
                .onSuccess { (meId, items) ->
                    // 전역 랭킹에도 혹시 빠져있을 수 있으니 동일 보정 사용
                    _ui.value = _ui.value.copy(
                        all = toRankList(items, meId, meSummary),
                        rankLoading = false
                    )
                }
                .onFailure { e ->
                    _ui.value = _ui.value.copy(rankLoading = false, error = e.message ?: "전체 랭킹 조회 실패")
                }
        }
    }
}



//package com.malmungchi.feature.friend
//
//
//import androidx.annotation.DrawableRes
//import androidx.compose.runtime.collectAsState
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.malmungchi.core.model.AddFriendResult
//import com.malmungchi.core.model.FriendSummary
//import com.malmungchi.core.model.UserDto
//import com.malmungchi.core.repository.AuthRepository
//import com.malmungchi.core.repository.FriendRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//
//data class FriendAddUiState(
//    val myCode: String = "",
//    val foundFriend: FriendUi? = null,
//    val isAdded: Boolean = false,
//    val loading: Boolean = false,
//    val error: String? = null,
//
//    // 랭킹 탭/로딩/리스트
//    val rankTab: RankTab = RankTab.FRIEND,
//    val rankLoading: Boolean = false,
//    val friends: List<FriendRank> = emptyList(),
//    val all: List<FriendRank> = emptyList()
//)
//
//
//
//enum class RankTab { FRIEND, ALL }
//
//// ───────────────── UI Models ─────────────────
//data class FriendUi(
//    val code: String,
//    val name: String,
//    @DrawableRes val avatarRes: Int? = null
//)
//
//
//data class FriendRank(
//    val rank: Int,
//    val name: String,
//    val points: Int,
//    val isMe: Boolean = false,
//    @DrawableRes val avatarRes: Int? = null
//)
//
//// UserDto -> FriendSummary 변환 (내 정보를 friends 리스트에 주입하기 위함)
//private fun UserDto.toFriendSummary() = FriendSummary(
//    id = id,
//    name = name,
//    avatar_name = avatar_name,
//    point = point,
//    friend_code = friend_code
//)
//
//// 서버 → UI 변환
//fun toRankList(raw: List<FriendSummary>, meId: Int?, me: FriendSummary?): List<FriendRank> {
//    // 본인이 없으면 추가
//    val ensured = if (meId != null && raw.none { it.id == meId }) {
//        raw + (me ?: FriendSummary(
//            id = meId,
//            name = "나",
//            avatar_name = null,
//            point = 0,
//            friend_code = ""
//        ))
//    } else {
//        raw
//    }
//
//    return ensured.sortedByDescending { it.point ?: 0 }
//        .mapIndexed { idx, f ->
//            FriendRank(
//                rank = idx + 1,
//                name = f.name ?: "친구",
//                points = f.point ?: 0,
//                isMe = (meId != null && f.id == meId),
//                avatarRes = mapAvatarNameToRes(f.avatar_name)
//            )
//        }
//}
////fun toRankList(raw: List<FriendSummary>, meId: Int?): List<FriendRank> =
////    raw.sortedByDescending { it.point ?: 0 }
////        .mapIndexed { idx, f ->
////            FriendRank(
////                rank = idx + 1,
////                name = f.name ?: "친구",
////                points = f.point ?: 0,
////                isMe = (meId != null && f.id == meId),
////                avatarRes = mapAvatarNameToRes(f.avatar_name)
////            )
////        }
//
//@HiltViewModel
//class FriendAddViewModel @Inject constructor(
//    private val authRepo: AuthRepository,      // 내 코드(me) 조회
//    private val friendRepo: FriendRepository   // 친구추가/랭킹 API
//) : ViewModel() {
//
//    private val _ui = MutableStateFlow(FriendAddUiState())
//    val ui: StateFlow<FriendAddUiState> = _ui.asStateFlow()
//
//    init {
//        // 앱 진입 시 내 코드 가져오기
//        viewModelScope.launch {
//            runCatching { authRepo.me() }
//                .onSuccess { me ->
//                    val code = me?.friend_code.orEmpty()
//                    _ui.value = _ui.value.copy(myCode = code)
//                }
//            // ✅ 기본 탭(FRIEND) 랭킹 1회 로드
//            ensureLoaded(RankTab.FRIEND)
//        }
//    }
//
//    /** “검색” 버튼: 서버는 실제로 ‘추가’까지 수행 */
//    fun searchAndAdd(code: String) {
//        val trimmed = code.trim().uppercase()
//        if (trimmed.length != 7) {
//            _ui.value = _ui.value.copy(error = "코드는 7자리여야 합니다.")
//            return
//        }
//        _ui.value = _ui.value.copy(loading = true, error = null)
//
//        viewModelScope.launch {
//            runCatching { friendRepo.addFriendByCode(trimmed) }
//                .onSuccess { res: AddFriendResult ->
//                    val friend = res.friend
//                    val avatar = mapAvatarNameToRes(friend.avatar_name)
//                    _ui.value = _ui.value.copy(
//                        foundFriend = FriendUi(
//                            code = friend.friend_code,
//                            name = friend.name ?: "친구",
//                            avatarRes = avatar
//                        ),
//                        isAdded = true,
//                        loading = false,
//                        error = null
//                    )
//                    // 친구 추가했으니 친구 랭킹 갱신
//                    loadFriendsRanking(force = true)
//                }
//                .onFailure { e ->
//                    _ui.value = _ui.value.copy(loading = false, error = e.message ?: "친구 추가 실패")
//                }
//        }
//    }
//
//    fun clearError() {
//        if (_ui.value.error != null) _ui.value = _ui.value.copy(error = null)
//    }
//
//    // ---------- 랭킹(친구/전체) ----------
//    fun switchTab(tab: RankTab) {
//        if (_ui.value.rankTab == tab) return
//        _ui.value = _ui.value.copy(rankTab = tab)
//        ensureLoaded(tab)
//    }
//
//    fun refresh() {
//        when (_ui.value.rankTab) {
//            RankTab.FRIEND -> loadFriendsRanking(force = true)
//            RankTab.ALL    -> loadGlobalRanking(force = true)
//        }
//    }
//
//    private fun ensureLoaded(tab: RankTab) {
//        val needLoad = when (tab) {
//            RankTab.FRIEND -> _ui.value.friends.isEmpty()
//            RankTab.ALL    -> _ui.value.all.isEmpty()
//        }
//        if (needLoad) {
//            when (tab) {
//                RankTab.FRIEND -> loadFriendsRanking()
//                RankTab.ALL    -> loadGlobalRanking()
//            }
//        }
//    }
//
//    private fun loadFriendsRanking(force: Boolean = false) {
//        if (!force && _ui.value.friends.isNotEmpty()) return
//        _ui.value = _ui.value.copy(rankLoading = true, error = null)
//        viewModelScope.launch {
//            runCatching { friendRepo.getFriendsRanking() }
//                .onSuccess { (meId, items) ->
//                    // 내 정보 불러오기 (이미 init에서 authRepo.me() 호출한 적 있음)
//                    val me = runCatching { authRepo.me() }.getOrNull()
//                    _ui.value = _ui.value.copy(
//                        friends = toRankList(items, meId, me),
//                        rankLoading = false
//                    )
//                }
////            runCatching { friendRepo.getFriendsRanking() }
////                .onSuccess { (meId, items) ->
////                    _ui.value = _ui.value.copy(
////                        friends = toRankList(items, meId),
////                        rankLoading = false
////                    )
////                }
//                .onFailure { e ->
//                    _ui.value = _ui.value.copy(rankLoading = false, error = e.message ?: "친구 랭킹 조회 실패")
//                }
//        }
//    }
//
//    private fun loadGlobalRanking(force: Boolean = false) {
//        if (!force && _ui.value.all.isNotEmpty()) return
//        _ui.value = _ui.value.copy(rankLoading = true, error = null)
//        viewModelScope.launch {
//            runCatching { friendRepo.getGlobalRanking() }
//                .onSuccess { (meId, items) ->
//                    _ui.value = _ui.value.copy(
//                        all = toRankList(items, meId),
//                        rankLoading = false
//                    )
//                }
//                .onFailure { e ->
//                    _ui.value = _ui.value.copy(rankLoading = false, error = e.message ?: "전체 랭킹 조회 실패")
//                }
//        }
//    }
//}