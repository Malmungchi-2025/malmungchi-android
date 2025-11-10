package com.malmungchi.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.model.quiz.*

@Composable
fun QuizSolveRoute(
    vm: QuizFlowViewModel = hiltViewModel(),
    onQuitToHome: () -> Unit = {},   // ← (선택) 종료 시 이동 콜백 추가
    onFinish: () -> Unit = {}
) {
    val ui by vm.ui.collectAsState()
    val snack = remember { SnackbarHostState() }

    // ✅ 추가 1: 종료 알럿 상태
    var showExit by remember { mutableStateOf(false) }

    // ✅ 추가 2: 시스템 뒤로키 처리 (첫 문항이면 알럿, 아니면 이전 문항)
    BackHandler(enabled = true) {
        if (ui.index == 0) showExit = true else vm.onEvent(QuizEvent.Back)
    }

    // ✅ 선택 상태를 StateFlow로 구독
    val selectedMcq  by vm.currentMcq.collectAsState()
    val selectedOx   by vm.currentOx.collectAsState()
    val shortAnswer  by vm.currentShort.collectAsState()

    LaunchedEffect(ui.error) { ui.error?.let { snack.showSnackbar(it) } }
    LaunchedEffect(ui.finished) { if (ui.finished) onFinish() }

    val submitted = vm.currentSubmitted()
    val idx = ui.index

    Box(Modifier.fillMaxSize()) {
        when (val q = ui.current) {
            is McqStep -> QuizMcqScreen(
                categoryTitle = ui.headerTitle,
                step = ui.displayStep, total = ui.total, progress = ui.progress,
                question = q,
                selectedOptionId = selectedMcq,
                submitted = submitted,
                onSelect = { vm.onEvent(QuizEvent.SelectMcq(it)) },
                onSubmit = { if (!submitted) vm.onEvent(QuizEvent.Submit) },
                onBack = { if (idx == 0) showExit = true else vm.onEvent(QuizEvent.Back) },
                showPrimaryButton = !submitted
            )
            is OxStep -> QuizOxScreen(
                categoryTitle = ui.headerTitle,
                step = ui.displayStep, total = ui.total, progress = ui.progress,
                question = q,
                selectedIsO = selectedOx,
                submitted = submitted,
                onSelect = { vm.onEvent(QuizEvent.SelectOx(it)) },
                onSubmit = { if (!submitted) vm.onEvent(QuizEvent.Submit) },
                onBack = { if (idx == 0) showExit = true else vm.onEvent(QuizEvent.Back) },
                showPrimaryButton = !submitted
            )
            is ShortStep -> QuizShortAnswerScreen(
                categoryTitle = ui.headerTitle,
                step = ui.displayStep, total = ui.total, progress = ui.progress,
                question = q,
                inputText = shortAnswer,
                submitted = submitted,
                isCorrect = vm.isCurrentCorrect(),
                onInputChange = { vm.onEvent(QuizEvent.FillShort(it)) },
                onSubmit = { if (!submitted) vm.onEvent(QuizEvent.Submit) },
                onBack = { if (idx == 0) showExit = true else vm.onEvent(QuizEvent.Back) },
                showPrimaryButton = !submitted
            )
            null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // ✅ 추가 3: 종료 알럿 (기존 QuizExitAlert 써도 되고, 아래처럼 AlertDialog로 바로 구현해도 OK)
        if (showExit) {
            AlertDialog(
                onDismissRequest = { showExit = false },
                title = { Text("퀴즈를 종료할까요?") },
                text  = { Text("현재 진행 중인 퀴즈가 종료됩니다.") },
                confirmButton = {
                    TextButton(onClick = { showExit = false; onQuitToHome() }) {
                        Text("종료")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExit = false }) { Text("계속하기") }
                }
            )
        }

        SnackbarHost(snack, Modifier.align(Alignment.BottomCenter))
    }
}
