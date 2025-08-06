package com.malmungchi.feature.study.second

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.StudyReadingViewModel



// ✅ ProgressBar (2단계까지 파란색)
@Composable
fun StepProgressBarPreview(totalSteps: Int = 3, currentStep: Int = 2) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier.weight(1f).height(14.dp).background(
                    color = if (index < currentStep) Color(0xFF195FCF) else Color(0xFFF2F2F2),
                    shape = RoundedCornerShape(50)
                )
            )
        }
    }
}

// ✅ 입력된 부분만 검정색, 나머지 회색
@Composable
fun TypingTextOverlay(original: String, typed: String) {
    val annotated = buildAnnotatedString {
        val matchCount = typed.zip(original).takeWhile { (t, o) -> t == o }.count()
        append(AnnotatedString(original.take(matchCount), SpanStyle(color = Color.Black)))
        append(AnnotatedString(original.drop(matchCount), SpanStyle(color = Color(0xFF989898))))
    }
    Text(
        annotated,
        fontSize = 16.sp,
        fontFamily = Pretendard,
        lineHeight = 26.sp
    )
}

@Composable
fun TypingText(original: String, typed: String) {
    val annotated: AnnotatedString = buildAnnotatedString {
        var matchIndex = 0
        for (i in typed.indices) {
            if (i < original.length && typed[i] == original[i]) {
                matchIndex++
            } else break
        }
        append(AnnotatedString(original.take(matchIndex), spanStyle = SpanStyle(color = Color.Black)))
        append(AnnotatedString(original.drop(matchIndex), spanStyle = SpanStyle(color = Color(0xFF989898))))
    }
    Text(annotated, fontSize = 16.sp, fontFamily = Pretendard, lineHeight = 24.sp)
}

// ✅ 상단바
@Composable
fun TopBar(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                painter = painterResource(id = R.drawable.btn_img_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified
            )
        }
        Text(
            text = title,
            fontSize = 20.sp,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
            color = Color.Black
        )
        Spacer(Modifier.width(48.dp))
    }
}

// ✅ 하단 버튼
@Composable
fun BottomNavigationButtons(onBackClick: () -> Unit, onNextClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedButton(
            onClick = onBackClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
            modifier = Modifier.height(42.dp).width(160.dp)
        ) {
            Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard)
        }

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
/**
 * ✅ 2단계 필사 화면 (기존 UI 그대로, 필사 입력 부분만 수정)
 */
