package com.malmungchi.feature.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.core.model.quiz.McqStep
import com.malmungchi.core.model.quiz.OxStep
import com.malmungchi.core.model.quiz.QuizCategory
import com.malmungchi.core.model.quiz.QuizOption
import com.malmungchi.core.model.quiz.QuizSet
import com.malmungchi.core.model.quiz.ShortStep
import com.malmungchi.core.model.quiz.Submission
import com.malmungchi.core.repository.QuizRepository


private val BrandBlue = Color(0xFF195FCF)

@Composable
fun QuizRetryHost(
    vm: QuizFlowViewModel,
    onFinish: (correctCount: Int) -> Unit,
    onBack: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    // ★★★ [추가] finished 상태면 결과 화면을 바로 렌더하고 조기 return
    if (ui.finished) {
        QuizRetryAllResultScreen(
            categoryTitle = ui.headerTitle,
            results = vm.buildRetryResultItems(),
            onBack = onBack,
            onFinishClick = { onFinish(ui.correctCount) }
        )
        return
    }

    val step = ui.displayStep
    val total = ui.total
    val progress = ui.progress

    // ★★★ 추가: 선택 상태를 StateFlow로 구독해야 클릭 시 UI가 갱신됩니다.
    val selectedMcq  by vm.currentMcq.collectAsState()
    val selectedOx   by vm.currentOx.collectAsState()
    val shortAnswer  by vm.currentShort.collectAsState()

    val submittedRaw = vm.currentSubmitted()
    // ★ Fix: 리트라이에서도 제출 상태 그대로 쓰기
    val submitted = vm.currentSubmitted()
    //val submitted = if (vm.isRetryMode()) false else submittedRaw
    val isLast = vm.isLastStep()

    Box(Modifier.fillMaxSize().background(Color.White)) {
        when (val q = ui.current) {
            is McqStep -> QuizMcqScreen(
                categoryTitle = ui.headerTitle,
                step = step, total = total, progress = progress,
                question = q,
                selectedOptionId = selectedMcq,
                submitted = submitted,
                onSelect = { vm.onEvent(QuizEvent.SelectMcq(it)) },
                onSubmit = { vm.onEvent(QuizEvent.Submit) },
                onBack = { vm.onEvent(QuizEvent.Back) },
                showPrimaryButton = !submitted
            )
            is OxStep -> QuizOxScreen(
                categoryTitle = ui.headerTitle,
                step = step, total = total, progress = progress,
                question = q,
                selectedIsO = selectedOx,
                submitted = submitted,
                onSelect = { vm.onEvent(QuizEvent.SelectOx(it)) },
                onSubmit = { vm.onEvent(QuizEvent.Submit) },
                onBack = { vm.onEvent(QuizEvent.Back) },
                showPrimaryButton = !submitted
            )
            is ShortStep -> QuizShortAnswerScreen(
                categoryTitle = ui.headerTitle,
                step = step, total = total, progress = progress,
                question = q,
                inputText = shortAnswer,
                submitted = submitted,
                isCorrect = vm.isCurrentCorrect(),
                onInputChange = { vm.onEvent(QuizEvent.FillShort(it)) },
                onSubmit = { vm.onEvent(QuizEvent.Submit) },
                onBack = { vm.onEvent(QuizEvent.Back) },
                showPrimaryButton = !submitted,
                hideNextAfterSubmit = true            // 선택(다음 문제 라벨까지 숨길 때)
            )
            null -> {}
        }

        // ===== 제출 후 하단 액션(해설/다음 or 결과) =====
        if (submitted) {
            BottomActionBar(
                showExplanation = ui.showExplanation,
                isLast = isLast,
                onShowExplanation = { vm.onEvent(QuizEvent.ShowExplanation) },
                onNext = {
                    if (isLast) onFinish(ui.correctCount)
                    else vm.onEvent(QuizEvent.Next)           // ✅ Submit 말고 Next!
                },
                onConfirmExplanation = {
                    vm.onEvent(QuizEvent.HideExplanation)
                    if (isLast) onFinish(ui.correctCount)
                    else vm.onEvent(QuizEvent.Next)           // ✅ 해설 확인 후에도 Next
                }
            )
        }
    }

    if (ui.showExplanation) {
        ExplanationDialog(
            text = ui.explanation.orEmpty(),
            onDismiss = { vm.onEvent(QuizEvent.HideExplanation) },
            onConfirm = { vm.onEvent(QuizEvent.HideExplanation) }
        )
    }
}

