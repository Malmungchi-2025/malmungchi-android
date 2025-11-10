package com.malmungchi.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AvatarSetupUiState(
    val userName: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AvatarSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AvatarSetupUiState())
    val ui: StateFlow<AvatarSetupUiState> = _ui

    /** 완료 화면 진입 시 한 번 호출해서 사용자 이름을 채웁니다. */
    fun loadUserName() {
        viewModelScope.launch {
            runCatching { authRepository.me() }
                .onSuccess { user ->
                    _ui.update { it.copy(userName = user?.name.orEmpty(), error = null) }
                }
                .onFailure { e ->
                    // 이름 로드 실패해도 치명적이진 않으니 로그/메시지 정도만
                    _ui.update { it.copy(error = e.message) }
                }
        }
    }

    /**
     * 아바타 저장.
     * 성공 시 onSuccess()를 호출해서 다음 화면으로 이동시키면 됩니다.
     */
    fun saveAvatarName(avatarName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(isSaving = true, error = null) }
            runCatching { authRepository.updateAvatarName(avatarName) }
                .onSuccess {
                    _ui.update { it.copy(isSaving = false, error = null) }
                    onSuccess()
                }
                .onFailure { e ->
                    _ui.update { it.copy(isSaving = false, error = e.message ?: "아바타 저장 실패") }
                }
        }
    }

    fun clearError() {
        _ui.update { it.copy(error = null) }
    }
}