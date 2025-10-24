package com.malmungchi.feature.login


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.BuildConfig
import com.malmungchi.core.model.LoginResponse
import com.malmungchi.core.model.RegisterResponse
import com.malmungchi.core.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val otpRequested: Boolean = false,
    val otpVerified: Boolean = false,
    val registered: Boolean = false,
    val lastRegister: RegisterResponse? = null,
    val lastLogin: LoginResponse? = null
)

sealed interface AuthEvent {
    data class Toast(val message: String): AuthEvent
    object OtpRequested: AuthEvent
    object OtpVerified: AuthEvent
    object Registered: AuthEvent
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    suspend fun requestOtpAwait(email: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { repo.devRequestOtp(email.trim()) }.getOrElse { false }
    }

    suspend fun verifyOtpAwait(email: String, code: String): Boolean = withContext(Dispatchers.IO) {
        if (code.length != 6) return@withContext false
        runCatching { repo.devVerifyOtp(email.trim(), code) }.getOrElse { false }
    }

//    /** Gmail만 허용 (요구사항 반영) */
//    private fun isGmail(email: String): Boolean {
//        val normalized = email.trim().lowercase()
//        return normalized.endsWith("@gmail.com") && "@" in normalized
//    }
    //지메일&네이버만 메일 전송
    private fun isAllowedEmail(email: String): Boolean {

        val e = email.trim().lowercase()
        return e.endsWith("@gmail.com") || e.endsWith("@naver.com")

    }

    /** 1) DEV: OTP 요청 */
    fun requestOtp(email: String) {
        viewModelScope.launch {
            val e = email.trim().lowercase()

            // 🔴 릴리즈 빌드에서는 Gmail/Naver만 허용
            if (!BuildConfig.DEBUG && !isAllowedEmail(e)) {
                _events.send(AuthEvent.Toast("허용되지 않은 이메일 도메인입니다."))
                return@launch
            }

            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                repo.devRequestOtp(e)
            }.onSuccess { ok ->
                if (ok) {
                    _ui.update { it.copy(loading = false, otpRequested = true) }
                    _events.send(AuthEvent.OtpRequested)
                } else {
                    _ui.update { it.copy(loading = false, error = "인증번호 요청에 실패했습니다.") }
                }
            }.onFailure { t ->
                _ui.update { s -> s.copy(loading = false, error = t.message ?: "네트워크 오류") }
            }
        }
    }


    /** 2) DEV: OTP 검증 */
    fun verifyOtp(email: String, code: String) {
        viewModelScope.launch {
            val e = email.trim()
            if (code.length != 6) {
                _events.send(AuthEvent.Toast("6자리 코드를 입력하세요."))
                return@launch
            }
            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                repo.devVerifyOtp(e, code)
            }.onSuccess { ok ->
                if (ok) {
                    _ui.update { it.copy(loading = false, otpVerified = true) }
                    _events.send(AuthEvent.OtpVerified)
                } else {
                    _ui.update { it.copy(loading = false, error = "인증번호가 올바르지 않아요.") }
                }
            }.onFailure { t->
                _ui.update { s -> s.copy(loading = false, error = t.message ?: "네트워크 오류") }
            }
        }
    }

    /** 3) 회원가입 (DB 저장) */
    fun register(name: String, email: String, password: String, nickname: String? = null) {
        viewModelScope.launch {
            val n = name.trim()
            val e = email.trim().lowercase()
            val p = password

            if (n.isBlank()) {
                _events.send(AuthEvent.Toast("이름을 입력해 주세요.")); return@launch
            }
            if (p.length < 8) {
                _events.send(AuthEvent.Toast("비밀번호는 8자 이상이어야 합니다.")); return@launch
            }

            // 🔴 릴리즈 빌드에서는 Gmail/Naver만 허용
            if (!BuildConfig.DEBUG && !isAllowedEmail(e)) {
                _events.send(AuthEvent.Toast("허용되지 않은 이메일 도메인입니다."))
                return@launch
            }

            _ui.update { it.copy(loading = true, error = null) }
            runCatching {
                repo.register(e, p, n, nickname)
            }.onSuccess { res ->
                if (res.success) {
                    _ui.update { it.copy(loading = false, registered = true, lastRegister = res) }
                    _events.send(AuthEvent.Registered)
                } else {
                    _ui.update { it.copy(loading = false, error = res.message ?: "회원가입 실패") }
                }
            }.onFailure { t ->
                _ui.update { s -> s.copy(loading = false, error = t.message ?: "네트워크 오류") }
            }
        }
    }

    /** (옵션) 이메일 인증 메일 재전송 */
    fun resendVerification(email: String) {
        viewModelScope.launch {
            val e = email.trim().lowercase()
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { repo.resendVerification(e) }
                .onSuccess { ok ->
                    _ui.update { it.copy(loading = false) }
                    _events.trySend(AuthEvent.Toast(if (ok) "인증 메일 재전송 완료" else "재전송 실패"))
                }
                .onFailure { t->
                    _ui.update { s -> s.copy(loading = false, error = t.message ?: "네트워크 오류") }
                }
        }
    }

    fun consumeError(): String? {
        val msg = _ui.value.error
        if (msg != null) _ui.update { it.copy(error = null) }
        return msg
    }


    fun login(
        email: String,
        password: String,
        onResult: (ok: Boolean, userId: Int?, token: String?, message: String?) -> Unit
    ) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }

            runCatching {
                repo.login(email.trim(), password)  // ← 바로 LoginResponse 반환
            }.onSuccess { res ->
                val user = res.user
                val token = res.token

                // ✅ 성공
                if (res.success && token != null && user != null) {
                    val userIdInt = try { user.id.toInt() } catch (_: Exception) { null }
                    if (userIdInt != null) {
                        _ui.update { it.copy(loading = false, lastLogin = res) }
                        onResult(true, userIdInt, token, null)
                    } else {
                        _ui.update { it.copy(loading = false) }
                        onResult(false, null, null, "로그인 실패: 잘못된 사용자 ID 형식")
                    }
                }
                // ❌ 실패 — 서버가 success:false로 응답
                else {
                    _ui.update { it.copy(loading = false) }
                    val msg = res.message ?: "이메일 또는 비밀번호가 올바르지 않습니다."
                    onResult(false, null, null, msg)
                }

            }.onFailure { e ->
                _ui.update { it.copy(loading = false) }
                val msg = when {
                    e.message?.contains("401") == true -> "이메일 또는 비밀번호가 올바르지 않습니다."
                    e.message?.contains("400") == true -> "이메일/비밀번호를 모두 입력해주세요."
                    else -> "네트워크 오류가 발생했습니다."
                }
                onResult(false, null, null, msg)
            }
        }
    }

}