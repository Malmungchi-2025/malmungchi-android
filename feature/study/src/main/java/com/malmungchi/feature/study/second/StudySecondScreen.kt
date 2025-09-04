package com.malmungchi.feature.study.second

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.StudyReadingViewModel
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

/* ---------- 색상/상수 ---------- */
private val Blue_195FCF = Color(0xFF195FCF)
private val Film_50 = Color.Black.copy(alpha = 0.5f)
private val Field_EFF4FB = Color(0xFFEFF4FB)
private val Yellow_FFD91C = Color(0xFFFFD91C)
private val Gray_989898 = Color(0xFF989898)
private val Gray_616161 = Color(0xFF616161)
private val Red_FF0D0D = Color(0xFFFF0D0D)

/* ✅ 온보딩 예시 문구: 큰따옴표 포함 */
private const val GuideSentence =
    "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀"

/* ---------- 온보딩 단계 ---------- */
private enum class GuideOverlayStep { Step1, Step2, None }

/* ---------- 안전 로더 ---------- */
@Composable
private fun painterResourceSafe(id: Int?): Painter? {
    if (id == null) return null
    return runCatching { painterResource(id) }.getOrNull()
}

/* ---------- 상단바 ---------- */
@Composable
fun TopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            val backPainter = painterResourceSafe(R.drawable.btn_img_back)
            if (backPainter != null) {
                Icon(
                    painter = backPainter,
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color(0x33000000), RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            color = Color.Black
        )

        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun OriginalWithHeHighlight(
    text: String,
    baseColor: Color = Color.White,
    heColor: Color = Yellow_FFD91C,
) {
    val annotated = buildAnnotatedString {
        var wiheCount = 0
        var i = 0
        while (i < text.length) {
            if (i <= text.lastIndex - 1 && text[i] == '위' && text[i + 1] == '해') {
                wiheCount++
                // "위"는 기본색
                append(AnnotatedString("위", SpanStyle(color = baseColor)))
                // 첫 번째 "위해"의 "해"만 노란색, 그 이후는 흰색
                append(
                    AnnotatedString(
                        "해",
                        SpanStyle(color = if (wiheCount == 1) heColor else baseColor)
                    )
                )
                i += 2
            } else {
                append(AnnotatedString(text[i].toString(), SpanStyle(color = baseColor)))
                i++
            }
        }
    }

    Text(
        text = annotated,
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 26.sp
    )
}

/* ---------- 진행 바 ---------- */
@Composable
fun StepProgressBarPreview(totalSteps: Int = 3, currentStep: Int = 2) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .background(
                        color = if (index < currentStep) Blue_195FCF else Color(0xFFF2F2F2),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

/* ---------- 문장 카운터 ---------- */
@Composable
private fun SentenceCounter(current: Int, total: Int,  modifier: Modifier = Modifier) {
    Text(
        text = "$current/$total",
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Gray_989898,
        modifier = modifier
    )
}

/* ---------- 원문 + 오탈자 하이라이트 ---------- */
@Composable
private fun OriginalWithTypos(
    original: String,
    typed: String,
    errorIndex: Int? = null,
    color: Color = Color.Black,
) {
    val match = typed.commonPrefixWith(original).length
    val annotated = buildAnnotatedString {
        append(AnnotatedString(original.take(match), SpanStyle(color = color)))
        if (errorIndex != null && errorIndex in original.indices) {
            append(AnnotatedString(original.substring(match, errorIndex), SpanStyle(color = color)))
            append(AnnotatedString(original[errorIndex].toString(), SpanStyle(color = Red_FF0D0D)))
            append(AnnotatedString(original.drop(errorIndex + 1), SpanStyle(color = color)))
        } else {
            append(AnnotatedString(original.drop(match), SpanStyle(color = color)))
        }
    }
    Text(
        text = annotated,
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 26.sp
    )
}

/* ---------- 오버레이(필름 + 말풍선) ---------- */
@Composable
private fun HandwritingGuideOverlay(
    step: GuideOverlayStep,
    onTapAnywhere: () -> Unit,
    modifier: Modifier = Modifier,

    // 말풍선 위치/크기
    bubble1OffsetX: Dp = 0.dp,
    bubble1OffsetY: Dp = 0.dp,
    bubble2OffsetX: Dp = 0.dp,
    bubble2OffsetY: Dp = 0.dp,
    bubble1Width: Dp? = null,
    bubble2Width: Dp? = null,

    // 화살표 위치/크기
    arrowOffsetX: Dp = (-24).dp,
    arrowOffsetY: Dp = (-40).dp,
    arrowSize: Dp = 48.dp
) {
    if (step == GuideOverlayStep.None) return

    val isPreview = LocalInspectionMode.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Film_50)
            // 프리뷰에선 탭 제스처만 막고(이미지 렌더는 허용)
            .then(
                if (isPreview) Modifier
                else Modifier.pointerInput(step) { detectTapGestures { onTapAnywhere() } }
            )
            .zIndex(10f)
    ) {
        val showArrow = when (step) {
            GuideOverlayStep.Step1 -> true
            GuideOverlayStep.Step2 -> false
            GuideOverlayStep.None  -> false
        }
        val bubbleRes = when (step) {
            GuideOverlayStep.Step1 -> R.drawable.img_word1
            GuideOverlayStep.Step2 -> R.drawable.img_word2
            GuideOverlayStep.None  -> null
        }

        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 96.dp) // 좌측 20 라인 유지
        ) {
            if (bubbleRes != null) {
                val base = Modifier
                    .wrapContentSize()
                    .let {
                        if (step == GuideOverlayStep.Step1) it.offset(bubble1OffsetX, bubble1OffsetY)
                        else it.offset(bubble2OffsetX, bubble2OffsetY)
                    }
                    .let {
                        when {
                            step == GuideOverlayStep.Step1 && bubble1Width != null -> it.width(bubble1Width)
                            step == GuideOverlayStep.Step2 && bubble2Width != null -> it.width(bubble2Width)
                            else -> it
                        }
                    }

                val bubblePainter = painterResourceSafe(bubbleRes)
                if (bubblePainter != null) {
                    Image(
                        painter = bubblePainter,
                        contentDescription = null,
                        modifier = base
                    )
                } else {
                    // 플레이스홀더 (리소스 이슈 시)
                    Box(
                        base
                            .height(80.dp)
                            .width(240.dp)
                            .background(Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    )
                }
            }

            if (showArrow) {
                val arrowMod = Modifier
                    .align(Alignment.TopEnd)
                    .offset(arrowOffsetX, arrowOffsetY)
                    .size(arrowSize)

                val arrowPainter = painterResourceSafe(R.drawable.img_arrow)
                if (arrowPainter != null) {
                    Icon(
                        painter = arrowPainter,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = arrowMod
                    )
                } else {
                    Box(arrowMod.background(Color(0x55FFFFFF), RoundedCornerShape(8.dp)))
                }
            }
        }

        val malchiMod = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 20.dp, bottom = 40.dp)
            .size(84.dp)

        val malchiPainter = painterResourceSafe(R.drawable.img_malchi)
        if (malchiPainter != null) {
            Icon(
                painter = malchiPainter,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = malchiMod
            )
        } else {
            Box(malchiMod.background(Color(0x55FFFFFF), RoundedCornerShape(12.dp)))
        }
    }
}