@Composable
fun ExplanationDialog(
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val lines = text.trim().lines()
    val titleLine = lines.firstOrNull()?.trim().orEmpty()
    val body = lines.drop(1).joinToString("\n").trim()

    AlertDialog(
        onDismissRequest = onDismiss,
        // center + 60% width confirm button
        containerColor = Color.White,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)   // ✅ 60% width
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        "확인했어요",
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        },
        // no dismiss button
        title = {
            if (titleLine.isNotEmpty()) {
                Text(
                    text = titleLine,                       // ex) 정답은 [공감]입니다!
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,       // ✅ SemiBold
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                    // ✅ centered
                )
            }
        },
        text = {
            if (body.isNotEmpty()) {
                Text(
                    text = body,                             // commentary
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,         // ✅ Medium
                    color = Color.Black,
                    textAlign = TextAlign.Center ,
                    modifier = Modifier.fillMaxWidth() // ✅ centered
                )
            }
        }
    )
}

@Composable
private fun BottomActionBar(
    showExplanation: Boolean,
    isLast: Boolean,
    onShowExplanation: () -> Unit,
    onNext: () -> Unit,
    onConfirmExplanation: () -> Unit
) {
    // 화면 제일 아래에 "불투명 바"를 깔아 아래 버튼을 완전히 가림
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Surface(
            color = Color.White,            // ✅ 투명 X, 하얀 배경으로 덮어쓰기
            shadowElevation = 12.dp          // (선택) 살짝 그림자
        ) {
            if (!showExplanation) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 48.dp) // 공통 패딩
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val btnMod = Modifier.weight(1f).height(48.dp)

                    OutlinedButton(
                        onClick = onShowExplanation,
                        modifier = btnMod,
                        border = BorderStroke(2.dp, BrandBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = BrandBlue
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text("해설 보기", fontFamily = Pretendard, fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold, color = BrandBlue)
                    }

                    Button(
                        onClick = onNext,
                        modifier = btnMod, // ✅ 동일
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue, contentColor = Color.White),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Text(if (isLast) "결과 보기" else "다음 문제",
                            fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 20.dp, vertical = 16.dp)
//                        .navigationBarsPadding(), // 하단 시스템바 피하기
//                    horizontalArrangement = Arrangement.spacedBy(12.dp)
//                ) {
//                    OutlinedButton(
//                        onClick = onShowExplanation,
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(48.dp),
//                        border = BorderStroke(2.dp, BrandBlue),
//                        colors = ButtonDefaults.outlinedButtonColors(
//                            containerColor = Color.Transparent,
//                            contentColor = BrandBlue
//                        ),
//                        shape = MaterialTheme.shapes.extraLarge
//                    ) {
//                        Text(
//                            "해설 보기",
//                            fontFamily = Pretendard,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = BrandBlue
//                        )
//                    }
//
//                    Button(
//                        onClick = onNext,
//                        modifier = Modifier
//                            .weight(1f)
//                            .height(48.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = BrandBlue,
//                            contentColor = Color.White
//                        ),
//                        shape = MaterialTheme.shapes.extraLarge
//                    ) {
//                        Text(
//                            if (isLast) "결과 보기" else "다음 문제",
//                            fontFamily = Pretendard,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//            } else {
//                Button(
//                    onClick = onConfirmExplanation,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 48.dp)
//                        .navigationBarsPadding()
//                        .height(48.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = BrandBlue,
//                        contentColor = Color.White
//                    ),
//                    shape = MaterialTheme.shapes.extraLarge
//                ) {
//                    Text(
//                        "확인했어요",
//                        fontFamily = Pretendard,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold
//                    )
//                }
//            }
        }
    }
}

