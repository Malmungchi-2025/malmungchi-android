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
import com.malmungchi.feature.study.StudyReadingViewModel
import androidx.compose.material3.IconButton


@Composable
fun StudyReadingScreen(
    token: String = "dummy_token",
    viewModel: StudyReadingViewModel = hiltViewModel(),
    totalSteps: Int = 3,
    currentStep: Int = 1,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    val quote by viewModel.quote.collectAsState()
    val selectedWord by viewModel.selectedWord.collectAsState()
    val highlightWords by viewModel.highlightWords.collectAsState()
    val studyId by viewModel.studyId.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var yellowPenMode by remember { mutableStateOf(false) }

    val penStates = listOf(R.drawable.img_pen_black, R.drawable.img_pen_yellow, R.drawable.img_pen_blue)
    var currentPenIndex by remember { mutableStateOf(0) }

    // ✅ 오늘의 학습 글감 API 호출
    LaunchedEffect(Unit) { viewModel.fetchTodayQuote(token) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {

            // ✅ 상단 바 (뒤로가기 + 타이틀)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_img_back), // ✅ 기존 XML 리소스 사용
                        contentDescription = "뒤로가기",
                        modifier = Modifier.size(24.dp) // 필요 시 크기 조절
                    )
                }
                Text(
                    text = "오늘의 학습",
                    fontSize = 20.sp,
                    fontFamily = Pretendard,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width(48.dp)) // 오른쪽 균형 맞춤
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "학습 진행률",
                fontSize = 14.sp,
                color = Color.Black,              // ✅ 글씨 색상 변경
                modifier = Modifier.padding(start = 4.dp) // ✅ ProgressBar와 X축 정렬 맞춤
            )
            Spacer(Modifier.height(6.dp))
            StepProgressBar(totalSteps, currentStep)
            Spacer(Modifier.height(20.dp))

            // ✅ 본문
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9F9F9), modifier = Modifier.weight(1f)) {
                if (yellowPenMode) {
                    // 🟡 노란펜 모드 → 단어 클릭 가능
                    ClickableHighlightedText(text = quote) { word ->
                        viewModel.searchWord(token, word)
                        showBottomSheet = true
                    }
                } else {
                    // 일반 모드 or 파란펜 하이라이트
                    RegexHighlightedText(text = quote, highlights = highlightWords)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ✅ 하단 버튼 (펜 동작)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Image(
                    painter = painterResource(id = penStates[currentPenIndex]),
                    contentDescription = "펜",
                    modifier = Modifier.size(64.dp).clickable {
                        currentPenIndex = (currentPenIndex + 1) % penStates.size
                        when (currentPenIndex) {
                            1 -> { // 🟡 노란펜 → 단어 클릭 활성화
                                yellowPenMode = true
                            }
                            2 -> { // 🔵 파란펜 → 서버 단어 목록 하이라이트
                                yellowPenMode = false
                                studyId?.let { viewModel.loadVocabularyList(token, it) }
                            }
                            else -> yellowPenMode = false
                        }
                    }
                )

                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(42.dp).width(160.dp)
                ) {
                    Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
                }
            }
        }
    }

    // ✅ 단어 BottomSheet
    if (showBottomSheet && selectedWord != null) {
        WordCollectBottomSheet(
            word = selectedWord!!.word,
            definition = selectedWord!!.meaning,
            example = selectedWord!!.example ?: "",
            onDismiss = { showBottomSheet = false },
            onSaveClick = {
                viewModel.saveWord(token, selectedWord!!) {
                    showBottomSheet = false
                }
            }
        )
    }
}

/** ✅ 노란펜: 모든 단어 클릭 가능 텍스트 */
@Composable
fun ClickableHighlightedText(text: String, onWordClick: (String) -> Unit) {
    val words = text.split(" ")
    val annotated = buildAnnotatedString {
        var startIndex = 0
        for (word in words) {
            val endIndex = startIndex + word.length
            pushStringAnnotation(tag = "WORD", annotation = word)
            withStyle(SpanStyle(color = Color(0xFF333333), textDecoration = TextDecoration.None)) {
                append(word)
            }
            pop()
            append(" ")
            startIndex = endIndex + 1
        }
    }
    ClickableText(text = annotated, onClick = { offset ->
        annotated.getStringAnnotations("WORD", offset, offset).firstOrNull()?.let { sa ->
            val cleanWord = sa.item.replace(Regex("[^ㄱ-ㅎ가-힣a-zA-Z]"), "")
            if (cleanWord.isNotEmpty()) onWordClick(cleanWord)
        }
    }, modifier = Modifier.padding(16.dp))
}

/** ✅ 파란펜: 서버 단어 목록 Regex 하이라이트 */
@Composable
fun RegexHighlightedText(text: String, highlights: List<String>) {
    val annotated: AnnotatedString = buildAnnotatedString {
        append(text)
        highlights.forEach { word ->
            val regex = Regex(Regex.escape(word))
            regex.findAll(text).forEach { match ->
                addStyle(
                    style = SpanStyle(background = Color(0xFFB2C9FF)),
                    start = match.range.first,
                    end = match.range.last + 1
                )
            }
        }
    }
    Text(annotated, fontSize = 14.sp, color = Color(0xFF333333), modifier = Modifier.padding(16.dp))
}


/** ✅ 하이라이트된 텍스트 출력 */
@Composable
fun HighlightedText(text: String, highlights: List<String>) {
    val annotated: AnnotatedString = buildAnnotatedString {
        append(text)
        highlights.forEach { word ->
            val index = text.indexOf(word)
            if (index >= 0) {
                addStyle(
                    style = SpanStyle(background = Color(0xFFB2C9FF)),
                    start = index,
                    end = index + word.length
                )
            }
        }
    }
    Text(annotated, fontSize = 14.sp, color = Color(0xFF333333), modifier = Modifier.padding(16.dp))
}

/** ✅ ProgressBar */
@Composable
fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier.weight(1f).height(14.dp).background(
                    color = if (index == currentStep - 1) Color(0xFF195FCF) else Color(0xFFF2F2F2),
                    shape = RoundedCornerShape(50)
                )
            )
        }
    }
}

/** ✅ Preview */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewStudyReadingScreen() {
    StudyReadingScreen()
}


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