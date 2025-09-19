package com.malmungchi.feature.study.second

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.zIndex
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.StudyReadingViewModel
import java.text.Normalizer // ✅ NFC 정규화용

/* ---------- 색상/상수 ---------- */
private val Blue_195FCF = Color(0xFF195FCF)
private val Film_50 = Color.Black.copy(alpha = 0.5f)
private val Field_EFF4FB = Color(0xFFEFF4FB)
private val Yellow_FFD91C = Color(0xFFFFD91C)
private val Gray_989898 = Color(0xFF989898)
private val Gray_616161 = Color(0xFF616161)
private val Red_FF0D0D = Color(0xFFFF0D0D)

/* ✅ 동일 들여쓰기 (문장/입력칩 x좌표 일치) */
private val SentenceIndent = 8.dp
private val ChipInnerStartPadding = 12.dp

/* ✅ 온보딩 예시 문구 */
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
                Icon(painter = backPainter, contentDescription = "뒤로가기", tint = Color.Unspecified)
            } else {
                Box(Modifier.fillMaxSize().background(Color(0x33000000), RoundedCornerShape(4.dp)))
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

/* ---------- Step1 문장 하이라이트(위/해) ---------- */
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
                append(AnnotatedString("위", SpanStyle(color = baseColor)))
                append(AnnotatedString("해", SpanStyle(color = if (wiheCount == 1) heColor else baseColor)))
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
private fun SentenceCounter(current: Int, total: Int, modifier: Modifier = Modifier) {
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

/* ---------- ✅ 한글 조합 안전 비교 유틸 ---------- */
private fun nfc(s: String): String = Normalizer.normalize(s, Normalizer.Form.NFC)
private fun isSafePrefix(new: String, original: String): Boolean {
    val t = nfc(new)
    val o = nfc(original)
    if (o.startsWith(t)) return true
    val last = t.lastOrNull() ?: return true
    val isJamo = (last in '\u3131'..'\u318E') || (last in '\u1100'..'\u11FF') ||
            (last in '\uA960'..'\uA97F') || (last in '\uD7B0'..'\uD7FF')
    return if (isJamo) o.startsWith(t.dropLast(1)) else false
}

/* ---------- 오버레이(필름 + 말풍선) ---------- */
@Composable
private fun HandwritingGuideOverlay(
    step: GuideOverlayStep,
    onTapAnywhere: () -> Unit,
    modifier: Modifier = Modifier,
    bubble1OffsetX: Dp = 0.dp,
    bubble1OffsetY: Dp = 0.dp,
    bubble2OffsetX: Dp = 0.dp,
    bubble2OffsetY: Dp = 0.dp,
    bubble1Width: Dp? = null,
    bubble2Width: Dp? = null,
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
            .then(if (isPreview) Modifier else Modifier.pointerInput(step) { detectTapGestures { onTapAnywhere() } })
            .zIndex(10f)
    ) {
        val showArrow = step == GuideOverlayStep.Step1
        val bubbleRes = when (step) {
            GuideOverlayStep.Step1 -> R.drawable.img_words1_hint1
            GuideOverlayStep.Step2 -> R.drawable.img_words2_hint2
            GuideOverlayStep.None -> null
        }

        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 96.dp)
        ) {
            if (bubbleRes != null) {
                val base = Modifier
                    .wrapContentSize()
                    .let { if (step == GuideOverlayStep.Step1) it.offset(bubble1OffsetX, bubble1OffsetY) else it.offset(bubble2OffsetX, bubble2OffsetY) }
                    .let {
                        when {
                            step == GuideOverlayStep.Step1 && bubble1Width != null -> it.width(bubble1Width)
                            step == GuideOverlayStep.Step2 && bubble2Width != null -> it.width(bubble2Width)
                            else -> it
                        }
                    }

                val bubblePainter = painterResourceSafe(bubbleRes)
                if (bubblePainter != null) {
                    Image(painter = bubblePainter, contentDescription = null, modifier = base)
                } else {
                    Box(base.height(80.dp).width(240.dp).background(Color(0x33FFFFFF), RoundedCornerShape(12.dp)))
                }
            }

            if (showArrow) {
                val arrowPainter = painterResourceSafe(R.drawable.img_arrow)
                val arrowMod = Modifier.align(Alignment.TopEnd).offset(40.dp, (-100).dp).size(48.dp)
                if (arrowPainter != null) {
                    Icon(painter = arrowPainter, contentDescription = null, tint = Color.Unspecified, modifier = arrowMod)
                } else {
                    Box(arrowMod.background(Color(0x55FFFFFF), RoundedCornerShape(8.dp)))
                }
            }
        }

        val malchiPainter = painterResourceSafe(R.drawable.img_malchi)
        val malchiMod = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 40.dp).size(84.dp)
        if (malchiPainter != null) {
            Icon(painter = malchiPainter, contentDescription = null, tint = Color.Unspecified, modifier = malchiMod)
        } else {
            Box(malchiMod.background(Color(0x55FFFFFF), RoundedCornerShape(12.dp)))
        }
    }
}

