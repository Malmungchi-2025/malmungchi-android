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
import androidx.compose.ui.input.pointer.pointerInput


@Composable
fun StudyReadingScreen(
    viewModel: StudyReadingViewModel,
    totalSteps: Int = 3,
    currentStep: Int = 1,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) { android.util.Log.d("NAV", ">> study_reading 진입") }
    val quote by viewModel.quote.collectAsState()
    val selectedWord by viewModel.selectedWord.collectAsState()
    val highlightWords by viewModel.highlightWords.collectAsState()
    val studyId by viewModel.studyId.collectAsState()

    // ✅ 로딩 판단(에러는 제외)
    val isError = quote.startsWith("❗")
    val isQuoteLoading = !isError && (
            quote.isBlank() ||
                    quote == "생성 중…" ||
                    quote.startsWith("로딩 중")
            )

    var showBottomSheet by remember { mutableStateOf(false) }
    var yellowPenMode by remember { mutableStateOf(false) }

    // UI 하이라이트용 임시 단어 상태
    var tempSelectedWord by remember { mutableStateOf<String?>(null) }

    val penStates = listOf(R.drawable.img_pen_black, R.drawable.img_pen_yellow, R.drawable.img_pen_blue)
    var currentPenIndex by remember { mutableStateOf(0) }

    // 수집 말풍선
    var showCollectBubble by remember { mutableStateOf(false) }
    var bubblePosition by remember { mutableStateOf(Offset.Zero) }

    // 오늘의 학습 글감
    LaunchedEffect(Unit) { viewModel.fetchTodayQuote() }

    // 본문 공통 modifier (헤더에 영향 없음)
    val contentModifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())

    // Box 좌표
    var boxCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // 공통 텍스트 스타일
    val commonTextStyle = TextStyle(
        fontSize = 14.sp,
        lineHeight = 22.sp,
        color = Color(0xFF333333),
        textAlign = TextAlign.Start
    )
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            // ✅ 헤더(뒤로가기/타이틀, 학습 진행률, 진행바) 좌우 여백 20dp만 적용됨
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 16.dp)
            .onGloballyPositioned { coords -> boxCoords = coords }
    ) {
        // "수집" 말풍선
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
            // ⛔️ 기존 Column.padding(16.dp) 제거 → 헤더는 좌우 20dp만 갖게 됨
        ) {
            // 상단 바
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp) // 48dp 터치 타겟 유지
                ) {
                    // ⬇️ 내용 영역을 꽉 채워서 '왼쪽 가운데' 정렬
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.btn_img_back),
                            contentDescription = "뒤로가기",
                            modifier = Modifier.size(24.dp) // 아이콘 자체 크기
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

            StepProgressBar(totalSteps, currentStep) // ✅ 내부 가로 패딩 제거됨

            Spacer(Modifier.height(24.dp))

            // 본문
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9F9F9),
                modifier = Modifier.weight(1f)
            ) {
                when (currentPenIndex) {
                    1 -> {
                        ClickableHighlightedText(
                            text = quote,
                            selectedWord = tempSelectedWord,
                            onWordClick = { word, offset ->
                                tempSelectedWord = word
                                bubblePosition = offset
                                showCollectBubble = true
                            },
                            modifier = contentModifier,
                            textStyle = commonTextStyle,
                            containerCoords = boxCoords
                        )
                    }
                    2 -> {
                        RegexHighlightedText(
                            text = quote,
                            highlights = highlightWords,
                            modifier = contentModifier,
                            textStyle = commonTextStyle
                        )
                    }
                    else -> {
                        Text(
                            text = quote,
                            style = commonTextStyle,
                            modifier = contentModifier
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 하단 버튼들
            Row(
                Modifier.fillMaxWidth().padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = penStates[currentPenIndex]),
                    contentDescription = "펜",
                    modifier = Modifier
                        .size(64.dp)
                        .align(Alignment.CenterVertically)
                        .clickable {
                            currentPenIndex = (currentPenIndex + 1) % penStates.size
                            when (currentPenIndex) {
                                1 -> yellowPenMode = true
                                2 -> {
                                    yellowPenMode = false
                                    studyId?.let { viewModel.loadVocabularyList(it) }
                                }
                                else -> yellowPenMode = false
                            }
                        }
                )

                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .height(42.dp)
                        .width(160.dp)
                ) {
                    Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
                }
            }
        }

        // ===== 🔽🔽🔽 로딩 오버레이(마지막에 그려서 위에 덮어씌움) 🔽🔽🔽
        if (isQuoteLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(2f) // 모든 요소 위
                    .background(Color.White.copy(alpha = 0.95f))
                    .clickable( // 터치 막기용
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nickname_loading),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp)
                )
            }
        }
        // ===== 🔼🔼🔼 로딩 오버레이 끝 🔼🔼🔼
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

