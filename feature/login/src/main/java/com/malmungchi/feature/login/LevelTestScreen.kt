package com.malmungchi.feature.login

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.core.model.LevelTestSubmitAnswer
import kotlin.random.Random
import androidx.activity.compose.BackHandler

// ----------------------------- Design Tokens -----------------------------
private val BrandBlue = Color(0xFF195FCF)
private val TrackGray = Color(0xFFE5E7EB)
private val Option4Bg = Color(0xFFEFF4FB)
private val DisabledBg = Color(0xFFFAFAFA)
private val BorderGray = Color(0xFFE6E6E6)
private val TextPrimary = Color(0xFF111111)
private val TextSecondary = Color(0xFF6B7280)

// ----------------------------- Model -----------------------------
data class LTQuestion(
    val index: Int,
    val text: String,
    val options: List<String>
)
enum class LTStage { S0, S1, S2, S3 }


// ----------------------------- New: Loading Screen -----------------------------
@Composable
fun LevelTestLoadingScreen(
    onBack: () -> Unit,
    onCancelClick: () -> Unit,
    // 서버 진행률(0f..1f). 알 수 없으면 null로 주면 자동 애니메이션
    progress: Float? = null,
) {
    val sidePadding = 20.dp
    val isDone = (progress ?: 0f) >= 1f

    // 진행률 연출: progress==null 이면 0→1을 반복 상승/감쇠(서서히 채워지는 느낌)
    val auto by rememberInfiniteProgress()
    val target = when {
        progress == null -> auto
        else -> progress.coerceIn(0f, 1f)
    }
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing),
        label = "loading_progress"
    )

    // 채워질 때 컬러: 진행 전(회색) → 완료 시(BrandBlue)
    val indicatorColor = if (isDone) BrandBlue else Color(0xFFE0E0E0)
    val trackColor = Color(0xFFF2F2F2)

    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = sidePadding)
    ) {
        // TopBar (좌측 20dp 맞추기 위해 padding 제거, 아래에서 직접 배치)
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "back",
                    tint = Color.Black
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // 일러스트 + 원형 진행
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_loading),
                contentDescription = "loading",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
            // 원형 진행 인디케이터를 이미지 위에 오버레이
            CircularProgressIndicator(
                progress = { animated },
                strokeWidth = 8.dp,
                modifier = Modifier.size(210.dp),
                color = indicatorColor,
                trackColor = trackColor
            )
        }

        Spacer(Modifier.height(28.dp))

        // 제목: "문제 생성 중 ..."
        Text(
            text = "문제 생성 중 ...",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        )

        Spacer(Modifier.height(12.dp))

        // 서브문구
        Text(
            text = "말뭉치 앱에서는 정해진 글감으로 나만의 글쓰기가 가능해요:)",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF989898)
            )
        )

        Spacer(Modifier.weight(1f))

        // "문제 생성 취소" (밑줄, E0E0E0, Medium 12)
        Text(
            text = "문제 생성 취소",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable(onClick = onCancelClick)
                .padding(vertical = 24.dp),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFFE0E0E0),
                // 밑줄
                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
            )
        )
    }
}

/** progress 파라미터가 없을 때 서서히 채워지는 반복 애니메이션 */
@Composable
private fun rememberInfiniteProgress(): State<Float> {
    var dir by remember { mutableStateOf(1f) }
    var value by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            val steps = 24
            repeat(steps) {
                value = (value + dir / steps).coerceIn(0f, 1f)
                kotlinx.coroutines.delay(30)
            }
            dir *= -1f
        }
    }
    // ✅ remember 로 감싸서 반환
    return remember { derivedStateOf { value } }
}