@Composable
fun StudySecondScreen(
    token: String,
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    val sentences by viewModel.sentences.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val savedInputs = remember { mutableStateMapOf<Int, String>() }
    val listState = rememberLazyListState()
    var showAlert by remember { mutableStateOf(false) } // ✅ Alert 상태 추가

    val focusRequesters = remember { mutableStateListOf<FocusRequester>() }

    // ✅ focusRequesters 크기를 sentences 수와 맞춤
    if (focusRequesters.size < sentences.size) {
        repeat(sentences.size - focusRequesters.size) {
            focusRequesters.add(FocusRequester())
        }
    }

    // ✅ 최초 진입 시 서버 데이터 로드 및 UI 동기화
    LaunchedEffect(Unit) {
        viewModel.initHandwritingStudy(token)
        viewModel.fetchHandwriting(token) { loaded ->
            savedInputs.clear()
            savedInputs.putAll(loaded)
        }
    }

    // ✅ 현재 문장으로 스크롤 및 포커스 이동
    LaunchedEffect(currentIndex) {
        listState.animateScrollToItem(index = currentIndex, scrollOffset = -20)
        focusRequesters.getOrNull(currentIndex)?.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        TopBar(title = "오늘의 학습", onBackClick = onBackClick)

        Spacer(Modifier.height(24.dp))
        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.height(16.dp))
        StepProgressBarPreview(totalSteps = 3, currentStep = 2)
        Spacer(Modifier.height(24.dp))

        // ✅ LazyColumn으로 문장별 입력 카드 표시
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(sentences) { index, sentence ->
                val isEnabled = index <= currentIndex

                // 🔥 입력이 완성되면 자동으로 다음 문장으로 이동
                LaunchedEffect(savedInputs[index]) {
                    val input = savedInputs[index]?.trim() ?: ""
                    if (isEnabled && input.equals(sentence.trim(), ignoreCase = true) && index == currentIndex) {
                        viewModel.saveAllInputs(savedInputs.toMap())
                        if (currentIndex < sentences.size - 1) {
                            viewModel.nextSentence()
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = sentence,
                            fontSize = 16.sp,
                            fontFamily = Pretendard,
                            color = Color(0xFF444444),
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = savedInputs[index] ?: viewModel.getInputFor(index),
                            onValueChange = { savedInputs[index] = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequesters.getOrNull(index) ?: FocusRequester()),
                            enabled = isEnabled,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF9F9F9),
                                unfocusedContainerColor = Color(0xFFF9F9F9),
                                disabledContainerColor = Color(0xFFEFEFEF)
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ✅ 하단 버튼
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = { if (currentIndex > 0) viewModel.previousSentence() },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                modifier = Modifier.height(42.dp).width(160.dp)
            ) {
                Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard)
            }

            Button(
                onClick = {
                    viewModel.saveAllInputs(savedInputs.toMap())
                    if (currentIndex < sentences.size - 1) {
                        // ✅ 아직 필사가 완료되지 않으면 Alert 띄움
                        showAlert = true
                    } else {
                        // ✅ 전부 완료 → 다음 단계
                        viewModel.finalizeHandwriting(token, onNextClick)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(42.dp).width(160.dp)
            ) {
                Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
            }
        }
    }

    // ✅ Alert 연결
    if (showAlert) {
        SkipHandwritingAlert.Show(
            onConfirm = {
                showAlert = false
                onNextClick() // 👉 "네" → 메인 화면으로 이동
            },
            onDismiss = {
                showAlert = false
                // 👉 "아니요" → Alert만 닫고 필사 화면 유지
            }
        )
    }
}
//@Composable
//fun StudySecondScreen(
//    token: String,
//    viewModel: StudyReadingViewModel = hiltViewModel(),
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    val sentences by viewModel.sentences.collectAsState()
//    val currentIndex by viewModel.currentIndex.collectAsState()
//    val savedInputs = remember { mutableStateMapOf<Int, String>() }
//    val listState = rememberLazyListState()
//
//    val focusRequesters = remember { mutableStateListOf<FocusRequester>() }
//
//    // ✅ focusRequesters 크기를 sentences 수와 맞춤
//    if (focusRequesters.size < sentences.size) {
//        repeat(sentences.size - focusRequesters.size) {
//            focusRequesters.add(FocusRequester())
//        }
//    }
//
//    // ✅ 화면 진입 시 문장 초기화 호출
//    LaunchedEffect(Unit) {
//        viewModel.initHandwritingStudy(token)
//        // ✅ 서버에서 기존 필사 내용을 가져오면 UI의 savedInputs도 동기화
//        viewModel.fetchHandwriting(token) { loaded ->
//            savedInputs.clear()
//            savedInputs.putAll(loaded)
//        }
//    }
//
//
//    // ✅ API 호출 유지
//    LaunchedEffect(currentIndex) {
//        listState.animateScrollToItem(index = currentIndex, scrollOffset = -20)
//        focusRequesters.getOrNull(currentIndex)?.requestFocus() // ✅ 자동 포커스 추가
//    }
//
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        TopBar(title = "오늘의 학습", onBackClick = onBackClick)
//
//        Spacer(Modifier.height(24.dp))
//        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
//        Spacer(Modifier.height(16.dp))
//        StepProgressBarPreview(totalSteps = 3, currentStep = 2)
//        Spacer(Modifier.height(24.dp))
//
//        // ✅ LazyColumn으로 문장별 입력 카드 표시
//        LazyColumn(
//            state = listState,
//            modifier = Modifier.weight(1f),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            itemsIndexed(sentences) { index, sentence ->
//                val isEnabled = index <= currentIndex
//
//                // 🔥 입력이 완성되면 자동으로 다음 문장으로 이동
//                LaunchedEffect(savedInputs[index]) {
//                    val input = savedInputs[index]?.trim() ?: ""
//                    if (isEnabled && input.trim() == sentence.trim() && index == currentIndex) {
//                        // ✅ 현재 문장을 다 입력한 경우 → 자동 다음 문장 이동
//                        viewModel.saveAllInputs(savedInputs.toMap())
//                        if (currentIndex < sentences.size - 1) {
//                            viewModel.nextSentence()
//                        }
//                    }
//                }
//
//                Surface(
//                    shape = RoundedCornerShape(12.dp),
//                    shadowElevation = 4.dp,
//                    color = Color.White,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Column(Modifier.padding(16.dp)) {
//                        Text(
//                            text = sentence,
//                            fontSize = 16.sp,
//                            fontFamily = Pretendard,
//                            color = Color(0xFF444444),
//                            lineHeight = 24.sp
//                        )
//                        Spacer(Modifier.height(8.dp))
//                        TextField(
//                            value = savedInputs[index] ?: viewModel.getInputFor(index),
//                            onValueChange = { savedInputs[index] = it },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .focusRequester(focusRequesters[index]),
//                            enabled = isEnabled,
//                            colors = TextFieldDefaults.colors(
//                                focusedContainerColor = Color(0xFFF9F9F9),
//                                unfocusedContainerColor = Color(0xFFF9F9F9),
//                                disabledContainerColor = Color(0xFFEFEFEF)
//                            )
//                        )
//                    }
//                }
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        // ✅ 기존 버튼 유지 + 동작만 변경
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            OutlinedButton(
//                onClick = {
//                    if (currentIndex > 0) viewModel.previousSentence()
//                },
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard)
//            }
//
//            Button(
//                onClick = {
//                    viewModel.saveAllInputs(savedInputs.toMap()) // 🔥 현재 입력 저장
//                    if (currentIndex == sentences.size - 1) {
//                        viewModel.finalizeHandwriting(token, onNextClick)
//                    } else {
//                        viewModel.nextSentence()
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                shape = RoundedCornerShape(50),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
//            }
//        }
//    }
//}
//@Composable
//fun StudySecondScreen(
//    token: String,
//    viewModel: StudyReadingViewModel = hiltViewModel(),
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    // 🔥 기존 quote 대신 문장 리스트 StateFlow 사용
//    // 🔥 기존 userInput은 현재 문장 입력값으로만 사용
//    val sentences by viewModel.sentences.collectAsState()   // 🔥 추가
//    val currentIndex by viewModel.currentIndex.collectAsState() // 🔥 추가
//    val userInput by viewModel.userInput.collectAsState()
//
//    val currentSentence = sentences.getOrNull(currentIndex) ?: "" // 🔥 현재 문장 추출
//
//    // ✅ 기존 API 호출 유지
//    LaunchedEffect(Unit) {
//        viewModel.fetchTodayQuote(token)
//        viewModel.fetchHandwriting(token)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        TopBar(title = "오늘의 학습", onBackClick = onBackClick)
//
//        Spacer(Modifier.height(24.dp))
//        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
//        Spacer(Modifier.height(16.dp))
//        StepProgressBarPreview(totalSteps = 3, currentStep = 2)
//        Spacer(Modifier.height(24.dp))
//
//        // 🔥 기존 Surface 유지 but 그림자(shadowElevation) + 문장별 필사 로직으로 변경
//        Surface(
//            shape = RoundedCornerShape(12.dp),
//            shadowElevation = 4.dp, // 🔥 그림자 추가
//            color = Color.White,
//            modifier = Modifier.weight(1f)
//        ) {
//            Column(
//                modifier = Modifier.fillMaxSize().padding(20.dp),
//                verticalArrangement = Arrangement.Top
//            ) {
//                // 🔥 기존 TypingTextOverlay → 현재 문장 단순 표시
//                Text(
//                    text = currentSentence,
//                    fontSize = 16.sp,
//                    fontFamily = Pretendard,
//                    color = Color(0xFF444444),
//                    lineHeight = 26.sp
//                )
//
//                Spacer(Modifier.height(16.dp))
//
//                // 🔥 기존 BasicTextField 제거 → TextField로 변경
//                // 🔥 userInput → 현재 문장 입력 StateFlow 연결
//                TextField(
//                    value = userInput,
//                    onValueChange = { viewModel.onUserInputChange(it) },
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = TextFieldDefaults.colors( // ✅ Material3 방식
//                        focusedContainerColor = Color(0xFFF9F9F9),
//                        unfocusedContainerColor = Color(0xFFF9F9F9)
//                    )
//                )
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        // 🔥 버튼 클릭 시 다음 문장 이동 로직 추가
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            OutlinedButton(
//                onClick = { viewModel.previousSentence() }, // 🔥 기존 onBackClick → 이전 문장 이동
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard)
//            }
//
//            Button(
//                onClick = {
//                    viewModel.saveCurrentInput() // ✅ token 제거 (ViewModel 함수와 맞춤)
//                    if (currentIndex == sentences.size - 1) {
//                        viewModel.finalizeHandwriting(token, onNextClick)
//                    } else {
//                        viewModel.nextSentence()
//                    }
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                shape = RoundedCornerShape(50),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
//            }
//        }
//    }
//}

///**
// * ✅ 2단계 필사 화면
// */
//@Composable
//fun StudySecondScreen(
//    token: String,
//    viewModel: StudyReadingViewModel = hiltViewModel(),
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    val quote by viewModel.quote.collectAsState()
//    val userInput by viewModel.userInput.collectAsState()
//
//    // ✅ 진입 시 API 호출
//    LaunchedEffect(Unit) {
//        viewModel.fetchTodayQuote(token)
//        viewModel.fetchHandwriting(token)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        TopBar(title = "오늘의 학습", onBackClick = onBackClick)
//
//        Spacer(Modifier.height(24.dp))
//        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
//        Spacer(Modifier.height(16.dp))
//        StepProgressBarPreview(totalSteps = 3, currentStep = 2)
//        Spacer(Modifier.height(24.dp))
//
//        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9F9F9), modifier = Modifier.weight(1f)) {
//            Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
//                TypingTextOverlay(original = quote, typed = userInput)
//
//                BasicTextField(
//                    value = userInput,
//                    onValueChange = { newValue ->
//                        if (newValue.length <= quote.length) {
//                            viewModel.onUserInputChange(newValue)
//                        }
//                    },
//                    textStyle = LocalTextStyle.current.copy(color = Color.Transparent),
//                    cursorBrush = SolidColor(Color.Transparent),
//                    modifier = Modifier.matchParentSize()
//                )
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            OutlinedButton(
//                onClick = onBackClick,
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard)
//            }
//
//            Button(
//                onClick = {
//                    viewModel.saveHandwriting(
//                        token,
//                        onSuccess = { onNextClick() },
//                        onFailure = { e -> Log.e("HANDWRITING", "저장 실패: ${e.message}") }
//                    )
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                shape = RoundedCornerShape(50),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
//            }
//        }
//    }
//}

// ✅ Preview용 ViewModel Stub
class StudySecondPreviewViewModel {
    private val _quote = mutableStateOf("사과다. 이 문장을 필사해 보세요.")
    val quote: State<String> get() = _quote

    private val _userInput = mutableStateOf("사")
    val userInput: State<String> get() = _userInput

    fun onUserInputChange(newInput: String) {
        _userInput.value = newInput
    }
}

open class StudySecondViewModelStub(private val previewVM: StudySecondPreviewViewModel) {
    val quote: State<String> get() = previewVM.quote
    val userInput: State<String> get() = previewVM.userInput
    fun onUserInputChange(newInput: String) = previewVM.onUserInputChange(newInput)
}

/**
 * ✅ Preview
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewStudySecondScreen() {
    val fakeQuote = "사과다. 이 문장을 필사해 보세요."
    val fakeInput = remember { mutableStateOf("사") }

    Column(
        Modifier.fillMaxSize().background(Color.White).padding(16.dp)
    ) {
        TopBar(title = "오늘의 학습", onBackClick = {})

        Spacer(Modifier.height(24.dp))
        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.height(16.dp))
        StepProgressBarPreview(totalSteps = 3, currentStep = 2)
        Spacer(Modifier.height(24.dp))

        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9F9F9), modifier = Modifier.weight(1f)) {
            Box(Modifier.fillMaxSize().padding(20.dp)) {
                TypingTextOverlay(original = fakeQuote, typed = fakeInput.value)
                BasicTextField(
                    value = fakeInput.value,
                    onValueChange = { newValue -> if (newValue.length <= fakeQuote.length) fakeInput.value = newValue },
                    textStyle = LocalTextStyle.current.copy(color = Color.Transparent),
                    cursorBrush = SolidColor(Color.Transparent),
                    modifier = Modifier.matchParentSize()
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        BottomNavigationButtons(onBackClick = {}, onNextClick = {})
    }
}

//
///**
// * ✅ 2단계 필사 화면
// */
//@Composable
//fun StudySecondScreen(
//    token: String,
//    viewModel: StudyReadingViewModel = hiltViewModel(),   // ✅ 실제 앱에서는 Hilt ViewModel 사용
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    val quote by viewModel.quote.collectAsState()
//    val userInput by viewModel.userInput.collectAsState()
//
//    // ✅ 화면 진입 시 서버에서 기존 필사 내용 로드
//    LaunchedEffect(Unit) {
//        viewModel.fetchTodayQuote(token)       // 글감 불러오기
//        viewModel.fetchHandwriting(token)      // 🔹 기존 필사 내용 불러오기
//    }
//
//
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        /** 🔹 상단 바 (오늘의 학습) */
//        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//            IconButton(onClick = onBackClick) {
//                Icon(
//                    painter = painterResource(id = R.drawable.btn_img_back),
//                    contentDescription = "뒤로가기",
//                    tint = Color.Unspecified
//                )
//            }
//            Text(
//                text = "오늘의 학습",
//                fontSize = 20.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.SemiBold,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.weight(1f),
//                color = Color.Black
//            )
//            Spacer(Modifier.width(48.dp))
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        /** 🔹 학습 진행률 */
//        Text(
//            "학습 진행률",
//            fontSize = 16.sp,
//            color = Color.Black,
//            fontWeight = FontWeight.Normal,
//            modifier = Modifier.padding(start = 8.dp)
//        )
//        Spacer(Modifier.height(16.dp))   // ✅ 동일한 간격 (텍스트 → 프로그래스바)
//        StepProgressBarPreview(totalSteps = 3, currentStep = 2)
//        Spacer(Modifier.height(24.dp))   // ✅ 동일한 간격 (프로그래스바 → 다음 UI)
//
//        /** 🔹 필사 입력 영역 (글감 자체가 입력 UI) */
//        Surface(
//            shape = RoundedCornerShape(12.dp),
//            color = Color(0xFFF9F9F9),
//            modifier = Modifier.weight(1f)
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(20.dp)
//            ) {
//                // ✅ 회색+검정 변환되는 본문
//                TypingTextOverlay(original = quote, typed = userInput)
//
//                // ✅ 투명 입력창 (실제 입력은 여기서)
//                BasicTextField(
//                    value = userInput,
//                    onValueChange = { newValue ->
//                        if (newValue.length <= quote.length) {
//                            viewModel.onUserInputChange(newValue) // ✅ 여기 수정됨
//                        }
//                    },
//                    textStyle = LocalTextStyle.current.copy(color = Color.Transparent),
//                    cursorBrush = SolidColor(Color.Transparent), // 커서도 숨김
//                    modifier = Modifier
//                        .matchParentSize() // 글 위에 완전히 덮음
//                )
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//        /** 🔹 하단 버튼 */
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//            OutlinedButton(
//                onClick = onBackClick,
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) { Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard) }
//
//            Button(
//                onClick = {
//                    viewModel.saveHandwriting(
//                        token,
//                        onSuccess = { onNextClick() },
//                        onFailure = { e -> Log.e("HANDWRITING", "저장 실패: ${e.message}") }
//                    )
//                },
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                shape = RoundedCornerShape(50),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
//            }
//    }
//}
//@Composable
//fun TypingTextOverlay(original: String, typed: String) {
//    val annotated = buildAnnotatedString {
//        val matchCount = typed.zip(original).takeWhile { (t, o) -> t == o }.count()
//        append(AnnotatedString(original.take(matchCount), SpanStyle(color = Color.Black)))
//        append(AnnotatedString(original.drop(matchCount), SpanStyle(color = Color(0xFF989898))))
//    }
//    Text(
//        annotated,
//        fontSize = 16.sp,
//        fontFamily = Pretendard,
//        lineHeight = 26.sp
//    )
//}
//
//
///**
// * ✅ 입력된 부분만 검정색, 나머지 회색으로 렌더링
// */
//@Composable
//fun TypingText(original: String, typed: String) {
//    val annotated: AnnotatedString = buildAnnotatedString {
//        var matchIndex = 0
//        for (i in typed.indices) {
//            if (i < original.length && typed[i] == original[i]) {
//                matchIndex++
//            } else break
//        }
//        append(AnnotatedString(original.take(matchIndex), spanStyle = SpanStyle(color = Color.Black)))
//        append(AnnotatedString(original.drop(matchIndex), spanStyle = SpanStyle(color = Color(0xFF989898))))
//    }
//    Text(annotated, fontSize = 16.sp, fontFamily = Pretendard, lineHeight = 24.sp)
//}
//
///**
// * ✅ ProgressBar (2단계까지 파란색)
// */
//@Composable
//fun StepProgressBarPreview(totalSteps: Int = 3, currentStep: Int = 2) {
//    Row(
//        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        repeat(totalSteps) { index ->
//            Box(
//                modifier = Modifier.weight(1f).height(14.dp).background(
//                    color = if (index < currentStep) Color(0xFF195FCF) else Color(0xFFF2F2F2),
//                    shape = RoundedCornerShape(50)
//                )
//            )
//        }
//    }
//}
//
///**
// * ✅ Preview용 ViewModel (collectAsState 대신 mutableStateOf 사용)
// */
//class StudySecondPreviewViewModel {
//    private val _quote = mutableStateOf("사과다. 이 문장을 필사해 보세요.")
//    val quote: State<String> get() = _quote
//
//    private val _userInput = mutableStateOf("사")
//    val userInput: State<String> get() = _userInput
//
//    fun onUserInputChange(newInput: String) {
//        _userInput.value = newInput
//    }
//}
//
//
///**
// * ✅ Preview
// */
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//fun PreviewStudySecondScreen() {
//    val fakeQuote = "사과다. 이 문장을 필사해 보세요."
//    val fakeInput = remember { mutableStateOf("사") }
//
//    Column(
//        Modifier.fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        TopBar(title = "오늘의 학습", onBackClick = {})
//
//        Spacer(Modifier.height(24.dp))
//        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
//        Spacer(Modifier.height(16.dp))
//        StepProgressBarPreview(totalSteps = 3, currentStep = 2)
//        Spacer(Modifier.height(24.dp))
//
//        // ✅ 실제 UI 구조와 동일하게 Preview에서도 사용
//        Surface(
//            shape = RoundedCornerShape(12.dp),
//            color = Color(0xFFF9F9F9),
//            modifier = Modifier.weight(1f)
//        ) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(20.dp)
//            ) {
//                TypingTextOverlay(original = fakeQuote, typed = fakeInput.value)
//
//                BasicTextField(
//                    value = fakeInput.value,
//                    onValueChange = { newValue ->
//                        if (newValue.length <= fakeQuote.length) fakeInput.value = newValue
//                    },
//                    textStyle = LocalTextStyle.current.copy(color = Color.Transparent),
//                    cursorBrush = SolidColor(Color.Transparent),
//                    modifier = Modifier.matchParentSize()
//                )
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//        BottomNavigationButtons(onBackClick = {}, onNextClick = {})
//    }
//}
//@Composable
//fun TopBar(title: String, onBackClick: () -> Unit) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        IconButton(onClick = onBackClick) {
//            Icon(
//                painter = painterResource(id = R.drawable.btn_img_back),
//                contentDescription = "뒤로가기",
//                tint = Color.Unspecified
//            )
//        }
//        Text(
//            text = title,
//            fontSize = 20.sp,
//            fontFamily = Pretendard,
//            fontWeight = FontWeight.SemiBold,
//            textAlign = TextAlign.Center,
//            modifier = Modifier.weight(1f),
//            color = Color.Black
//        )
//        Spacer(Modifier.width(48.dp))
//    }
//}
//
//@Composable
//fun BottomNavigationButtons(
//    onBackClick: () -> Unit,
//    onNextClick: () -> Unit
//) {
//    Row(
//        Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        OutlinedButton(
//            onClick = onBackClick,
//            shape = RoundedCornerShape(50),
//            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
//            modifier = Modifier.height(42.dp).width(160.dp)
//        ) {
//            Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard)
//        }
//
//        Button(
//            onClick = onNextClick,
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//            shape = RoundedCornerShape(50),
//            modifier = Modifier.height(42.dp).width(160.dp)
//        ) {
//            Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
//        }
//    }
//}
///**
// * ✅ Preview에서 collectAsState()를 흉내 내기 위한 Stub
// */
//open class StudySecondViewModelStub(private val previewVM: StudySecondPreviewViewModel) {
//    val quote: State<String> get() = previewVM.quote
//    val userInput: State<String> get() = previewVM.userInput
//    fun onUserInputChange(newInput: String) = previewVM.onUserInputChange(newInput)
//}}