/* ===== 프리뷰 더미 ===== */
private fun previewOriginalSet(): QuizSet = QuizSet(
    id = "demo",
    category = QuizCategory.JOB_PREP,
    steps = listOf(
        McqStep(
            id = "q1",
            text = "다른 사람의 감정을 이해 및 공감하는 능력을 뜻하는 단어는?",
            options = listOf(
                QuizOption(1, "공감"), QuizOption(2, "직관"),
                QuizOption(3, "분석"), QuizOption(4, "판단")
            ),
            correctOptionId = 1
        ),
        OxStep(
            id = "q2",
            statement = "‘기꺼이 남을 위하여 자신을 희생하는 마음가짐’을 뜻하는 단어는 ‘이기심’이다.",
            answerIsO = false
        ),
        ShortStep(
            id = "q3",
            guide = "밑줄 친 단어를 격식 있게 바꿔 쓰세요.",
            sentence = "오늘 안에 보내줄게.",
            underlineText = "오늘",
            answerText = "금일"
        )
    )
)



/* (A) 이전 단계(2/2, 미제출) */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "재도전 · 이전 단계(제출 전)")
@Composable
private fun Preview_Retry_Previous_Step() {
    MaterialTheme {
        Surface(color = Color.White) {
            val retrySet = buildRetrySetFromWrong(previewOriginalSet(), listOf("q1", "q3"))
            val vm = remember(/*retrySet.id + "_prev"*/ "retry_prev", retrySet) {
            QuizFlowViewModel().apply {
                onEvent(QuizEvent.LoadWithSet(retrySet))
                onEvent(QuizEvent.SelectMcq(3))
                onEvent(QuizEvent.Submit)
                onEvent(QuizEvent.Next)
            }
        }
            QuizRetryHost(vm = vm, onFinish = {}, onBack = {})
        }
    }
}

/* (B) 현재 단계: 제출 후(해설/다음 노출) */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "재도전 · 제출 후(해설/다음)")
@Composable
private fun Preview_Retry_AfterSubmit() {
    MaterialTheme {
        Surface(color = Color.White) {
            val retrySet = buildRetrySetFromWrong(previewOriginalSet(), listOf("q1", "q3"))
            val vm = remember(/*retrySet.id + "_after"*/ "retry_after", retrySet) {
                QuizFlowViewModel().apply {
                    onEvent(QuizEvent.LoadWithSet(retrySet))
                    onEvent(QuizEvent.SelectMcq(3))
                    onEvent(QuizEvent.Submit)
                }
            }
            QuizRetryHost(vm = vm, onFinish = {}, onBack = {})
        }
    }
}

/* (C) 해설 모달 오픈 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "재도전 · 해설 모달")
@Composable
private fun Preview_Retry_Explanation_Open() {
    MaterialTheme {
        Surface(color = Color.White) {
            val retrySet = buildRetrySetFromWrong(previewOriginalSet(), listOf("q1", "q3"))
            val vm = remember(/*retrySet.id + "_exp"*/ "retry_exp", retrySet) {
                QuizFlowViewModel().apply {
                    onEvent(QuizEvent.LoadWithSet(retrySet))
                    onEvent(QuizEvent.SelectMcq(3))
                    onEvent(QuizEvent.Submit)
                    onEvent(QuizEvent.ShowExplanation)
                }
            }
            QuizRetryHost(vm = vm, onFinish = {}, onBack = {})
        }
    }
}

/* (D) 마지막 문항 제출 후: 결과 보기 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "재도전 · 마지막(결과 보기)")
@Composable
private fun Preview_Retry_Last_Step_Result() {
    MaterialTheme {
        Surface(color = Color.White) {
            val lastOnly = buildRetrySetFromWrong(previewOriginalSet(), listOf("q3"))
            val vm = remember(/*lastOnly.id + "_last"*/ "retry_last", lastOnly) {
                QuizFlowViewModel().apply {
                    onEvent(QuizEvent.LoadWithSet(lastOnly))
                    onEvent(QuizEvent.FillShort("금일"))
                    onEvent(QuizEvent.Submit)
                }
            }
            QuizRetryHost(vm = vm, onFinish = { }, onBack = {})
        }
    }
}

