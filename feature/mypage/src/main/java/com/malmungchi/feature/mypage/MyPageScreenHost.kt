//package com.malmungchi.feature.mypage
//
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.hilt.navigation.compose.hiltViewModel
//// (선택) lifecycle-aware collect:
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//
//@Composable
//fun MyPageScreenHost(
//    onClickSettings: () -> Unit = {},
//    onClickViewAllWords: () -> Unit = {},
//    onClickViewAllBadges: () -> Unit = {},
//    vm: MyPageViewModel = hiltViewModel()
//) {
//    // val ui by vm.ui.collectAsState()  // 기본
//    val ui by vm.ui.collectAsStateWithLifecycle() // 권장
//
//    LaunchedEffect(Unit) { vm.load() }
//
//    when {
//        ui.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator()
//        }
//        ui.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Text(text = ui.error ?: "에러")
//        }
//        else -> MyPageScreen(
//            userName = ui.userName,
//            levelLabel = ui.levelLabel,
//            levelProgress = ui.levelProgress,
//            onClickSettings = onClickSettings,
//            onClickViewAllWords = onClickViewAllWords,
//            onClickViewAllBadges = onClickViewAllBadges
//        )
//    }
//}