package com.malmungchi.feature.mypage


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.model.UserDto
import com.malmungchi.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyPageUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val user: UserDto? = null
) {
    val userName: String get() = user?.nickname ?: user?.name ?: "사용자"
    val levelLabel: String get() = when (user?.level) {
        0 -> "입문"; 1 -> "기초"; 2 -> "중급"; 3 -> "심화"; else -> "미정"
    }
    val levelProgress: Float get() = ((user?.level ?: 0) / 3f).coerceIn(0f, 1f)
}

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(MyPageUiState())
    val ui: StateFlow<MyPageUiState> = _ui

    fun load() {
        _ui.value = _ui.value.copy(loading = true, error = null)
        viewModelScope.launch {
            runCatching { repo.me() }
                .onSuccess { user ->
                    _ui.value = MyPageUiState(loading = false, user = user)
                }
                .onFailure { e ->
                    _ui.value = MyPageUiState(loading = false, error = e.message ?: "불러오기 실패")
                }
        }
    }
}