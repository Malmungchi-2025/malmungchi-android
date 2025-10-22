package com.malmungchi.feature.mypage.badge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BadgeViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BadgeUiState>(BadgeUiState.Loading)
    val uiState: StateFlow<BadgeUiState> = _uiState

    fun loadBadges() {
        viewModelScope.launch {
            try {
                val badgeMap = repo.getMyBadges()
                val badgeList = badgeMap.map { (key, unlocked) ->
                    val title = badgeTitleMap[key] ?: key

                    // ✅ 여기서부터 수정!
                    val mappedKey = when (key) {
                        "1_week_attendance" -> "img_badge_1week_attendance"
                        "1_month_attendance" -> "img_badge_1month_attendance"
                        "100_days_attendance" -> "img_badge_100days_attendance"
                        else -> "img_badge_${key}"
                    }
                    // ✅ 여기까지!

                    BadgeUi(mappedKey, title, unlocked == true)
                }
                _uiState.value = BadgeUiState.Success(badgeList)
            } catch (e: Exception) {
                _uiState.value = BadgeUiState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }



    private val badgeTitleMap = mapOf(
        "1_week_attendance" to "일주일 출석",
        "1_month_attendance" to "한 달 출석",
        "100_days_attendance" to "100일 출석",
        "first_lesson" to "오늘의 학습\n첫 학습 완료",
        "five_lessons" to "오늘의 학습\n5회 학습 완료",
        "first_quizmunch" to "퀴즈뭉치\n첫 학습 완료",
        "five_quizzes" to "퀴즈뭉치\n5회 학습 완료",
        "first_ai_chat" to "AI 대화\n첫 학습 완료",
        "five_ai_chats" to "AI 대화\n5회 학습 완료",
        "first_rank" to "처음 1등 달성",
        "rank_1week" to "일주일 1등 유지",
        "rank_1month" to "한 달 1등 유지",
        "bonus_month" to "보너스 배지",
        "early_morning" to "새벽 학습",
        "five_logins_day" to "하루 5회 학습"
    )
}



sealed interface BadgeUiState {
    object Loading : BadgeUiState
    data class Success(val badges: List<BadgeUi>) : BadgeUiState
    data class Error(val message: String) : BadgeUiState
}