@Preview(showBackground = true)
@Composable
private fun Preview_LevelTestLoading_Auto() {
    LevelTestLoadingScreen(
        onBack = {},
        onCancelClick = {},
        progress = null // 진행률 모를 때(자동 서서히 채우기)
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_LevelTestLoading_Done() {
    LevelTestLoadingScreen(
        onBack = {},
        onCancelClick = {},
        progress = 1f // 완료 시 원형이 195FCF로
    )
}

// ----------------------------- New: Cancel Confirm Dialog -----------------------------
@Composable
fun CancelGenerationDialog(
    onDismiss: () -> Unit,
    onConfirmContinue: () -> Unit, // 계속하기
    onCancelAll: () -> Unit        // 취소하기
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "테스트를 마치지 않으면 학습을 진행할 수 없어요:(",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "정말 취소하시겠어요?",
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                )
            }
        },
        // 버튼 영역: 좌측 "취소하기"(F7F7F7), 우측 "계속하기"(195FCF)
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCancelAll,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF7F7F7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "취소하기",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    )
                }
                Button(
                    onClick = onConfirmContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "계속하기",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_CancelDialog() {
    CancelGenerationDialog(
        onDismiss = {},
        onConfirmContinue = {},
        onCancelAll = {}
    )
}

// ----------------------------- New: Halfway Screen (Full) -----------------------------
@Composable
fun HalfwayScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit
) {
    val side = 20.dp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = side)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "back",
                    tint = Color.Black
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            "절반 지났어요, 잘하고 있어요!",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = BrandBlue
            )
        )

        Spacer(Modifier.height(32.dp))

        // 이미지(가이드 예시). 실제 리소스가 따로 있으면 교체
        Image(
            painter = painterResource(id = R.drawable.img_malmungchi_logo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(220.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.5f)
                .padding(bottom = 40.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "계속하기",
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_HalfwayScreen() {
    HalfwayScreen(onBack = {}, onContinue = {})
}

// ----------------------------- Public API -----------------------------
@Composable
fun LevelTestScreen(
    userName: String,
    stage: LTStage,
    questions: List<LTQuestion>,
    onBack: () -> Unit,
    onGoStudy: () -> Unit,
    onSubmitAnswers: (answers: List<LevelTestSubmitAnswer>) -> Unit = {}
    //onSubmitAnswers: (answers: List<LevelTestSubmitAnswer>) -> Unit
) {
    var current by remember { mutableStateOf(0) } // 0..14


    // Nothing? 추론 방지
    val answers = remember(questions) {
        mutableStateListOf<String?>(*arrayOfNulls<String>(questions.size))
    }

    // 1) Dialog 대신 화면 전환용 state
    var showHalfScreen by remember { mutableStateOf(false) }

    // (선택) HalfwayScreen 한 번만 띄우고 싶다면 사용
    var halfShown by remember { mutableStateOf(false) } // <-- NEW

// ✅ 공통 뒤로가기 로직
    val handleBack: () -> Unit = {
        when {
            showHalfScreen -> showHalfScreen = false        // HalfwayScreen 떠있으면 닫기
            current > 0 -> current -= 1                      // 첫 문제가 아니면 이전 문제
            else -> onBack()                                 // 첫 문제면 라우팅 back
        }
    }

// ✅ 하드웨어 뒤로가기
    BackHandler(onBack = handleBack)

    // 2) 절반 화면 가드 (다이얼로그 X, 풀스크린)
    if (showHalfScreen) {
        HalfwayScreen(
           // onBack = { showHalfScreen = false },   // 또는 onBack()
            onBack = handleBack,
            onContinue = { showHalfScreen = false }
        )
        return                                   // 이 컴포저블에서 조기 종료
    }

//    var showHalfDialog by remember { mutableStateOf(false) }
//    var finished by remember { mutableStateOf(false) }
//
//    val levelLabel = remember(stage) { stageToLabel(stage) }
//
//    if (finished) {
//        ResultView(userName = userName, levelLabel = levelLabel, onGoStudy = onGoStudy)
//        return
//    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ic_back: 좌우 16 정렬 + 상단 16 여유
//        TopBar(onBack = onBack)
        TopBar(onBack = handleBack)

        // 1/15 → 프로그레스바(1.2배 두께) 순서
        CountLabel(text = "${current + 1}/${questions.size}")

        ProgressBar(progress = (current + 1).toFloat() / questions.size.toFloat())

        QuestionBlock(
            question = questions[current],
            selected = answers[current],
            onSelect = { answers[current] = it }
        )

        Spacer(Modifier.weight(1f))

        SubmitBar(
            isLast = current == questions.lastIndex,
            enabled = answers[current] != null,
            onClick = {
                if (current == questions.lastIndex) {
                    // ✅ 서버 제출 페이로드 구성
                    val payload = questions.mapIndexed { i, q ->
                        LevelTestSubmitAnswer(
                            questionIndex = q.index,         // LTQuestion.index 사용
                            choice = answers[i]!!             // null 아님 (enabled로 보장)
                        )
                    }
                    onSubmitAnswers(payload)                 // ✅ 외부로 제출 전달
                    //finished = true                          // 결과 뷰 전환(서버가 +1 레벨 처리)
                } else {
//                    current += 1
//                    if (current == 7) showHalfScreen = true
////                    if (current == 7) showHalfDialog = true
                    current += 1
                    if (current == 7 && !halfShown) {
                        showHalfScreen = true
                        halfShown = true
                    }
                }
            }
        )
    }

//    if (showHalfDialog) {
//        HalfwayDialog(onDismiss = { showHalfDialog = false })
//    }
}

// ----------------------------- UI Pieces -----------------------------
@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 0.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "back",
                tint = Color.Black
            )
        }
    }
}

