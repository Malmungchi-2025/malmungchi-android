package com.malmungchi.feature.friend

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch

@Composable
fun FriendAddRoute(
    onBack: () -> Unit = {},
    onViewRank: () -> Unit = {}   // 랭킹 화면으로 이동
) {
    val vm: FriendAddViewModel = hiltViewModel()
    val state by vm.ui.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current

    // 에러 스낵바
    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            vm.clearError()
        }
    }

    // isAdded 되면(서버에서 이미 친구 추가 완료), 하단 토스트는 FriendAddScreen이 띄우고
    // 사용자는 "친구순위보기" 버튼으로 이동
    // (자동 이동을 원하면 아래 주석 해제)
    // LaunchedEffect(state.isAdded) {
    //     if (state.isAdded) onViewRank()
    // }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        FriendAddScreen(
            myCode = state.myCode,
            foundFriend = state.foundFriend,
            isAdded = state.isAdded,
           loading = state.loading,                // ⬅️ 추가
            onBack = onBack,
            onSearch = { code -> vm.searchAndAdd(code) },
            onAddFriend = { _ -> onViewRank() },    // 이미 서버에서 추가됨 → 랭킹 보기로 연결
            onViewRank = onViewRank,
            onCopyMyCode = { code ->
                clipboard.setText(AnnotatedString(code))
                scope.launch { snackbarHostState.showSnackbar("복사되었습니다") }
            },
            contentPadding = padding                // ⬅️ inset 전달(선택)
        )
    }
}