/** 노란펜: 모든 단어 클릭 가능 텍스트 */
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
                startIndex = endIndex + 1 // 공백 포함
            }
        },
        onClick = { offset ->
            annotated.getStringAnnotations("WORD", offset, offset).firstOrNull()
                ?.let { annotation ->
                    val rect = wordPositions[annotation.item]
                    if (rect != null && textLayoutCoords != null) {
                        val anchorInText = Offset(rect.left + rect.width / 2, rect.top)
                        val anchorInBox =
                            if (containerCoords != null)
                                containerCoords.localPositionOf(textLayoutCoords!!, anchorInText)
                            else anchorInText
                        val bubbleTopLeft = Offset(
                            x = anchorInBox.x,
                            y = anchorInBox.y - with(density) { 48.dp.toPx() + 0.dp.toPx() }
                        )
                        onWordClick(annotation.item, bubbleTopLeft)
                    } else {
                        onWordClick(annotation.item, Offset.Zero)
                    }
                }
        }
    )
}

/** 파란펜: 서버 단어 목록 Regex 하이라이트 */
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
    Text(annotated, style = textStyle, modifier = modifier)
}

/** ProgressBar (가로 패딩 제거) */
@Composable
fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
    Row(
        Modifier.fillMaxWidth(), // ⬅️ 좌우 20dp만 적용되도록 추가 패딩 없음
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewStudyReadingScreen() {
    // 미리보기용 더미 VM이 없다면, 실제 프로젝트에서는 미리보기 전용 VM을 주입하세요.
    // 여기선 시그니처만 맞춰둡니다.
    // StudyReadingScreen(viewModel = hiltViewModel())
}

/** 로딩 전용 미리보기 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 800)
@Composable
fun PreviewStudyReadingScreen_Loading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_nickname_loading),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
    }
}



//package com.malmungchi.feature.study.first
//
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.ClickableText
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.malmungchi.core.model.WordItem
//import com.malmungchi.feature.study.Pretendard
//import com.malmungchi.feature.study.R
//import androidx.compose.material3.IconButton
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.layout.LayoutCoordinates
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.zIndex
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.withStyle
//import com.malmungchi.feature.study.StudyReadingViewModel
//
//@Composable
//fun StudyReadingScreen(
//    viewModel: StudyReadingViewModel,
//    totalSteps: Int = 3,
//    currentStep: Int = 1,
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    LaunchedEffect(Unit) { android.util.Log.d("NAV", ">> study_reading 진입") }
//    val quote by viewModel.quote.collectAsState()
//    val selectedWord by viewModel.selectedWord.collectAsState()
//    val highlightWords by viewModel.highlightWords.collectAsState()
//    val studyId by viewModel.studyId.collectAsState()
//
//    val isQuoteLoading = quote.isBlank()
//
//    var showBottomSheet by remember { mutableStateOf(false) }
//    var yellowPenMode by remember { mutableStateOf(false) }
//
//    // UI 하이라이트용 임시 단어 상태
//    var tempSelectedWord by remember { mutableStateOf<String?>(null) }
//
//    val penStates = listOf(R.drawable.img_pen_black, R.drawable.img_pen_yellow, R.drawable.img_pen_blue)
//    var currentPenIndex by remember { mutableStateOf(0) }
//
//    // 수집 말풍선
//    var showCollectBubble by remember { mutableStateOf(false) }
//    var bubblePosition by remember { mutableStateOf(Offset.Zero) }
//
//    // 오늘의 학습 글감
//    LaunchedEffect(Unit) { viewModel.fetchTodayQuote() }
//
//    // 본문 공통 modifier (헤더에 영향 없음)
//    val contentModifier = Modifier
//        .padding(16.dp)
//        .verticalScroll(rememberScrollState())
//
//    // Box 좌표
//    var boxCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
//
//    // 공통 텍스트 스타일
//    val commonTextStyle = TextStyle(
//        fontSize = 14.sp,
//        lineHeight = 22.sp,
//        color = Color(0xFF333333),
//        textAlign = TextAlign.Start
//    )
//    val density = LocalDensity.current
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            // ✅ 헤더(뒤로가기/타이틀, 학습 진행률, 진행바) 좌우 여백 20dp만 적용됨
//            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 16.dp)
//            .onGloballyPositioned { coords -> boxCoords = coords }
//    ) {
//        // "수집" 말풍선
//        if (showCollectBubble && boxCoords != null) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_collect_bubble),
//                contentDescription = "수집",
//                modifier = Modifier
//                    .zIndex(1f)
//                    .offset {
//                        IntOffset(
//                            (bubblePosition.x - with(density) { 24.dp.toPx() }).toInt(),
//                            bubblePosition.y.toInt()
//                        )
//                    }
//                    .size(48.dp)
//                    .clickable {
//                        viewModel.searchWord(tempSelectedWord ?: "")
//                        showBottomSheet = true
//                        showCollectBubble = false
//                    }
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//            // ⛔️ 기존 Column.padding(16.dp) 제거 → 헤더는 좌우 20dp만 갖게 됨
//        ) {
//            // 상단 바
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(
//                    onClick = onBackClick,
//                    modifier = Modifier.size(48.dp) // 48dp 터치 타겟 유지
//                ) {
//                    // ⬇️ 내용 영역을 꽉 채워서 '왼쪽 가운데' 정렬
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.CenterStart
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.btn_img_back),
//                            contentDescription = "뒤로가기",
//                            modifier = Modifier.size(24.dp) // 아이콘 자체 크기
//                        )
//                    }
//                }
//
//                Text(
//                    text = "오늘의 학습",
//                    fontSize = 20.sp,
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier.weight(1f),
//                    textAlign = TextAlign.Center
//                )
//                Spacer(Modifier.width(48.dp))
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            Text(
//                "학습 진행률",
//                fontSize = 16.sp,
//                color = Color.Black,
//                fontWeight = FontWeight.Normal,
//                modifier = Modifier.padding(start = 4.dp)
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            StepProgressBar(totalSteps, currentStep) // ✅ 내부 가로 패딩 제거됨
//
//            Spacer(Modifier.height(24.dp))
//
//            // 본문
//            Surface(
//                shape = RoundedCornerShape(12.dp),
//                color = Color(0xFFF9F9F9),
//                modifier = Modifier.weight(1f)
//            ) {
//                when (currentPenIndex) {
//                    1 -> {
//                        ClickableHighlightedText(
//                            text = quote,
//                            selectedWord = tempSelectedWord,
//                            onWordClick = { word, offset ->
//                                tempSelectedWord = word
//                                bubblePosition = offset
//                                showCollectBubble = true
//                            },
//                            modifier = contentModifier,
//                            textStyle = commonTextStyle,
//                            containerCoords = boxCoords
//                        )
//                    }
//                    2 -> {
//                        RegexHighlightedText(
//                            text = quote,
//                            highlights = highlightWords,
//                            modifier = contentModifier,
//                            textStyle = commonTextStyle
//                        )
//                    }
//                    else -> {
//                        Text(
//                            text = quote,
//                            style = commonTextStyle,
//                            modifier = contentModifier
//                        )
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // 하단 버튼들
//            Row(
//                Modifier.fillMaxWidth().padding(bottom = 48.dp),
//
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Image(
//                    painter = painterResource(id = penStates[currentPenIndex]),
//                    contentDescription = "펜",
//                    modifier = Modifier
//                        .size(64.dp)
//                        .align(Alignment.CenterVertically)
//                        .clickable {
//                            currentPenIndex = (currentPenIndex + 1) % penStates.size
//                            when (currentPenIndex) {
//                                1 -> yellowPenMode = true
//                                2 -> {
//                                    yellowPenMode = false
//                                    studyId?.let { viewModel.loadVocabularyList(it) }
//                                }
//                                else -> yellowPenMode = false
//                            }
//                        }
//                )
//
//                Button(
//                    onClick = onNextClick,
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                    shape = RoundedCornerShape(50),
//                    modifier = Modifier
//                        .align(Alignment.CenterVertically)
//                        .height(42.dp)
//                        .width(160.dp)
//                ) {
//                    Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
//                }
//            }
//        }
//    }
//
//    // 단어 BottomSheet
//    if (showBottomSheet && selectedWord != null) {
//        WordCollectBottomSheet(
//            word = selectedWord!!.word,
//            definition = selectedWord!!.meaning,
//            example = selectedWord!!.example ?: "",
//            onDismiss = {
//                showBottomSheet = false
//                tempSelectedWord = null
//            },
//            onSaveClick = {
//                viewModel.saveWord(selectedWord!!) {
//                    showBottomSheet = false
//                    tempSelectedWord = null
//                }
//            }
//        )
//    }
//}
//
///** 노란펜: 모든 단어 클릭 가능 텍스트 */
//@Composable
//fun ClickableHighlightedText(
//    text: String,
//    selectedWord: String? = null,
//    onWordClick: (String, Offset) -> Unit,
//    modifier: Modifier = Modifier,
//    textStyle: TextStyle = TextStyle.Default,
//    containerCoords: LayoutCoordinates? = null
//) {
//    val density = LocalDensity.current
//    val words = text.split(" ")
//    val annotated = buildAnnotatedString {
//        var currentIndex = 0
//        words.forEachIndexed { index, rawWord ->
//            val cleanWord = rawWord.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z]"), "")
//            pushStringAnnotation(tag = "WORD", annotation = cleanWord)
//            withStyle(
//                style = SpanStyle(
//                    color = Color(0xFF333333),
//                    background = if (cleanWord == selectedWord) Color(0xFFFFD91C) else Color.Transparent
//                )
//            ) { append(rawWord) }
//            pop()
//            currentIndex += rawWord.length
//            if (index != words.lastIndex) {
//                append(" ")
//                currentIndex++
//            }
//        }
//    }
//
//    val wordPositions = remember { mutableStateMapOf<String, Rect>() }
//    var textLayoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
//
//    ClickableText(
//        text = annotated,
//        modifier = modifier
//            .fillMaxWidth()
//            .onGloballyPositioned { coords -> textLayoutCoords = coords },
//        style = textStyle,
//        onTextLayout = { layoutResult ->
//            wordPositions.clear()
//            var startIndex = 0
//            words.forEach { rawWord ->
//                val cleanWord = rawWord.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z]"), "")
//                val endIndex = startIndex + rawWord.length
//                val boxes = (startIndex until endIndex).map { layoutResult.getBoundingBox(it) }
//                if (boxes.isNotEmpty()) {
//                    val left = boxes.minOf { it.left }
//                    val top = boxes.minOf { it.top }
//                    val right = boxes.maxOf { it.right }
//                    val bottom = boxes.maxOf { it.bottom }
//                    wordPositions[cleanWord] = Rect(left, top, right, bottom)
//                }
//                startIndex = endIndex + 1 // 공백 포함
//            }
//        },
//        onClick = { offset ->
//            annotated.getStringAnnotations("WORD", offset, offset).firstOrNull()
//                ?.let { annotation ->
//                    val rect = wordPositions[annotation.item]
//                    if (rect != null && textLayoutCoords != null) {
//                        val anchorInText = Offset(rect.left + rect.width / 2, rect.top)
//                        val anchorInBox =
//                            if (containerCoords != null)
//                                containerCoords.localPositionOf(textLayoutCoords!!, anchorInText)
//                            else anchorInText
//                        val bubbleTopLeft = Offset(
//                            x = anchorInBox.x,
//                            y = anchorInBox.y - with(density) { 48.dp.toPx() + 0.dp.toPx() }
//                        )
//                        onWordClick(annotation.item, bubbleTopLeft)
//                    } else {
//                        onWordClick(annotation.item, Offset.Zero)
//                    }
//                }
//        }
//    )
//}
//
///** 파란펜: 서버 단어 목록 Regex 하이라이트 */
//@Composable
//fun RegexHighlightedText(
//    text: String,
//    highlights: List<String>,
//    modifier: Modifier = Modifier,
//    textStyle: TextStyle = TextStyle.Default
//) {
//    val annotated: AnnotatedString = buildAnnotatedString {
//        append(text)
//        highlights.forEach { word ->
//            val regex = Regex(Regex.escape(word))
//            regex.findAll(text).forEach { match ->
//                addStyle(
//                    style = SpanStyle(background = Color(0xFFCCFF00)),
//                    start = match.range.first,
//                    end = match.range.last + 1
//                )
//            }
//        }
//    }
//    Text(annotated, style = textStyle, modifier = modifier)
//}
//
///** ProgressBar (가로 패딩 제거) */
//@Composable
//fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
//    Row(
//        Modifier.fillMaxWidth(), // ⬅️ 좌우 20dp만 적용되도록 추가 패딩 없음
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        repeat(totalSteps) { index ->
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .height(16.dp)
//                    .background(
//                        color = if (index == currentStep - 1) Color(0xFF195FCF) else Color(0xFFF2F2F2),
//                        shape = RoundedCornerShape(50)
//                    )
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//fun PreviewStudyReadingScreen() {
//    // 미리보기용 더미 VM이 없다면, 실제 프로젝트에서는 미리보기 전용 VM을 주입하세요.
//    // 여기선 시그니처만 맞춰둡니다.
//    // StudyReadingScreen(viewModel = hiltViewModel())
//}
//
///** 로딩 전용 미리보기 */
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 800)
//@Composable
//fun PreviewStudyReadingScreen_Loading() {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 16.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Image(
//            painter = painterResource(id = R.drawable.img_nickname_loading),
//            contentDescription = null,
//            modifier = Modifier.size(120.dp)
//        )
//    }
//}


//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.ClickableText
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.AnnotatedString
//import androidx.compose.ui.text.SpanStyle
//import androidx.compose.ui.text.buildAnnotatedString
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.malmungchi.core.model.WordItem
//import com.malmungchi.feature.study.Pretendard
//
//import com.malmungchi.feature.study.R
//import androidx.compose.material3.IconButton
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.layout.LayoutCoordinates
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.unit.IntOffset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.zIndex
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.withStyle
//import com.malmungchi.feature.study.StudyReadingViewModel
//
//@Composable
//fun StudyReadingScreen(
//    viewModel: StudyReadingViewModel,
//    totalSteps: Int = 3,
//    currentStep: Int = 1,
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    LaunchedEffect(Unit) { android.util.Log.d("NAV", ">> study_reading 진입") }
//    val quote by viewModel.quote.collectAsState()
//    val selectedWord by viewModel.selectedWord.collectAsState()
//    val highlightWords by viewModel.highlightWords.collectAsState()
//    val studyId by viewModel.studyId.collectAsState()
//
//    var showBottomSheet by remember { mutableStateOf(false) }
//    var yellowPenMode by remember { mutableStateOf(false) }
//
//    // UI 하이라이트용 임시 단어 상태
//    var tempSelectedWord by remember { mutableStateOf<String?>(null) }
//
//    val penStates = listOf(R.drawable.img_pen_black, R.drawable.img_pen_yellow, R.drawable.img_pen_blue)
//    var currentPenIndex by remember { mutableStateOf(0) }
//
//    // 수집 말풍선
//    var showCollectBubble by remember { mutableStateOf(false) }
//    var bubblePosition by remember { mutableStateOf(Offset.Zero) }
//
//    // 오늘의 학습 글감
//    LaunchedEffect(Unit) { viewModel.fetchTodayQuote() }
//
//    // 본문 공통 modifier (헤더에 영향 없음)
//    val contentModifier = Modifier
//        .padding(16.dp)
//        .verticalScroll(rememberScrollState())
//
//    // Box 좌표
//    var boxCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
//
//    // 공통 텍스트 스타일
//    val commonTextStyle = TextStyle(
//        fontSize = 14.sp,
//        lineHeight = 22.sp,
//        color = Color(0xFF333333),
//        textAlign = TextAlign.Start
//    )
//    val density = LocalDensity.current
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            // ✅ 헤더(뒤로가기/타이틀, 학습 진행률, 진행바) 좌우 여백 20dp만 적용됨
//            .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)
//            .onGloballyPositioned { coords -> boxCoords = coords }
//    ) {
//        // "수집" 말풍선
//        if (showCollectBubble && boxCoords != null) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_collect_bubble),
//                contentDescription = "수집",
//                modifier = Modifier
//                    .zIndex(1f)
//                    .offset {
//                        IntOffset(
//                            (bubblePosition.x - with(density) { 24.dp.toPx() }).toInt(),
//                            bubblePosition.y.toInt()
//                        )
//                    }
//                    .size(48.dp)
//                    .clickable {
//                        viewModel.searchWord(tempSelectedWord ?: "")
//                        showBottomSheet = true
//                        showCollectBubble = false
//                    }
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//            // ⛔️ 기존 Column.padding(16.dp) 제거 → 헤더는 좌우 20dp만 갖게 됨
//        ) {
//            // 상단 바
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(24.dp)
//                        .clickable(onClick = onBackClick),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.btn_img_back),
//                        contentDescription = "뒤로가기",
//                        modifier = Modifier.fillMaxSize()
//                    )
//                }
//
//                Spacer(Modifier.width(8.dp))
//                Text(
//                    text = "오늘의 학습",
//                    fontSize = 20.sp,
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier.weight(1f),
//                    textAlign = TextAlign.Center
//                )
//                Spacer(Modifier.width(48.dp))
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            Text(
//                "학습 진행률",
//                fontSize = 16.sp,
//                color = Color.Black,
//                fontWeight = FontWeight.Normal,
//                modifier = Modifier.padding(start = 4.dp)
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            StepProgressBar(totalSteps, currentStep) // ✅ 내부 가로 패딩 제거됨
//
//            Spacer(Modifier.height(24.dp))
//
//            // 본문
//            Surface(
//                shape = RoundedCornerShape(12.dp),
//                color = Color(0xFFF9F9F9),
//                modifier = Modifier.weight(1f)
//            ) {
//                when (currentPenIndex) {
//                    1 -> {
//                        ClickableHighlightedText(
//                            text = quote,
//                            selectedWord = tempSelectedWord,
//                            onWordClick = { word, offset ->
//                                tempSelectedWord = word
//                                bubblePosition = offset
//                                showCollectBubble = true
//                            },
//                            modifier = contentModifier,
//                            textStyle = commonTextStyle,
//                            containerCoords = boxCoords
//                        )
//                    }
//                    2 -> {
//                        RegexHighlightedText(
//                            text = quote,
//                            highlights = highlightWords,
//                            modifier = contentModifier,
//                            textStyle = commonTextStyle
//                        )
//                    }
//                    else -> {
//                        Text(
//                            text = quote,
//                            style = commonTextStyle,
//                            modifier = contentModifier
//                        )
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // 하단 버튼들
//            Row(
//                Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Image(
//                    painter = painterResource(id = penStates[currentPenIndex]),
//                    contentDescription = "펜",
//                    modifier = Modifier
//                        .size(64.dp)
//                        .padding(bottom = 48.dp)
//                        .align(Alignment.CenterVertically)
//                        .clickable {
//                            currentPenIndex = (currentPenIndex + 1) % penStates.size
//                            when (currentPenIndex) {
//                                1 -> yellowPenMode = true
//                                2 -> {
//                                    yellowPenMode = false
//                                    studyId?.let { viewModel.loadVocabularyList(it) }
//                                }
//                                else -> yellowPenMode = false
//                            }
//                        }
//                )
//
//                // 하단 Row 내부의 기존 Button 교체
//                Button(
//                    onClick = onNextClick,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color(0xFF195FCF) // ✅ 동일 색상
//                    ),
//                    shape = RoundedCornerShape(50),       // ✅ 동일 라운드
//                    modifier = Modifier
//                        .align(Alignment.CenterVertically)
//                        .padding(bottom = 48.dp)          // ✅ 동일 위치 여백
//                        .height(42.dp)                    // ✅ 동일 높이
//                        .width(160.dp)                    // ✅ 동일 너비
//                ) {
//                    Text(
//                        "다음 단계",
//                        fontSize = 16.sp,                 // ✅ 동일 폰트 크기
//                        fontFamily = Pretendard,          // ✅ 동일 폰트
//                        color = Color.White               // ✅ 동일 텍스트 색
//                    )
//                }
//            }
//        }
//    }
//
//    // 단어 BottomSheet
//    if (showBottomSheet && selectedWord != null) {
//        WordCollectBottomSheet(
//            word = selectedWord!!.word,
//            definition = selectedWord!!.meaning,
//            example = selectedWord!!.example ?: "",
//            onDismiss = {
//                showBottomSheet = false
//                tempSelectedWord = null
//            },
//            onSaveClick = {
//                viewModel.saveWord(selectedWord!!) {
//                    showBottomSheet = false
//                    tempSelectedWord = null
//                }
//            }
//        )
//    }
//}
//
///** 노란펜: 모든 단어 클릭 가능 텍스트 */
//@Composable
//fun ClickableHighlightedText(
//    text: String,
//    selectedWord: String? = null,
//    onWordClick: (String, Offset) -> Unit,
//    modifier: Modifier = Modifier,
//    textStyle: TextStyle = TextStyle.Default,
//    containerCoords: LayoutCoordinates? = null
//) {
//    val density = LocalDensity.current
//    val words = text.split(" ")
//    val annotated = buildAnnotatedString {
//        var currentIndex = 0
//        words.forEachIndexed { index, rawWord ->
//            val cleanWord = rawWord.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z]"), "")
//            pushStringAnnotation(tag = "WORD", annotation = cleanWord)
//            withStyle(
//                style = SpanStyle(
//                    color = Color(0xFF333333),
//                    background = if (cleanWord == selectedWord) Color(0xFFFFD91C) else Color.Transparent
//                )
//            ) { append(rawWord) }
//            pop()
//            currentIndex += rawWord.length
//            if (index != words.lastIndex) {
//                append(" ")
//                currentIndex++
//            }
//        }
//    }
//
//    val wordPositions = remember { mutableStateMapOf<String, Rect>() }
//    var textLayoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
//
//    ClickableText(
//        text = annotated,
//        modifier = modifier
//            .fillMaxWidth()
//            .onGloballyPositioned { coords -> textLayoutCoords = coords },
//        style = textStyle,
//        onTextLayout = { layoutResult ->
//            wordPositions.clear()
//            var startIndex = 0
//            words.forEach { rawWord ->
//                val cleanWord = rawWord.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z]"), "")
//                val endIndex = startIndex + rawWord.length
//                val boxes = (startIndex until endIndex).map { layoutResult.getBoundingBox(it) }
//                if (boxes.isNotEmpty()) {
//                    val left = boxes.minOf { it.left }
//                    val top = boxes.minOf { it.top }
//                    val right = boxes.maxOf { it.right }
//                    val bottom = boxes.maxOf { it.bottom }
//                    wordPositions[cleanWord] = Rect(left, top, right, bottom)
//                }
//                startIndex = endIndex + 1 // 공백 포함
//            }
//        },
//        onClick = { offset ->
//            annotated.getStringAnnotations("WORD", offset, offset).firstOrNull()
//                ?.let { annotation ->
//                    val rect = wordPositions[annotation.item]
//                    if (rect != null && textLayoutCoords != null) {
//                        val anchorInText = Offset(rect.left + rect.width / 2, rect.top)
//                        val anchorInBox =
//                            if (containerCoords != null)
//                                containerCoords.localPositionOf(textLayoutCoords!!, anchorInText)
//                            else anchorInText
//                        val bubbleTopLeft = Offset(
//                            x = anchorInBox.x,
//                            y = anchorInBox.y - with(density) { 48.dp.toPx() + 8.dp.toPx() }
//                        )
//                        onWordClick(annotation.item, bubbleTopLeft)
//                    } else {
//                        onWordClick(annotation.item, Offset.Zero)
//                    }
//                }
//        }
//    )
//}
//
///** 파란펜: 서버 단어 목록 Regex 하이라이트 */
//@Composable
//fun RegexHighlightedText(
//    text: String,
//    highlights: List<String>,
//    modifier: Modifier = Modifier,
//    textStyle: TextStyle = TextStyle.Default
//) {
//    val annotated: AnnotatedString = buildAnnotatedString {
//        append(text)
//        highlights.forEach { word ->
//            val regex = Regex(Regex.escape(word))
//            regex.findAll(text).forEach { match ->
//                addStyle(
//                    style = SpanStyle(background = Color(0xFFCCFF00)),
//                    start = match.range.first,
//                    end = match.range.last + 1
//                )
//            }
//        }
//    }
//    Text(annotated, style = textStyle, modifier = modifier)
//}
//
///** ProgressBar (가로 패딩 제거) */
//@Composable
//fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
//    Row(
//        Modifier.fillMaxWidth(), // ⬅️ 좌우 20dp만 적용되도록 추가 패딩 없음
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        repeat(totalSteps) { index ->
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//                    .height(16.dp)
//                    .background(
//                        color = if (index == currentStep - 1) Color(0xFF195FCF) else Color(0xFFF2F2F2),
//                        shape = RoundedCornerShape(50)
//                    )
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 800)
//@Composable
//fun PreviewStudyReadingScreen() {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 16.dp)
//    ) {
//        Column(modifier = Modifier.fillMaxSize()) {
//            // 상단 바
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(onClick = {}) {
//                    Image(
//                        painter = painterResource(id = R.drawable.btn_img_back),
//                        contentDescription = "뒤로가기",
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//                Text(
//                    text = "오늘의 학습",
//                    fontSize = 20.sp,
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier.weight(1f),
//                    textAlign = TextAlign.Center
//                )
//                Spacer(Modifier.width(48.dp))
//            }
//
//            Spacer(Modifier.height(24.dp))
//            Text("학습 진행률", fontSize = 16.sp, color = Color.Black)
//            Spacer(Modifier.height(16.dp))
//            StepProgressBar(totalSteps = 3, currentStep = 1)
//            Spacer(Modifier.height(24.dp))
//
//            // 본문
//            Surface(
//                shape = RoundedCornerShape(12.dp),
//                color = Color(0xFFF9F9F9),
//                modifier = Modifier.weight(1f)
//            ) {
//                Text(
//                    text = "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀가 있으며…”",
//                    fontSize = 14.sp,
//                    color = Color(0xFF333333),
//                    modifier = Modifier.padding(16.dp)
//                )
//            }
//        }
//    }
//}