package com.malmungchi.feature.login

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding

@Composable
fun LevelSetCompleteRoute(
    levelTitle: String,
    onRetry: () -> Unit = {},
    onGoHome: () -> Unit = {},        // 저장 성공 후 진행
    characterRes: Int = R.drawable.ic_complete_character,
    vm: AvatarSetupViewModel = hiltViewModel()
) {
    val ui by vm.ui.collectAsState()

    // ✅ M3: rememberSnackbarHostState 대신 이렇게
    val snackHostState = remember { SnackbarHostState() }

    // 진입 시 사용자 이름 불러오기
    LaunchedEffect(Unit) { vm.loadUserName() }

    // 에러 스낵바
    LaunchedEffect(ui.error) {
        ui.error?.let {
            snackHostState.showSnackbar(it)
            vm.clearError()
        }
    }

    // 스낵바가 정상 위치에 뜨도록 Scaffold에 연결
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackHostState) }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            // 기존 완료 화면에 userName & onSaveAvatar만 연결
            LevelSetCompleteScreen(
                levelTitle = levelTitle,
                onRetry = onRetry,
                onStart = { /* 실제 네비는 save 성공 후 onSuccess에서 */ },
                characterRes = characterRes,
                userName = ui.userName,
                onSaveAvatar = { avatarName ->
                    vm.saveAvatarName(avatarName) {
                        // 저장 성공 시 원래 설계대로 이동
                        onGoHome()
                    }
                }
            )
        }
    }
}