/* ---------- 풀폭 입력칩 ---------- */
@Composable
private fun FullWidthInputChip(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    textColor: Color,
    background: Color,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = textColor,
            lineHeight = 26.sp
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(background)
                    .padding(paddingValues = contentPadding)
                    //.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                    Text(
                        placeholder,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Gray_989898
                    )
                }
                inner()
            }
        }
    )
}

/* ---------- 하단 버튼 ---------- */
@Composable
private fun BottomNavigationButtons(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onBackClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue_195FCF),
            modifier = Modifier
                .height(42.dp)
                .width(160.dp)
        ) { Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard) }

        Button(
            onClick = onNextClick,
            colors = ButtonDefaults.buttonColors(containerColor = Blue_195FCF),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .height(42.dp)
                .width(160.dp)
        ) { Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White) }
    }
}

/* ---------- 본 화면 ---------- */
@Composable
fun StudySecondScreen(
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    val sentences by viewModel.sentences.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()

    var overlayStep by rememberSaveable { mutableStateOf(GuideOverlayStep.Step1) }
    var errorIndex by remember { mutableStateOf<Int?>(null) }

    var contentTop by remember { mutableStateOf(Offset.Zero) }
    var anchorPos by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(errorIndex) { if (errorIndex != null) { delay(650); errorIndex = null } }

    val listState = rememberLazyListState()
    LaunchedEffect(currentIndex) { listState.animateScrollToItem(currentIndex, -20) }

    LaunchedEffect(Unit) {
        viewModel.initHandwritingStudy()
        viewModel.fetchHandwriting()
    }

    val sentenceFromVm = sentences.getOrNull(currentIndex).orEmpty()
    // ✅ 온보딩 단계에선 가이드 문장을 강제 노출
    val displaySentence =
        if (overlayStep == GuideOverlayStep.None) sentenceFromVm else GuideSentence

    Box(Modifier.fillMaxSize().background(Color.White)) {

        /* ----------------- ① 기본 컨텐츠 레이어 (z=0) ----------------- */
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 48.dp)
                .onGloballyPositioned { contentTop = it.positionInRoot() }
                .zIndex(0f)
        ) {
            TopBar(title = "오늘의 학습", onBackClick = onBackClick)

            Spacer(Modifier.height(24.dp))
            Text(
                "학습 진행률",
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(Modifier.height(16.dp))
            StepProgressBarPreview(totalSteps = 3, currentStep = 2)

            if (overlayStep == GuideOverlayStep.None) {
                Spacer(Modifier.height(40.dp))
                SentenceCounter(
                    current = (currentIndex + 1).coerceAtMost(sentences.size.coerceAtLeast(1)),
                    total = sentences.size.coerceAtLeast(1)
                )
                Spacer(Modifier.height(32.dp))
            } else {
                // 온보딩 중일 땐 카운터 숨기기
                Spacer(Modifier.height(40.dp))
            }

            Box(
                Modifier
                    .height(1.dp)
                    .onGloballyPositioned { anchorPos = it.positionInRoot() }
            )

            when (overlayStep) {
                GuideOverlayStep.Step1 -> {
                    OriginalWithTypos(
                        displaySentence,
                        typed = "",
                        errorIndex = null,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(12.dp))
                    FullWidthInputChip(
                        value = "",
                        onValueChange = {},
                        placeholder = null,
                        textColor = Gray_616161,
                        background = Field_EFF4FB,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                    )

                }
                GuideOverlayStep.Step2 -> { /* 필름 위 전용 레이어에서 그림 */ }
                GuideOverlayStep.None -> {
                    // 온보딩 종료 후 일반 화면에선 VM 문장을 그대로
                    OriginalWithTypos(
                        sentenceFromVm,
                        typed = viewModel.getInputFor(currentIndex),
                        errorIndex = null,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(12.dp))
                    FullWidthInputChip(
                        value = viewModel.getInputFor(currentIndex),
                        onValueChange = {},
                        placeholder = null,
                        textColor = Gray_616161,
                        background = Field_EFF4FB
                    )
                }
            }
        }

        /* ----------------- ② 필름 + 말풍선 레이어 (z=10) ----------------- */
        HandwritingGuideOverlay(
            step = overlayStep,
            onTapAnywhere = {
                overlayStep = when (overlayStep) {
                    GuideOverlayStep.Step1 -> GuideOverlayStep.Step2
                    GuideOverlayStep.Step2 -> GuideOverlayStep.None
                    GuideOverlayStep.None -> GuideOverlayStep.None
                }
            },
            modifier = Modifier.matchParentSize(),

            // ✅ 말풍선을 오른쪽 +40dp, 위로 -30dp 이동
            bubble1OffsetX = 40.dp,
            bubble1OffsetY = (-30).dp,
            bubble2OffsetX = 40.dp,
            bubble2OffsetY = (-30).dp,

            bubble1Width   = null,
            bubble2Width   = null,
            arrowOffsetX   = (40).dp,
            arrowOffsetY   = (-100).dp,
            arrowSize      = 48.dp
        )

        /* ----------------- ③ Step2 전용: ‘필름 위’ 고정 위치 레이어 (z=20) ----------------- */
        if (overlayStep == GuideOverlayStep.Step2) {
            val density = LocalDensity.current
            val dy = with(density) { (anchorPos.y - contentTop.y).toDp() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // ✅ 왼쪽 여백을 기존 20dp에서 4dp 더 줌 → 24.dp
                    .padding(start = 24.dp, end = 20.dp, top = 32.dp, bottom = 48.dp)
                    .absoluteOffset(x = 0.dp, y = dy + 148.dp)
                    .zIndex(20f)
            ) {
                // ✅ 전체 흰색 + ‘해’만 노란색
                OriginalWithHeHighlight(text = displaySentence)

                Spacer(Modifier.height(12.dp))

                // ✅ "위해" → "위헤", 여백 살짝 확대(vertical=10.dp)
                FullWidthInputChip(
                    value = "“빛을 보기 위헤",
                    onValueChange = {},
                    placeholder = null,
                    textColor = Color.Black,
                    background = Color.White,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}

/* ---------- 프리뷰 ---------- */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360)
@Composable
fun Preview_StudySecond_Step1() { PreviewSecond(step = GuideOverlayStep.Step1) }

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360)
@Composable
fun Preview_StudySecond_Step2() { PreviewSecond(step = GuideOverlayStep.Step2) }

@Composable
private fun PreviewSecond(step: GuideOverlayStep) {
    val demoSentences = listOf(
        // ✅ 큰따옴표 포함 예시
        "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀",
        "“우리는 생각하기 위해 머리가 있다.”"
    )
    var currentIndex by remember { mutableStateOf(0) }
    var overlay by remember { mutableStateOf(step) }
    var errorIndex by remember { mutableStateOf<Int?>(null) }

    var contentTop by remember { mutableStateOf(Offset.Zero) }
    var anchorPos by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(errorIndex) { if (errorIndex != null) { delay(650); errorIndex = null } }

    val sentence = if (overlay == GuideOverlayStep.None) demoSentences[currentIndex] else GuideSentence

    Box(Modifier.fillMaxSize().background(Color.White)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 48.dp)
                .onGloballyPositioned { contentTop = it.positionInRoot() }
        ) {
            TopBar(title = "오늘의 학습", onBackClick = {})
            Spacer(Modifier.height(24.dp))
            Text(
                "학습 진행률",
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(Modifier.height(16.dp))
            StepProgressBarPreview(totalSteps = 3, currentStep = 2)
            if (overlay == GuideOverlayStep.None) {
                Spacer(Modifier.height(40.dp))
                SentenceCounter(
                    current = currentIndex + 1,
                    total = demoSentences.size
                )
                Spacer(Modifier.height(32.dp))
            } else {
                // 온보딩(1/2단계)에서는 카운터 표시 X (레이아웃 간격만 유지)
                Spacer(Modifier.height(40.dp))
            }
//            Spacer(Modifier.height(20.dp))
//            SentenceCounter(current = currentIndex + 1, total = demoSentences.size, modifier = if (overlay == GuideOverlayStep.Step2) Modifier.alpha(0f) else Modifier)
//            Spacer(Modifier.height(16.dp))

            Box(
                Modifier
                    .height(1.dp)
                    .onGloballyPositioned { anchorPos = it.positionInRoot() }
            )

            if (overlay == GuideOverlayStep.Step1) {
                OriginalWithTypos(sentence, typed = "", errorIndex = null, color = Color.Black)
                Spacer(Modifier.height(12.dp))
                FullWidthInputChip(
                    value = "",
                    onValueChange = {},
                    placeholder = null,
                    textColor = Gray_616161,
                    background = Field_EFF4FB,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }

        HandwritingGuideOverlay(
            step = overlay,
            onTapAnywhere = {
                overlay = when (overlay) {
                    GuideOverlayStep.Step1 -> GuideOverlayStep.Step2
                    GuideOverlayStep.Step2 -> GuideOverlayStep.None
                    GuideOverlayStep.None -> GuideOverlayStep.None
                }
            },
            modifier = Modifier.matchParentSize(),

            // ✅ 말풍선을 오른쪽 +40dp, 위로 -30dp 이동
            bubble1OffsetX = 40.dp,
            bubble1OffsetY = (-30).dp,
            bubble2OffsetX = 40.dp,
            bubble2OffsetY = (-30).dp,

            bubble1Width   = null,
            bubble2Width   = null,
            arrowOffsetX   = (40).dp,
            arrowOffsetY   = (-100).dp,
            arrowSize      = 48.dp
        )

        if (overlay == GuideOverlayStep.Step2) {
            val dy = with(LocalDensity.current) { (anchorPos.y - contentTop.y).toDp() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // ✅ 프리뷰에서도 동일하게 왼쪽 24.dp
                    .padding(start = 24.dp, end = 20.dp, top = 32.dp, bottom = 48.dp)
                    .absoluteOffset(x = 0.dp, y = dy + 148.dp)
                    .zIndex(20f)
            ) {
                // ✅ 프리뷰도 동일한 하이라이트
                OriginalWithHeHighlight(text = sentence)

                Spacer(Modifier.height(12.dp))

                // ✅ 프리뷰도 "위헤" 및 여백 확대
                FullWidthInputChip(
                    value = "“빛을 보기 위헤",
                    onValueChange = {},
                    placeholder = null,
                    textColor = Color.Black,
                    background = Color.White,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
    }
}
