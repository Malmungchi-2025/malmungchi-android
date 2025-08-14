package com.malmungchi.feature.login


import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpRoute(
    onBack: () -> Unit = {},
    onRegistered: () -> Unit = {}   // 가입 완료 후 이동
) {
    val vm: AuthViewModel = hiltViewModel()
    val ui by vm.ui.collectAsState()

    // 회원가입 성공 시 이동
    LaunchedEffect(ui.registered) {
        if (ui.registered) onRegistered()
    }

    SignUpFlowScreen(
        onBack = onBack,
        onRequestEmailOtp = { email -> vm.requestOtpAwait(email) },
        onVerifyEmailOtp  = { email, code -> vm.verifyOtpAwait(email, code) },
        onDone = { name, email, password ->
            vm.register(name, email, password)   // 성공 시 위 LaunchedEffect가 이동시킴
        }
    )
}