@Composable
private fun ProgressBar(progress: Float) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        // 원하는 느낌으로 조절 가능 (딱딱함 ↓, 자연스러움 ↑)
        animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
        label = "level_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)               // 두께
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(999.dp)) // 트랙 pill 모양
            .background(TrackGray)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)          // 0→1 사이로 부드럽게 늘어남
                .clip(RoundedCornerShape(999.dp))// 헤드도 둥글게
                .background(BrandBlue)
        )
    }
}
@Composable
private fun CountLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        style = TextStyle(
            fontFamily = Pretendard,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    )
}

@Composable
private fun QuestionBlock(
    question: LTQuestion,
    selected: String?,
    onSelect: (String) -> Unit
) {
    // 문제
    Text(
        text = question.text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        style = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = TextPrimary
        )
    )

    // 문제-보기 간격 늘림(상단 24dp), 좌우 16 유지
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        question.options.take(4).forEachIndexed { idx, opt ->
            val isSelected = selected == opt
            val bg = if (isSelected) BrandBlue.copy(alpha = 0.10f) else Color.White
//            val bg = when {
//                isSelected -> BrandBlue.copy(alpha = 0.10f)
//                idx == 3 -> Option4Bg
//                else -> Color.White
//            }
            val border = if (isSelected) BrandBlue else BorderGray

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bg)
                    .border(1.dp, border, RoundedCornerShape(14.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = opt,
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = if (isSelected) BrandBlue else TextPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.SubmitBar(
    isLast: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .align(Alignment.CenterHorizontally) // 중앙
            .fillMaxWidth(0.5f)                  // 1.5배 확대(33% → 50%)
            .padding(start = 16.dp, end = 16.dp, bottom = 48.dp)
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) BrandBlue else DisabledBg,
            disabledContainerColor = DisabledBg,
            contentColor = if (enabled) Color.White else TextSecondary,
            disabledContentColor = TextSecondary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = if (isLast) "결과 보기" else "정답 제출",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        )
    }
}

@Composable
private fun HalfwayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            // 버튼 중앙
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "계속하기",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )
                }
            }
        },
        title = {
            Text(
                "절반 지났어요, 잘하고 있어요!",
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = BrandBlue // 제목 컬러 195FCF
                ),
                textAlign = TextAlign.Center // 가운데 정렬
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.img_malmungchi_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White // 다이얼로그 배경 흰색
    )
}

@Composable
private fun ResultView(
    userName: String,
    levelLabel: String,
    onGoStudy: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 텍스트 덩어리 자체가 화면 가운데
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    append("${userName}님의 현재 수준은\n‘")

                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF195FCF), // 레벨 부분만 파란색
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp
                        )
                    ) {
                        append(levelLabel)
                    }

                    append("’입니다")
                },
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = TextPrimary, // 기본 글씨색 (검정)
                    lineHeight = 30.sp
                )
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = "어휘/문해력을 키우기 위한 첫걸음!\n말뭉치와 함께 키워가볼까요?",
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp
                )
            )
        }

        // CTA 버튼: 제출 버튼과 동일한 위치/크기(중앙, 50%, 하단 48)
        Button(
            onClick = onGoStudy,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.5f)
                .padding(start = 16.dp, end = 16.dp, bottom = 48.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "학습하러 가기",
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            )
        }
    }
}

