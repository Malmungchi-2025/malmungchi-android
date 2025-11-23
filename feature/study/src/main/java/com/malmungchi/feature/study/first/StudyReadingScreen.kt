package com.malmungchi.feature.study.first

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.model.WordItem
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import androidx.compose.material3.IconButton
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.malmungchi.feature.study.StudyReadingViewModel
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun StudyReadingScreen(
    viewModel: StudyReadingViewModel,
    totalSteps: Int = 3,
    currentStep: Int = 1,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    var showGuide by remember { mutableStateOf(true) }

    // padding 없는 최상위 Box
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // 기존 UI 전체를 content로 분리
        StudyReadingContent(
            viewModel = viewModel,
            totalSteps = totalSteps,
            currentStep = currentStep,
            onBackClick = onBackClick,
            onNextClick = onNextClick,

        )

        // 가이드 오버레이는 항상 최상위 박스에서 표시해야 한다
        if (showGuide) {
            StudyReadingGuideScreen(
                onDismiss = { showGuide = false }
            )
        }
    }
}

@Composable
fun StudyReadingContent(
    viewModel: StudyReadingViewModel,
    totalSteps: Int = 3,
    currentStep: Int = 1,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) { android.util.Log.d("NAV", ">> study_reading 진입") }

    // ① 기능 가이드 보여줄지 여부
    //var showGuide by remember { mutableStateOf(true) }

    val quote by viewModel.quote.collectAsState()
    val selectedWord by viewModel.selectedWord.collectAsState()
    val highlightWords by viewModel.highlightWords.collectAsState()
    val studyId by viewModel.studyId.collectAsState()

    // 로딩 상태
    val isError = quote.startsWith("❗")


    var showBottomSheet by remember { mutableStateOf(false) }
    var yellowPenMode by remember { mutableStateOf(false) }
    var tempSelectedWord by remember { mutableStateOf<String?>(null) }

    val penStates = listOf(
        R.drawable.img_pen_black_new,
        R.drawable.img_pen_yellow_new,
        R.drawable.img_pen_blue_new_new
    )
    var currentPenIndex by remember { mutableStateOf(0) }

    // 수집 말풍선
    var showCollectBubble by remember { mutableStateOf(false) }
    var bubblePosition by remember { mutableStateOf(Offset.Zero) }

    // 오늘의 학습 글감 불러오기
    LaunchedEffect(Unit) { viewModel.fetchTodayQuote() }

    // 텍스트 스타일
    val commonTextStyle = TextStyle(
        fontSize = 16.sp,
        fontFamily = Pretendard,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF333333),
        lineHeight = 25.6.sp,
        letterSpacing = 0.15.sp,
        textAlign = TextAlign.Start
    )

    val contentModifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())

    val density = LocalDensity.current
    var boxCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 16.dp)
            .onGloballyPositioned { coords -> boxCoords = coords }
    ) {
        // 수집 말풍선
        if (showCollectBubble && boxCoords != null) {
            Image(
                painter = painterResource(id = R.drawable.ic_collect_bubble),
                contentDescription = "수집",
                modifier = Modifier
                    .zIndex(1f)
                    .offset {
                        IntOffset(
                            (bubblePosition.x - with(density) { 24.dp.toPx() }).toInt(),
                            bubblePosition.y.toInt()
                        )
                    }
                    .size(48.dp)
                    .clickable {
                        viewModel.searchWord(tempSelectedWord ?: "")
                        showBottomSheet = true
                        showCollectBubble = false
                    }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 상단바
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.btn_img_back),
                            contentDescription = "뒤로가기",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = "오늘의 학습",
                    fontSize = 20.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "학습 진행률",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 4.dp)
            )

            Spacer(Modifier.height(16.dp))
            StepProgressBar(totalSteps, currentStep)
            Spacer(Modifier.height(24.dp))

            // 본문 영역
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9F9F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 460.dp)   // 최소 460dp, 그 이상 자동 늘어남
                    //.height(460.dp)
            ) {
                val cleanedQuote = quote
                    .replace("\r\n", " ")
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .replace(Regex("[ \t]+"), " ")
                    .trim()

                when (currentPenIndex) {
                    1 -> ClickableHighlightedText(
                        text = cleanedQuote,
                        selectedWord = tempSelectedWord,
                        onWordClick = { word, offset ->
                            tempSelectedWord = word
                            bubblePosition = offset
                            showCollectBubble = true
                        },
                        //modifier = contentModifier,
                        modifier = Modifier.padding(16.dp),
                        textStyle = commonTextStyle,
                        containerCoords = boxCoords
                    )

                    2 -> RegexHighlightedText(
                        text = cleanedQuote,
                        highlights = highlightWords,
                        modifier = Modifier.padding(16.dp),
                        //modifier = contentModifier,
                        textStyle = commonTextStyle
                    )

                    else -> Text(
                        text = cleanedQuote,
                        style = commonTextStyle,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(16.dp)
                        //modifier = contentModifier
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }

        // ✅ 하단 버튼 (바텀시트 위 정확히 64dp)
        // ✅ 하단 버튼 (바텀시트 위 정확히 64dp)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-64).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
//                // ✏️ 피그마 Drop Shadow 스타일의 펜 버튼
//                Box(
//                    modifier = Modifier
//                        .size(50.dp)
//                        .clickable {
//                            currentPenIndex = (currentPenIndex + 1) % penStates.size
//                            yellowPenMode = (currentPenIndex == 1)
//                        },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(50.dp)
//                            .graphicsLayer {
//                                shadowElevation = 16.dp.toPx()          // ✅ blur 강도
//                                shape = RoundedCornerShape(50)
//                                clip = false
//                                ambientShadowColor = Color.Black.copy(alpha = 0.3f) // ✅ 진한 그림자
//                                spotShadowColor = Color.Black.copy(alpha = 0.3f)
//                            }
//                            .background(Color.White, RoundedCornerShape(50)),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        // ✅ 펜 이미지 — 파란펜만 살짝 더 크게!
//                        val penModifier = when (currentPenIndex) {
//                            2 -> Modifier.size(64.dp)  // 💙 파란펜만 +2dp 확대
//                            else -> Modifier.size(52.dp)
//                        }
//                        Image(
//                            painter = painterResource(id = penStates[currentPenIndex]),
//                            contentDescription = "펜",
//                            modifier = penModifier
//                            //modifier = Modifier.size(52.dp)
//                        )
//                    }
//                }
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            currentPenIndex = (currentPenIndex + 1) % penStates.size
                            yellowPenMode = (currentPenIndex == 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // ✅ 배경 흰색 원만 (필요하면 유지)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White, RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = penStates[currentPenIndex]),
                            contentDescription = "펜",
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                // ✅ 다음 단계 버튼 (수정 금지)
                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(42.dp)
                        .width(160.dp)
                ) {
                    Text(
                        "다음 단계",
                        fontSize = 16.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }


        // 로딩 오버레이
        // -> 제거. 지저분함.
        //가이드 오버레이 추가함.
//        if (showGuide) {
//            StudyReadingGuideScreen(
//                onDismiss = { showGuide = false }
//            )
//        }
    }

    // 단어 BottomSheet
    if (showBottomSheet && selectedWord != null) {
        WordCollectBottomSheet(
            word = selectedWord!!.word,
            definition = selectedWord!!.meaning,
            example = selectedWord!!.example ?: "",
            onDismiss = {
                showBottomSheet = false
                tempSelectedWord = null
            },
            onSaveClick = {
                viewModel.saveWord(selectedWord!!) {
                    showBottomSheet = false
                    tempSelectedWord = null
                }
            }
        )
    }
}

/** 노란펜 클릭 가능한 텍스트 **/
@Composable
fun ClickableHighlightedText(
    text: String,
    selectedWord: String? = null,
    onWordClick: (String, Offset) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default,
    containerCoords: LayoutCoordinates? = null
) {
    val density = LocalDensity.current
    val words = text.split(" ")
    val annotated = buildAnnotatedString {
        var currentIndex = 0
        words.forEachIndexed { index, rawWord ->
            val cleanWord = rawWord.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z]"), "")
            pushStringAnnotation(tag = "WORD", annotation = cleanWord)
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF333333),
                    background = if (cleanWord == selectedWord) Color(0xFFFFD91C) else Color.Transparent
                )
            ) { append(rawWord) }
            pop()
            currentIndex += rawWord.length
            if (index != words.lastIndex) {
                append(" ")
                currentIndex++
            }
        }
    }

    val wordPositions = remember { mutableStateMapOf<String, Rect>() }
    var textLayoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    ClickableText(
        text = annotated,
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords -> textLayoutCoords = coords },
        style = textStyle,
        onTextLayout = { layoutResult ->
            wordPositions.clear()
            var startIndex = 0
            words.forEach { rawWord ->
                val cleanWord = rawWord.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z]"), "")
                val endIndex = startIndex + rawWord.length
                val boxes = (startIndex until endIndex).map { layoutResult.getBoundingBox(it) }
                if (boxes.isNotEmpty()) {
                    val left = boxes.minOf { it.left }
                    val top = boxes.minOf { it.top }
                    val right = boxes.maxOf { it.right }
                    val bottom = boxes.maxOf { it.bottom }
                    wordPositions[cleanWord] = Rect(left, top, right, bottom)
                }
                startIndex = endIndex + 1
            }
        },
        onClick = { offset ->
            annotated.getStringAnnotations("WORD", offset, offset).firstOrNull()?.let { annotation ->
                val rect = wordPositions[annotation.item]
                if (rect != null && textLayoutCoords != null) {
                    val anchorInText = Offset(rect.left + rect.width / 2, rect.top)
                    val anchorInBox =
                        if (containerCoords != null)
                            containerCoords.localPositionOf(textLayoutCoords!!, anchorInText)
                        else anchorInText
                    val bubbleTopLeft = Offset(
                        x = anchorInBox.x,
                        y = anchorInBox.y - with(density) { 48.dp.toPx() }
                    )
                    onWordClick(annotation.item, bubbleTopLeft)
                } else {
                    onWordClick(annotation.item, Offset.Zero)
                }
            }
        }
    )
}