/* ---------- 풀폭 입력칩 ---------- */
@Composable
private fun FullWidthInputChip(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String? = null,
    textColor: Color,
    background: Color,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    maxHeight: Dp = 140.dp,
    onImeDone: (() -> Unit)? = null
) {
    val scroll = rememberScrollState()

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
        singleLine = false,
        maxLines = Int.MAX_VALUE,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onImeDone?.invoke() }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 42.dp, max = maxHeight)
                    .shadow(elevation, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(background)
                    .padding(paddingValues = contentPadding)
                    .verticalScroll(scroll)
            ) {
                if (value.text.isEmpty() && !placeholder.isNullOrBlank()) {
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
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBackClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue_195FCF),
            modifier = Modifier
                .height(42.dp)
                .weight(1f)
        ) { Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard) }

        Button(
            onClick = onNextClick,
            colors = ButtonDefaults.buttonColors(containerColor = Blue_195FCF),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .height(42.dp)
                .weight(1f)
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
    val currentIndexFromVm by viewModel.currentIndex.collectAsState()
    val typedFromVm by viewModel.userInput.collectAsState()


    var overlayStep by rememberSaveable { mutableStateOf(GuideOverlayStep.Step1) }

    // ✅ ① 스킵 다이얼로그 상태 선언 (기존엔 없어서 Unresolved)
    var showSkipDialog by rememberSaveable { mutableStateOf(false) }

    // ✅ 화면 내부에서 사용할 인덱스(UI 인덱스) — VM 값과 초기 동기화
    var uiIndex by rememberSaveable { mutableStateOf(0) }
    LaunchedEffect(currentIndexFromVm) { uiIndex = currentIndexFromVm }

    // ✅ Step2 고정 위치 기준(루트 y)
    var anchorYInRoot by remember { mutableStateOf(0f) }

    val listState = rememberLazyListState()
    LaunchedEffect(uiIndex) { listState.animateScrollToItem(uiIndex, -20) }

    LaunchedEffect(Unit) {
        viewModel.initHandwritingStudy()
        viewModel.fetchHandwriting()
    }

    val sentenceFromUi = sentences.getOrNull(uiIndex).orEmpty()
    val displaySentence =
        if (overlayStep == GuideOverlayStep.None) sentenceFromUi else GuideSentence

    var typedValue by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(typedFromVm, sentenceFromUi) {
        if (typedFromVm != typedValue.text) {
            typedValue =
                typedValue.copy(text = typedFromVm, selection = TextRange(typedFromVm.length))
        }
    }

    var errorIndexUi by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(overlayStep) { errorIndexUi = null }

    val original = sentenceFromUi

    // ✅ ② 완료/스킵 가능 여부 계산 (기존엔 없어서 Unresolved)
    val isLast = sentences.isNotEmpty() && uiIndex == sentences.lastIndex
    val isCurrentDone = nfc(typedValue.text) == nfc(original)
    val isFinished = overlayStep == GuideOverlayStep.None && isLast && isCurrentDone
    val canSkip = overlayStep == GuideOverlayStep.None && !isFinished

    // ✅ ③ BackHandler는 canSkip/showSkipDialog 계산 뒤에!
    BackHandler(enabled = canSkip) {
        showSkipDialog = true
    }

    // ✅ 문장 완료 시 동작: 마지막이면 다음 단계, 아니면 다음 문장으로
    val advanceOrFinish: () -> Unit = {
        if (uiIndex < sentences.lastIndex) {
            uiIndex += 1
            // VM/로컬 입력 초기화
            viewModel.onUserInputChange("")
            typedValue = TextFieldValue("")
        } else {
            onNextClick()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        /* ----------------- ① 기본 컨텐츠 레이어 (z=0) ----------------- */
        Column(
            Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 48.dp)
                .zIndex(0f)
        ) {
            TopBar(title = "오늘의 학습", onBackClick = onBackClick)

            Spacer(Modifier.height(24.dp))
            Text("학습 진행률", fontSize = 16.sp, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            StepProgressBarPreview(totalSteps = 3, currentStep = 2)

            if (overlayStep == GuideOverlayStep.None) {
                Spacer(Modifier.height(40.dp))
                SentenceCounter(
                    current = (uiIndex + 1).coerceAtMost(sentences.size.coerceAtLeast(1)),
                    total = sentences.size.coerceAtLeast(1)
                )
                Spacer(Modifier.height(32.dp))
            } else {
                Spacer(Modifier.height(40.dp))
            }

            // ✅ Step2 기준 앵커(y)
            Box(
                Modifier
                    .height(1.dp)
                    .onGloballyPositioned { coords -> anchorYInRoot = coords.positionInRoot().y }
            )

            when (overlayStep) {
                GuideOverlayStep.Step1 -> {
                    Column(Modifier.padding(start = SentenceIndent)) {
                        Box(Modifier.padding(start = ChipInnerStartPadding)) {
                            OriginalWithTypos(
                                displaySentence,
                                typed = "",
                                errorIndex = null,
                                color = Color.Black
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        FullWidthInputChip(
                            value = TextFieldValue(""),
                            onValueChange = {},
                            placeholder = null,
                            textColor = Gray_616161,
                            background = Field_EFF4FB,
                            contentPadding = PaddingValues(
                                start = ChipInnerStartPadding,
                                end = 12.dp,
                                top = 10.dp,
                                bottom = 10.dp
                            )
                            //contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }

                GuideOverlayStep.Step2 -> { /* 필름 위 전용 레이어에서 그림 */
                }

                GuideOverlayStep.None -> {
                    Column(Modifier.padding(start = SentenceIndent)) {
                        Box(Modifier.padding(start = ChipInnerStartPadding)) {
                            OriginalWithTypos(
                                original = original,
                                typed = typedValue.text,
                                errorIndex = errorIndexUi,
                                color = Color.Black
                            )
                        }
                        Spacer(Modifier.height(12.dp))

                        FullWidthInputChip(
                            value = typedValue,
                            onValueChange = { newV ->
                                if (newV.composition != null) {
                                    errorIndexUi = null
                                    typedValue = newV
                                    return@FullWidthInputChip
                                }

                                val newText = newV.text
                                val oldText = typedValue.text

                                if (newText.length <= oldText.length) {
                                    errorIndexUi = null
                                    typedValue = newV
                                    viewModel.onUserInputChange(newText)
                                    return@FullWidthInputChip
                                }

                                if (isSafePrefix(newText, original)) {
                                    errorIndexUi = null
                                    typedValue = newV
                                    viewModel.onUserInputChange(nfc(newText))

                                    // ✅ 정답 완성 시: 다음 문장 or 마지막이면 다음 단계
                                    if (nfc(newText) == nfc(original)) {
                                        advanceOrFinish()
                                    }
                                    return@FullWidthInputChip
                                }

                                // 오탈자 표시만
                                errorIndexUi = nfc(oldText).length
                            },
                            placeholder = null,
                            textColor = Gray_616161,
                            background = Field_EFF4FB,
                            elevation = 3.dp,
                            maxHeight = 160.dp,
                            onImeDone = {
                                if (nfc(typedValue.text) == nfc(original)) {
                                    advanceOrFinish()
                                }
                            },
                            // ⬇️ 칩의 내부 좌측 패딩을 명시적으로 12dp로
                            contentPadding = PaddingValues(
                                start = ChipInnerStartPadding,
                                end = 12.dp,
                                top = 10.dp,
                                bottom = 10.dp
                            ),

                            )
                    }

                    Spacer(Modifier.height(48.dp))

                    val hasNext = uiIndex < sentences.size - 1
                    if (hasNext) {
                        Text(
                            text = "다음 문장",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Gray_616161,
                            modifier = Modifier.padding(start = SentenceIndent)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = sentences[uiIndex + 1],
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = Gray_989898,
                            lineHeight = 26.sp,
                            modifier = Modifier.padding(start = SentenceIndent)
                        )
                    }

                    Spacer(Modifier.height(24.dp))
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
            bubble1OffsetX = 40.dp + 30.dp,
            bubble1OffsetY = (-30).dp,
            bubble2OffsetX = 40.dp + 30.dp,
            bubble2OffsetY = (-30).dp,
            bubble1Width = null,
            bubble2Width = null,
            arrowOffsetX = 40.dp,
            arrowOffsetY = (-100).dp,
            arrowSize = 48.dp
        )

        /* ----------------- ③ Step2 전용: ‘필름 위’ 고정 위치 레이어 (z=20) ----------------- */
        if (overlayStep == GuideOverlayStep.Step2) {
            val density = LocalDensity.current
            val anchorYDp = with(density) { anchorYInRoot.toDp() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 20.dp, top = 32.dp, bottom = 48.dp)
                    .absoluteOffset(x = 0.dp, y = anchorYDp - 20.dp)
                    .zIndex(20f)
            ) {
                Column(Modifier.padding(start = SentenceIndent)) {
                    OriginalWithHeHighlight(text = displaySentence)
                    Spacer(Modifier.height(12.dp))
                    FullWidthInputChip(
                        value = TextFieldValue("“빛을 보기 위헤"),
                        onValueChange = {},
                        placeholder = null,
                        textColor = Color.Black,
                        background = Color.White,
                        contentPadding = PaddingValues(
                            start = ChipInnerStartPadding,
                            end = 12.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        ),
                        //contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        elevation = 2.dp
                    )
                }
            }
        }

        // ✅ 하단 버튼 연결부만 교체
        if (overlayStep == GuideOverlayStep.None) {
            BottomNavigationButtons(
                onBackClick = {
                    if (canSkip) showSkipDialog = true else onBackClick()
                },
                onNextClick = {
                    if (canSkip) showSkipDialog = true else onNextClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp)
            )
        }

        // ✅ 스킵 다이얼로그 실제 표시
        if (showSkipDialog) {
            SkipHandwritingAlert.Show(
                onConfirm = {
                    showSkipDialog = false
                    onNextClick() // "네" → 다음 페이지로 이동
                },
                onDismiss = {
                    showSkipDialog = false
                    // "아니요" → 그대로 현재 화면 유지 (필사 계속)
                    // 필요하면 여기서 키보드/포커스 복구도 가능
                }
            )
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
        "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀",
        "“우리는 생각하기 위해 머리가 있다.”"
    )
    var uiIndex by rememberSaveable { mutableStateOf(0) }
    var overlay by remember { mutableStateOf(step) }

    var anchorYInRoot by remember { mutableStateOf(0f) }

    val sentence = if (overlay == GuideOverlayStep.None) demoSentences[uiIndex] else GuideSentence
    var typedValue by remember { mutableStateOf(TextFieldValue("")) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .zIndex(5f)
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 48.dp)
        ) {
            TopBar(title = "오늘의 학습", onBackClick = {})
            Spacer(Modifier.height(24.dp))
            Text("학습 진행률", fontSize = 16.sp, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            StepProgressBarPreview(totalSteps = 3, currentStep = 2)
            if (overlay == GuideOverlayStep.None) {
                Spacer(Modifier.height(40.dp))
                SentenceCounter(current = uiIndex + 1, total = demoSentences.size)
                Spacer(Modifier.height(32.dp))
            } else {
                Spacer(Modifier.height(40.dp))
            }

            Box(
                Modifier
                    .height(1.dp)
                    .onGloballyPositioned { coords -> anchorYInRoot = coords.positionInRoot().y }
            )

            if (overlay == GuideOverlayStep.Step1) {
                Column(Modifier.padding(start = SentenceIndent)) {
                    Box(Modifier.padding(start = ChipInnerStartPadding)) {
                        OriginalWithTypos(sentence, typed = "", errorIndex = null, color = Color.Black)
                    }
                    Spacer(Modifier.height(12.dp))
                    FullWidthInputChip(
                        value = TextFieldValue(""),
                        onValueChange = {},
                        textColor = Color(0xFF616131),
                        background = Field_EFF4FB,
                        contentPadding = PaddingValues(
                            start = ChipInnerStartPadding,
                            end = 12.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        )
                    )
                }
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
            bubble1OffsetX = 40.dp + 30.dp,
            bubble1OffsetY = (-30).dp,
            bubble2OffsetX = 40.dp +  30.dp,
            bubble2OffsetY = (-30).dp,
            bubble1Width   = null,
            bubble2Width   = null,
            arrowOffsetX   = 40.dp,
            arrowOffsetY   = (-100).dp,
            arrowSize      = 48.dp
        )

        if (overlay == GuideOverlayStep.Step2) {
            val density = LocalDensity.current
            val anchorYDp = with(density) { anchorYInRoot.toDp() }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 20.dp, top = 32.dp, bottom = 48.dp)
                    .absoluteOffset(x = 0.dp, y = anchorYDp + 48.dp)
                    .zIndex(20f)
            ) {
                Column(Modifier.padding(start = SentenceIndent)) {
                    Box(Modifier.padding(start = ChipInnerStartPadding)) {
                        OriginalWithHeHighlight(text = sentence)
                    }
                    Spacer(Modifier.height(12.dp))
                    FullWidthInputChip(
                        value = TextFieldValue("“빛을 보기 위헤"),
                        onValueChange = {},
                        textColor = Color.Black,
                        background = Color.White,
                        contentPadding = PaddingValues(
                            start = ChipInnerStartPadding,
                            end = 12.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        )
                    )
                }
            }
        }
    }
}

/* ---------- 실제 필사 화면 전용 프리뷰 (온보딩 아님) ---------- */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360)
@Composable
fun Preview_Handwriting_AfterOnboarding() {
    val sentences = listOf(
        "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀",
        "“우리는 생각하기 위해 머리가 있다.”"
    )
    var uiIndex by rememberSaveable { mutableStateOf(0) }
    val original = sentences[uiIndex]
    var typedValue by remember { mutableStateOf(TextFieldValue("“빛을 보기 위")) }
    var errorIndexUi by remember { mutableStateOf<Int?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 48.dp)
    ) {
        TopBar(title = "오늘의 학습", onBackClick = {})
        Spacer(Modifier.height(24.dp))
        Text("학습 진행률", fontSize = 16.sp, color = Color.Black)
        Spacer(Modifier.height(16.dp))
        StepProgressBarPreview(totalSteps = 3, currentStep = 2)

        Spacer(Modifier.height(40.dp))
        SentenceCounter(current = uiIndex + 1, total = sentences.size)
        Spacer(Modifier.height(32.dp))

        Column(Modifier.padding(start = SentenceIndent)) {
            OriginalWithTypos(original = original, typed = typedValue.text, errorIndex = errorIndexUi, color = Color.Black)
            Spacer(Modifier.height(12.dp))
            FullWidthInputChip(
                value = typedValue,
                onValueChange = { newV ->
                    if (newV.composition != null) {
                        errorIndexUi = null
                        typedValue = newV.copy()
                        return@FullWidthInputChip
                    }
                    val newText = newV.text
                    val oldText = typedValue.text
                    if (newText.length <= oldText.length) {
                        errorIndexUi = null
                        typedValue = newV
                        return@FullWidthInputChip
                    }
                    if (isSafePrefix(newText, original)) {
                        errorIndexUi = null
                        typedValue = newV
                        return@FullWidthInputChip
                    }
                    errorIndexUi = nfc(oldText).length
                },
                textColor = Gray_616161,
                background = Field_EFF4FB
            )
        }

        Spacer(Modifier.height(48.dp))

        val hasNext = uiIndex < sentences.size - 1
        if (hasNext) {
            Text(
                text = "다음 문장",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Gray_616161,
                modifier = Modifier.padding(start = SentenceIndent)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = sentences[uiIndex + 1],
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Gray_989898,
                lineHeight = 26.sp,
                modifier = Modifier.padding(start = SentenceIndent)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