// ----------------------------- Helper -----------------------------
private fun stageToLabel(stage: LTStage): String = when (stage) {
    LTStage.S0 -> "기초"
    LTStage.S1 -> "활용"
    LTStage.S2 -> "심화"
    LTStage.S3 -> "고급"
}

// ----------------------------- Previews -----------------------------
@Preview(showBackground = true)
@Composable
private fun Preview_LevelTest_Main() {
    val sample = (1..15).map { i ->
        LTQuestion(
            index = i,
            text = "다른 사람의 감정을 이해 및 공감하는 능력을 뜻하는 단어는?",
            options = listOf("공감", "직관", "분석", "판단")
        )
    }
    LevelTestScreen(
        userName = "00",
        stage = LTStage.S0,
        questions = sample,
        onBack = {},
        onGoStudy = {},
        onSubmitAnswers = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_HalfwayDialog() {
    HalfwayDialog(onDismiss = {})
}

@Preview(showBackground = true)
@Composable
private fun Preview_ResultView() {
    ResultView(
        userName = "채영",
        levelLabel = "기초",
        onGoStudy = {}
    )
}

@Composable
fun LevelTestRoute(
    userName: String,
    stageInt: Int,                    // 0~3
    onBack: () -> Unit,
    onGoStudy: () -> Unit,
    viewModel: LevelTestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(stageInt) { viewModel.load(stageInt) }

    when (val s = state) {
//        is LevelTestState.Idle,
//        is LevelTestState.Loading -> BoxFullScreenLoading()

        is LevelTestState.Idle,
        is LevelTestState.Loading -> {
            LevelTestLoadingScreen(
                onBack = onBack,
                onCancelClick = { /* show CancelGenerationDialog = true */ },
                //progress = s.progress ?: null  // ViewModel에서 진행률 제공 가능 시
                progress = null
            )
            if (/* cancelDialogVisible */ false) {
                CancelGenerationDialog(
                    onDismiss = { /* hide */ },
                    onConfirmContinue = { /* hide */ },
                    onCancelAll = { onBack() } // 전체 취소 → 뒤로
                )
            }
        }

        is LevelTestState.Error -> BoxFullScreenError(
            message = s.message,
            onRetry = { viewModel.load(stageInt) },
            onBack = onBack
        )

        is LevelTestState.Questions -> {
            //val uiQuestions = s.items.toLTQuestions()
            val uiQuestions = remember(s.items) { s.items.toLTQuestions() }
            LevelTestScreen(
                userName = userName,
                stage = stageInt.toLTStage(),
                questions = uiQuestions,
                onBack = onBack,
                onGoStudy = onGoStudy,
                onSubmitAnswers = { payload -> viewModel.submit(payload) }
            )
        }

        is LevelTestState.SubmitDone -> {
            // ✅ 서버 응답의 resultLevel을 그대로 ResultView에 전달!
            ResultView(
                userName = userName,
                levelLabel = s.resultLevel,
                onGoStudy = onGoStudy
            )
        }
    }
}

// core → UI
private fun List<com.malmungchi.core.model.LevelTestQuestion>.toLTQuestions(): List<LTQuestion> =
    mapIndexed { idx, q ->
        val key = q.questionIndex ?: (idx + 1)
        LTQuestion(
            index = key,
            text = q.question,
            options = q.options.shuffled(Random(key))
        )
    }

// Int(0..3) → LTStage
private fun Int.toLTStage(): LTStage = when (this) {
    0 -> LTStage.S0
    1 -> LTStage.S1
    2 -> LTStage.S2
    3 -> LTStage.S3
    else -> LTStage.S0
}

@Composable
private fun BoxFullScreenLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BoxFullScreenError(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message)
            Spacer(Modifier.height(12.dp))
            Row {
                Button(onClick = onBack) { Text("뒤로") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onRetry) { Text("다시 시도") }
            }
        }
    }
}