/** 파란펜 하이라이트 **/
@Composable
fun RegexHighlightedText(
    text: String,
    highlights: List<String>,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default
) {
    val annotated: AnnotatedString = buildAnnotatedString {
        append(text)
        highlights.forEach { word ->
            val regex = Regex(Regex.escape(word))
            regex.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(background = Color(0xFFCCFF00)),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
    }
    Text(text = annotated, style = textStyle, modifier = modifier)
}

/** 진행바 **/
@Composable
fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .background(
                        color = if (index == currentStep - 1) Color(0xFF195FCF) else Color(0xFFF2F2F2),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 844)
@Composable
fun PreviewStudyReadingScreen() {
    // ✨ 가짜 데이터 (본문 텍스트만 표시)
    val dummyQuote = """
        금일은 날씨가 맑은 날이었다. 아침에는 일찍 일어나 출근을 했다.
        지하철에서 나는 오늘의 일정을 되새기며 참조했다.
        회사에 도착하여 업무를 시작했는데, 친구가 생일이라는 사실을 떠올렸다.
        퇴근 후 마트에 들러 친구의 생일선물을 구매했다.
        집에 돌아와서 저녁을 먹으며 오늘 하루를 돌아보았다.
        오늘의 작은 행복을 느끼며 하루를 마무리했다.
    """.trimIndent()

    // 💡 ViewModel 없이 프리뷰용만 보여줌
    StudyReadingScreenPreviewOnly(
        quote = dummyQuote,
        highlightWords = listOf("날씨", "행복"),
        selectedWord = null,
        onNextClick = {},
        onBackClick = {}
    )
}

@Composable
fun StudyReadingScreenPreviewOnly(
    quote: String,
    highlightWords: List<String>,
    selectedWord: WordItem?,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var currentPenIndex by remember { mutableStateOf(0) }
    var yellowPenMode by remember { mutableStateOf(false) }

    val penStates = listOf(
        R.drawable.img_pen_black,
        R.drawable.img_pen_yellow,
        R.drawable.img_pen_blue_new
        //R.drawable.img_pen_blue
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 상단 바
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(48.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_img_back),
                        contentDescription = "뒤로가기",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "오늘의 학습",
                    fontSize = 20.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(24.dp))
            Text("학습 진행률", fontSize = 16.sp, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            StepProgressBar(totalSteps = 3, currentStep = 1)
            Spacer(Modifier.height(24.dp))

            // 본문
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9F9F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 460.dp)
            ) {
                Text(
                    text = quote,
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    lineHeight = 25.6.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.height(100.dp))
        }

        // ✅ 하단 버튼 (바텀시트 위 64dp)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .offset(y = (-64).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 펜 버튼 (blur 그림자 원 적용)
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            currentPenIndex = (currentPenIndex + 1) % penStates.size
                            yellowPenMode = (currentPenIndex == 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // ✅ 그림자 원 (blur 효과)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(50)
                            )
                            .blur(10.dp)
                    )

                    // ✅ 실제 흰색 원 + 펜 이미지
                    // ✅ 펜 버튼 (피그마처럼 원 테두리 주변에 그림자)
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                currentPenIndex = (currentPenIndex + 1) % penStates.size
                                yellowPenMode = (currentPenIndex == 1)
                            },
                        contentAlignment = Alignment.Center
                    ) {


                        // ✏️ 피그마 Drop Shadow 스타일의 펜 버튼
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clickable {
                                    currentPenIndex = (currentPenIndex + 1) % penStates.size
                                    yellowPenMode = (currentPenIndex == 1)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // ✅ Drop Shadow만 별도의 Box로 분리 — blur 아님
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .graphicsLayer {
                                        shadowElevation = 16.dp.toPx()          // 피그마의 blur 강도에 해당
                                        shape = RoundedCornerShape(50)
                                        clip = false
                                        ambientShadowColor = Color.Black.copy(alpha = 0.3f)
                                        spotShadowColor = Color.Black.copy(alpha = 0.3f)
                                    }
                                    .background(Color.White, RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = penStates[currentPenIndex]),
                                    contentDescription = "펜",
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }
                    }
                }

                // 다음 단계 버튼
                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(42.dp)
                        .width(160.dp)
                ) {
                    Text(
                        "다음 단계",
                        fontSize = 16.sp,
                        fontFamily = Pretendard,
                        color = Color.White
                    )
                }
            }
        }
    }
}

