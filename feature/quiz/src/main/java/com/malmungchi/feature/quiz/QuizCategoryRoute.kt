package com.malmungchi.feature.quiz

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.model.quiz.QuizSet

@Composable
fun QuizCategoryRoute(
    vm: QuizFlowViewModel,
    onPickCategory: (String) -> Unit     // ← 카테고리(라벨)만 밖으로 알림
) {
    val ui by vm.ui.collectAsState()

    // 에러 스낵바 (옵션)
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(ui.error) {
        ui.error?.let { snack.showSnackbar(it) }
    }

    Box(Modifier.fillMaxSize()) {
        QuizScreen(
            onClickJobPrep  = { onPickCategory("취업준비") },
            onClickBasic    = { onPickCategory("기초") },
            onClickPractice = { onPickCategory("활용") },
            onClickDeep     = { onPickCategory("심화") },
            onClickAdvanced = { onPickCategory("고급") },
        )

//        if (ui.loading) {
//            CircularProgressIndicator(Modifier.align(Alignment.Center))
//        }

        SnackbarHost(hostState = snack, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

//@Composable
//fun QuizCategoryRoute(
//    vm: QuizFlowViewModel,
//    onPickCategory: (String) -> Unit
//    //onPickCategory: (com.malmungchi.core.model.quiz.QuizCategory) -> Unit  // 세트 로딩되면 문제풀이 화면으로 이동하고 싶을 때 사용
//) {
//    val ui by vm.ui.collectAsState()
//
//    // 에러 스낵바 (옵션)
//    val snack = remember { SnackbarHostState() }
//    LaunchedEffect(ui.error) {
//        ui.error?.let { snack.showSnackbar(it) }
//    }
//
//    // 세트가 로드되면 콜백으로 알려주고 다음 화면으로 이동할 수 있게
//    LaunchedEffect(ui.current, ui.total, ui.headerTitle) {
//        // 로딩 완료 + 첫 문제 세팅되었으면 세트 객체를 넘겨 네비게이트
//        val currentSet = vm.internalCurrentSetOrNull()
//        if (!ui.loading && currentSet != null && ui.current != null) {
//            Log.d("Quiz", "Loaded set id=${currentSet.id}, category=${ui.headerTitle}")
//            onReadyToSolve(currentSet)
//        }
//    }
//
//    // 화면
//    Box(Modifier.fillMaxSize()) {
//        QuizScreen(
//            onClickJobPrep = { vm.startQuiz("취업준비") }, // UI에는 "취업\n준비" 표시지만 서버엔 공백 없이
//            onClickBasic   = { vm.startQuiz("기초") },
//            onClickPractice= { vm.startQuiz("활용") },
//            onClickDeep    = { vm.startQuiz("심화") },
//            onClickAdvanced= { vm.startQuiz("고급") },
//        )
//
//        // 로딩 인디케이터 (옵션)
//        if (ui.loading) {
//            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//        }
//
//        SnackbarHost(hostState = snack, modifier = Modifier.align(Alignment.BottomCenter))
//    }
//}
