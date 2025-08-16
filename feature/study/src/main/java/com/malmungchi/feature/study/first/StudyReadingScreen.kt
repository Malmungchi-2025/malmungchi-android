package com.malmungchi.feature.study.first


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.InlineTextContent
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import com.malmungchi.feature.study.StudyReadingViewModel

@Composable
fun StudyReadingScreen(
    //token: String = "dummy_token",          // ← 시그니처는 유지 (호출부 영향 없게)
    viewModel: StudyReadingViewModel,// = hiltViewModel(),
    totalSteps: Int = 3,
    currentStep: Int = 1,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    // study_reading
    LaunchedEffect(Unit) { android.util.Log.d("NAV", ">> study_reading 진입") }
    val quote by viewModel.quote.collectAsState()
    val selectedWord by viewModel.selectedWord.collectAsState()
    val highlightWords by viewModel.highlightWords.collectAsState()
    val studyId by viewModel.studyId.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var yellowPenMode by remember { mutableStateOf(false) }

    // ✅ UI 하이라이트용 임시 단어 상태 (펜 상태 변경에도 유지)
    var tempSelectedWord by remember { mutableStateOf<String?>(null) }

    val penStates = listOf(R.drawable.img_pen_black, R.drawable.img_pen_yellow, R.drawable.img_pen_blue)
    var currentPenIndex by remember { mutableStateOf(0) }

    // 수집 말풍선
    var showCollectBubble by remember { mutableStateOf(false) }
    var bubblePosition by remember { mutableStateOf(Offset.Zero) }

    // ✅ 오늘의 학습 글감 API 호출 (토큰 제거)
    LaunchedEffect(Unit) { viewModel.fetchTodayQuote() }

    // ✅ 공통 Modifier (노란펜/파란펜 모드 동일 적용)
    val contentModifier = Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState())

    // ✅ Box 전체 좌표 저장
    var boxCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    // ✅ 공통 텍스트 스타일
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
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
                top = 32.dp      // ✅ 위는 32, 나머지는 16
            )
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
                        // 🔄 토큰 미사용
                        viewModel.searchWord(tempSelectedWord ?: "")
                        showBottomSheet = true
                        showCollectBubble = false
                    }
            )
        }

        Column(Modifier.fillMaxSize().padding(16.dp)) {
            // 상단 바
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
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

            // 본문
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9F9F9), modifier = Modifier.weight(1f)) {
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
                            textStyle = commonTextStyle
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
                Modifier.fillMaxWidth(),
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
                                    // 🔄 토큰 미사용
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
                // 🔄 토큰 미사용
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
    textStyle: TextStyle = TextStyle.Default
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
            annotated.getStringAnnotations("WORD", offset, offset).firstOrNull()?.let { annotation ->
                val rect = wordPositions[annotation.item]
                if (rect != null && textLayoutCoords != null) {
                    val globalCenter = textLayoutCoords!!.localToRoot(
                        Offset(rect.left + rect.width / 2, rect.top)
                    )
                    val yOffset = with(density) { 8.dp.toPx() }
                    val finalPos = Offset(globalCenter.x, globalCenter.y - rect.height - yOffset)
                    onWordClick(annotation.item, finalPos)
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

/** ProgressBar */
@Composable
fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
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


//@Composable
//fun StudyReadingScreen(
//    token: String = "dummy_token",
//    viewModel: StudyReadingViewModel = hiltViewModel(),
//    totalSteps: Int = 3,
//    currentStep: Int = 1,
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    val quote by viewModel.quote.collectAsState()
//    val selectedWord by viewModel.selectedWord.collectAsState()
//    val highlightWords by viewModel.highlightWords.collectAsState()
//    val studyId by viewModel.studyId.collectAsState()
//
//    var showBottomSheet by remember { mutableStateOf(false) }
//    var yellowPenMode by remember { mutableStateOf(false) }
//
//    // ✅ UI 하이라이트용 임시 단어 상태 (펜 상태 변경에도 유지)
//    var tempSelectedWord by remember { mutableStateOf<String?>(null) }
//
//    val penStates = listOf(R.drawable.img_pen_black, R.drawable.img_pen_yellow, R.drawable.img_pen_blue)
//    var currentPenIndex by remember { mutableStateOf(0) }
//
//    //수집 말풍선
//    var showCollectBubble by remember { mutableStateOf(false) }
//    var bubblePosition by remember { mutableStateOf(Offset.Zero) }
//
//    // ✅ 오늘의 학습 글감 API 호출
//    LaunchedEffect(Unit) { viewModel.fetchTodayQuote(token) }
//
//    // ✅ 공통 Modifier (노란펜/파란펜 모드 동일 적용)
//    val contentModifier = Modifier
//        .padding(16.dp)
//        .verticalScroll(rememberScrollState())
//
//    // ✅ Box 전체 좌표 저장
//    var boxCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
//
//    // 🔥 [FIX] ✅ 공통 텍스트 스타일 추가 (펜 모드 관계없이 동일 적용)
//    val commonTextStyle = androidx.compose.ui.text.TextStyle(
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
//            .onGloballyPositioned { coords -> boxCoords = coords } // ✅ Box 좌표 기록
//    ) {
//        // 🔥 [FIX] ✅ "수집" 말풍선 크기 및 위치 보정
//        if (showCollectBubble && boxCoords != null) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_collect_bubble),
//                contentDescription = "수집",
//                modifier = Modifier
//                    .zIndex(1f)
//                    .offset {
//                        // 🔥 [FIX] ✅ 위치 보정: 단어 위 중앙 + 최소 y 오프셋
//                        IntOffset(
//                            (bubblePosition.x - with(density) { 24.dp.toPx() }).toInt(),
//                            bubblePosition.y.toInt()
//                        )
//                    }
//                    .size(48.dp) // 🔥 [FIX] ✅ 기존 90dp → 48dp 축소
//                    .clickable {
//                        viewModel.searchWord(token, tempSelectedWord ?: "")
//                        showBottomSheet = true
//                        showCollectBubble = false
//                    }
//            )
//        }
//
//        Column(Modifier.fillMaxSize().padding(16.dp)) {
//
//            // ✅ 상단 바 (뒤로가기 + 타이틀)
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(onClick = onBackClick) {
//                    Image(
//                        painter = painterResource(id = R.drawable.btn_img_back), // ✅ 기존 XML 리소스 사용
//                        contentDescription = "뒤로가기",
//                        modifier = Modifier.size(24.dp) // 필요 시 크기 조절
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
//                Spacer(Modifier.width(48.dp)) // 오른쪽 균형 맞춤
//            }
//
//            Spacer(Modifier.height(24.dp))
//            Text(
//                "학습 진행률",
//                fontSize = 16.sp,
//                color = Color.Black,              // ✅ 글씨 색상 변경
//                fontWeight = FontWeight.Normal,
//                modifier = Modifier.padding(start = 4.dp) // ✅ ProgressBar와 X축 정렬 맞춤
//            )
//            Spacer(Modifier.height(16.dp))
//            StepProgressBar(totalSteps, currentStep)
//            Spacer(Modifier.height(24.dp))
//
//            // ✅ 본문
//            // ✅ 본문 영역
//            // ✅ 본문
//            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9F9F9), modifier = Modifier.weight(1f)) {
//                when (currentPenIndex) {
//                    1 -> {
//                        // 🔥 [FIX] ✅ 노란펜에도 공통 스타일 적용
//                        ClickableHighlightedText(
//                            text = quote,
//                            selectedWord = tempSelectedWord,
//                            onWordClick = { word, offset ->
//                                tempSelectedWord = word
//                                bubblePosition = offset
//                                showCollectBubble = true
//                            },
//                            modifier = contentModifier,
//                            textStyle = commonTextStyle // 🔥 추가
//                        )
//                    }
//                    2 -> {
//                        // 🔥 [FIX] ✅ 파란펜도 동일한 스타일 적용
//                        RegexHighlightedText(
//                            text = quote,
//                            highlights = highlightWords,
//                            modifier = contentModifier,
//                            textStyle = commonTextStyle // 🔥 추가
//                        )
//                    }
//                    else -> {
//                        // 🔥 [FIX] ✅ 검정펜도 동일 스타일 적용
//                        Text(
//                            text = quote,
//                            style = commonTextStyle, // 🔥 변경
//                            modifier = contentModifier
//                        )
//                    }
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // ✅ 하단 버튼 (펜 동작)
//            // ✅ 하단 버튼 (펜 동작)
//            Row(
//                Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically   // ✅ 두 컴포넌트 Y축 정렬 고정
//            ) {
//                Image(
//                    painter = painterResource(id = penStates[currentPenIndex]),
//                    contentDescription = "펜",
//                    modifier = Modifier
//                        .size(64.dp)
//                        .align(Alignment.CenterVertically)       // ✅ 버튼과 평행 정렬
//                        .clickable {
//                            currentPenIndex = (currentPenIndex + 1) % penStates.size
//                            when (currentPenIndex) {
//                                1 -> yellowPenMode = true
//                                2 -> {
//                                    yellowPenMode = false
//                                    studyId?.let { viewModel.loadVocabularyList(token, it) }
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
//                        .align(Alignment.CenterVertically)       // ✅ 펜과 같은 Y축
//                        .height(42.dp)
//                        .width(160.dp)
//                ) {
//                    Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
//                }
//            }
//
//        }
//
//    }
//
//    // ✅ 단어 BottomSheet
//    if (showBottomSheet && selectedWord != null) {
//        WordCollectBottomSheet(
//            word = selectedWord!!.word,
//            definition = selectedWord!!.meaning,
//            example = selectedWord!!.example ?: "",
//            onDismiss = {
//                showBottomSheet = false
//                tempSelectedWord = null // ✅ 취소 시 하이라이트 제거
//            },
//            onSaveClick = {
//                viewModel.saveWord(token, selectedWord!!) {
//                    showBottomSheet = false
//                    tempSelectedWord = null // ✅ 저장 후도 하이라이트 제거
//                }
//            }
//        )
//    }
//}
//
///** ✅ 노란펜: 모든 단어 클릭 가능 텍스트 */
///** ✅ 노란펜: 모든 단어 클릭 가능 텍스트 */
///** ✅ 노란펜: 모든 단어 클릭 가능 텍스트 */
//@Composable
//fun ClickableHighlightedText(
//    text: String,
//    selectedWord: String? = null,
//    onWordClick: (String, Offset) -> Unit,
//    modifier: Modifier = Modifier,
//    textStyle: TextStyle = TextStyle.Default // 🔥 [FIX] textStyle 파라미터 추가
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
//    // ✅ 단어 → Rect 매핑 (단어별 위치 저장)
//    val wordPositions = remember { mutableStateMapOf<String, Rect>() }
//    var textLayoutCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
//
//    ClickableText(
//        text = annotated,
//        modifier = modifier
//            .fillMaxWidth()
//            .onGloballyPositioned { coords -> textLayoutCoords = coords },
//        style = textStyle, // 🔥 [FIX] 공통 스타일 적용
//        onTextLayout = { layoutResult ->
//            wordPositions.clear()
//            var startIndex = 0
//            words.forEach { rawWord ->
//                val cleanWord = rawWord.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z]"), "")
//                val endIndex = startIndex + rawWord.length
//
//                // ✅ 단어 전체 BoundingBox 계산
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
//            annotated.getStringAnnotations("WORD", offset, offset).firstOrNull()?.let { annotation ->
//                val rect = wordPositions[annotation.item]
//                if (rect != null && textLayoutCoords != null) {
//                    // ✅ 단어 중심 좌표 계산
//                    val globalCenter = textLayoutCoords!!.localToRoot(
//                        Offset(rect.left + rect.width / 2, rect.top)
//                    )
//                    // 🔥 [FIX] Density 변환으로 y좌표 보정
//                    val yOffset = with(density) { 8.dp.toPx() }
//                    val finalPos = Offset(globalCenter.x, globalCenter.y - rect.height - yOffset)
//                    onWordClick(annotation.item, finalPos)
//                } else {
//                    onWordClick(annotation.item, Offset.Zero)
//                }
//            }
//        }
//    )
//}
//
///** ✅ 파란펜: 서버 단어 목록 Regex 하이라이트 */
//@Composable
//fun RegexHighlightedText(
//    text: String,
//    highlights: List<String>,
//    modifier: Modifier = Modifier,
//    textStyle: TextStyle = TextStyle.Default // 🔥 [FIX] textStyle 추가
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
//    Text(annotated, style = textStyle, modifier = modifier) // 🔥 [FIX] 공통 스타일 적용
//}
//
//
///** ✅ 하이라이트된 텍스트 출력 */
//@Composable
//fun HighlightedText(text: String, highlights: List<String>) {
//    val annotated: AnnotatedString = buildAnnotatedString {
//        append(text)
//        highlights.forEach { word ->
//            val index = text.indexOf(word)
//            if (index >= 0) {
//                addStyle(
//                    style = SpanStyle(background = Color(0xFFB2C9FF)),
//                    start = index,
//                    end = index + word.length
//                )
//            }
//        }
//    }
//    Text(annotated, fontSize = 14.sp, color = Color(0xFF333333), modifier = Modifier.padding(16.dp))
//}
//
///** ✅ ProgressBar */
//@Composable
//fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
//    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//        repeat(totalSteps) { index ->
//            Box(
//                modifier = Modifier.weight(1f).height(16.dp).background(
//                    color = if (index == currentStep - 1) Color(0xFF195FCF) else Color(0xFFF2F2F2),
//                    shape = RoundedCornerShape(50)
//                )
//            )
//        }
//    }
//}
//
///** ✅ Preview */
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//fun PreviewStudyReadingScreen() {
//    StudyReadingScreen()
//}


//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
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
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.malmungchi.feature.study.Pretendard
//import com.malmungchi.feature.study.R
//import com.malmungchi.feature.study.StudyReadingViewModel
//import com.malmungchi.core.model.WordItem
//
//@Composable
//fun StudyReadingScreen(
//    token: String = "dummy_token",   // ✅ Preview에서 사용할 기본 토큰
//    viewModel: StudyReadingViewModel = hiltViewModel(),
//    totalSteps: Int = 3,
//    currentStep: Int = 1,
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    // ✅ ViewModel 상태 (서버 연동 후 반영될 값)
//    //val content by viewModel.content
//    //val saveResult by viewModel.saveResult
//
//
//    var selectedDefinition by remember { mutableStateOf("") }
//    var selectedExample by remember { mutableStateOf("") }
//
//    val penStates = listOf(
//        R.drawable.img_pen_black,
//        R.drawable.img_pen_yellow,
//        R.drawable.img_pen_blue
//    )
//    var currentPenIndex by remember { mutableStateOf(0) }
//
//    val quote by viewModel.quote.collectAsState()
//    var showBottomSheet by remember { mutableStateOf(false) }
//    val selectedWord by viewModel.selectedWord.collectAsState()
//
//
//    // ✅ 처음 진입 시 오늘의 학습 글귀 요청
//    LaunchedEffect(Unit) {
//        viewModel.fetchTodayQuote(token)
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(top = 32.dp, start = 16.dp, end = 32.dp)
//    ) {
//        Column(modifier = Modifier.fillMaxSize()) {
//
//            // 🔙 뒤로가기 + 제목
//            Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
//                Image(
//                    painter = painterResource(id = R.drawable.btn_img_back),
//                    contentDescription = "뒤로가기",
//                    modifier = Modifier
//                        .align(Alignment.CenterStart)
//                        .padding(start = 0.dp)  //  학습 진행률과 동일
//                        .size(28.dp)            //  크기 축소
//                        .clickable { onBackClick() }
//                )
//                Text(
//                    text = "오늘의 학습",
//                    fontSize = 20.sp,
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color(0xFF333333),
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            Text("학습 진행률",
//                fontSize = 16.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                color = Color(0xFF333333),
//                modifier = Modifier.padding(start = 4.dp))
//            Spacer(Modifier.height(12.dp))
//
//
//            StepProgressBar(totalSteps, currentStep)
//            Spacer(Modifier.height(20.dp))
//
//            // ✅ 학습 본문
//            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
//                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9F9F9)) {
//                    Text(
//                        text = quote,
//                        modifier = Modifier.padding(16.dp),
//                        fontSize = 14.sp,
//                        color = Color(0xFF333333)
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            // ✅ 하단 버튼
//            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                Image(
//                    painter = painterResource(id = penStates[currentPenIndex]),
//                    contentDescription = "펜",
//                    modifier = Modifier.size(64.dp).clickable {
//                        currentPenIndex = (currentPenIndex + 1) % penStates.size
//                        when (currentPenIndex) {
//                            1 -> {
//                                // 노란펜 → 더미 단어 선택
//                                viewModel.setSelectedWord(
//                                    WordItem("작성", "문서나 글 따위를 씀", "보고서를 작성하여 제출하세요.")
//                                )
//                                showBottomSheet = true
//                            }
//                            2 -> println("🔵 파란펜 → 서버 단어 하이라이트")
//                            else -> println("⚫ 검정펜 → 강조 제거")
//                        }
//                    }
//                )
//
//                Button(
//                    onClick = { onNextClick() },
//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                    shape = RoundedCornerShape(50),
//                    modifier = Modifier.height(42.dp).width(160.dp)
//                ) {
//                    Text("다음 단계", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
//                }
//            }
//        }
//    }
//
//    // ✅ 단어 BottomSheet
//    if (showBottomSheet && selectedWord != null) {
//        WordCollectBottomSheet(
//            word = selectedWord!!.word,
//            definition = selectedWord!!.meaning,
//            example = selectedWord!!.example ?: "",
//            onDismiss = { showBottomSheet = false },
//            onSaveClick = {
//                viewModel.saveWord(token, studyId = 1, wordItem = selectedWord!!) {
//                    showBottomSheet = false
//                }
//            }
//        )
//    }
//}
//
//// ✅ StepProgressBar는 UI 전용 → 연동 불필요
//@Composable
//fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
//    Row(
//        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        repeat(totalSteps) { index ->
//            Box(
//                modifier = Modifier.weight(1f).height(14.dp).background(
//                    color = if (index == currentStep - 1) Color(0xFF195FCF) else Color(0xFFF2F2F2),
//                    shape = RoundedCornerShape(50)
//                )
//            )
//        }
//    }
//}
//
///** ✅ Preview 전용 Wrapper (ViewModel 없이 contentText 미리보기) */
//@Composable
//fun StudyReadingScreenPreviewWrapper(contentText: String) {
//    Box(Modifier.fillMaxSize().background(Color.White)) {
//        Text(contentText, Modifier.align(Alignment.Center), fontSize = 16.sp, fontFamily = Pretendard)
//    }
//}
//
////@Preview(showBackground = true)
////@Composable
////fun PreviewStudyReadingScreen() {
////    StudyReadingScreenPreviewWrapper(
////        contentText = "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀가 있으며, 너희들은 시간을 느끼기 위해 가슴을 갖고 있다...”"
////    )
////}
//
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//fun PreviewStudyReadingScreen() {
//    // ✅ ViewModel 없이 UI만 테스트
//    StudyReadingScreen()
//}