package com.malmungchi.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.core.model.quiz.McqStep
import com.malmungchi.core.model.quiz.OxStep
import com.malmungchi.core.model.quiz.ShortStep
import kotlinx.coroutines.delay

@Composable
fun QuizSolveHost(
    vm: QuizFlowViewModel,
    onQuitToHome: () -> Unit,   // 중단 확정 시 QuizScreen으로
    onAllFinished: () -> Unit,  // 7문항 완료 시
    postSubmitDelayMs: Long = 1000L
) {
    val ui by vm.ui.collectAsState()

    // ✅ 선택 상태를 StateFlow로 “구독”
    val selectedMcq  by vm.currentMcq.collectAsState()
    val selectedOx   by vm.currentOx.collectAsState()
    val shortAnswer  by vm.currentShort.collectAsState()

    val submitted = vm.currentSubmitted()
    val idx = ui.index
    val isLast = vm.isLastStep()

    // 중단 알럿
    var showExit by remember { mutableStateOf(false) }
    if (showExit) {
        QuizExitAlert(
            visible = true,
            onConfirmQuit = { showExit = false; onQuitToHome() },
            onContinue = { showExit = false }
        )
    }

    // 시스템 뒤로키: 첫 문항이면 알럿, 아니면 이전 문항
    BackHandler(enabled = true) {
        if (idx == 0) showExit = true else vm.onEvent(QuizEvent.Back)
    }

    // 1초 정답 플래시 + 자동 다음
    var flash: Boolean? by remember { mutableStateOf(null) }
    LaunchedEffect(submitted, idx) {
        if (submitted) {
            // 서버 채점 결과 우선, 없으면 로컬 판정
            val ok = runCatching { vm.currentServerCorrectness() }.getOrNull() ?: vm.isCurrentCorrect()
            flash = ok
            delay(postSubmitDelayMs)
            if (!isLast) vm.onEvent(QuizEvent.Next) else onAllFinished()
            flash = null
        }
    }

    // 화면 스위칭 (MCQ → OX → SHORT)
    Box(Modifier.fillMaxSize()) {
        when (val q = ui.current) {
            is McqStep -> QuizMcqScreen(
                categoryTitle = ui.headerTitle,
                step = ui.displayStep, total = ui.total, progress = ui.progress,
                question = q,
                selectedOptionId = selectedMcq,
                submitted = submitted,
                onSelect = { vm.onEvent(QuizEvent.SelectMcq(it)) },
                onSubmit = { if (!submitted) vm.onEvent(QuizEvent.Submit) }, // 제출 → 1초 후 자동 이동
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

        // 정답/오답 플래시(오버레이)
        if (flash != null) AnswerFlashOverlay(isCorrect = flash!!)
    }
}

@Composable
private fun AnswerFlashOverlay(isCorrect: Boolean) {
    // 간단한 중앙 플로팅 배지
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = if (isCorrect) Color(0xE6195FCF) else Color(0xE6FF0D0D),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 12.dp
        ) {
            Text(
                text = if (isCorrect) "정답!" else "오답!",
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                fontFamily = Pretendard,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
