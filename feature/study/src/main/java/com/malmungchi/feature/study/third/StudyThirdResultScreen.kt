package com.malmungchi.feature.study.third

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.StudyReadingViewModel
import com.malmungchi.feature.study.second.StepProgressBarPreview
import com.malmungchi.feature.study.second.TopBar


// --------------------- DATA MODEL -------------------------
data class StudyResultQuestion(
    val question: String,
    val choices: List<String>,
    val correctIndex: Int,
    val userAnswerIndex: Int?,
    val explanation: String
)

// --------------------- COLORS -------------------------
private val BrandBlue = Color(0xFF195FCF)
private val BgBlue = Color(0xFFEFF4FB)
private val CorrectFill = Color(0xFFD1DFF5)
private val WrongRed = Color(0xFFFF0000)
private val WrongFill = Color(0xFFFFCCCC)
private val LabelGray = Color(0xFF616161)


// --------------------- CHOICE ROW -------------------------
@Composable
private fun ChoiceRowModern(
    text: String,
    isCorrectAnswer: Boolean,
    isUserSelectedWrong: Boolean
) {
    val bgColor = when {
        isCorrectAnswer -> CorrectFill
        isUserSelectedWrong -> WrongFill
        else -> Color.White
    }
    val borderColor = when {
        isCorrectAnswer -> BrandBlue
        isUserSelectedWrong -> WrongRed
        else -> Color(0xFFE0E0E0)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                lineHeight = 24.sp
            )
        }
    }
}


// --------------------- 해설 카드 -------------------------
@Composable
private fun AnswerExplanationBlock(
    answerText: String,
    explanation: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp, topEnd = 0.dp,
                    bottomStart = 12.dp, bottomEnd = 12.dp
                )
            )
            .background(BgBlue)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text("정답", fontSize = 12.sp, fontFamily = Pretendard, color = LabelGray)
        Spacer(Modifier.height(8.dp))

        Text(
            text = answerText,
            fontSize = 14.sp,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(16.dp))

        Text("해설", fontSize = 12.sp, fontFamily = Pretendard, color = LabelGray)
        Spacer(Modifier.height(4.dp))

        Text(
            text = explanation,
            fontSize = 14.sp,
            fontFamily = Pretendard,
            color = Color.Black
        )
    }
}


// --------------------- Wrapper -------------------------
@Composable
fun StudyThirdResultScreenWrapper(
    studyId: Int,
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onFinishClick: () -> Unit = {}
) {
    BackHandler { onBackClick() }

    val quizList by viewModel.quizList.collectAsState()

    LaunchedEffect(studyId) {
        viewModel.loadQuizList(studyId)
    }

    if (quizList.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val resultQuestions = quizList.map { quiz ->
        StudyResultQuestion(
            question = quiz.question,
            choices = quiz.options,
            correctIndex = quiz.options.indexOf(quiz.answer),
            userAnswerIndex = quiz.userChoice?.let { quiz.options.indexOf(it) },
            explanation = quiz.explanation
        )
    }

    StudyThirdResultScreen(
        questions = resultQuestions,
        onBackClick = onBackClick,
        onFinishClick = onFinishClick
    )
}


// --------------------- Main UI -------------------------
@Composable
fun StudyThirdResultScreen(
    questions: List<StudyResultQuestion>,
    onBackClick: () -> Unit,
    onFinishClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 48.dp)
    ) {
        TopBar("오늘의 학습", onBackClick)

        Spacer(Modifier.height(24.dp))
        Text("학습 진행률", fontFamily = Pretendard, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        StepProgressBarPreview(totalSteps = 3, currentStep = 3)
        Spacer(Modifier.height(16.dp))

        // -----------------------------------------------
        // 🔥 문제카드 + 해설카드 + 다음 문제 (정확한 B타입 구조)
        // -----------------------------------------------
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            itemsIndexed(questions) { index, q ->

                Column(Modifier.fillMaxWidth()) {   // ← Column이 전체를 감싸야 함

                    // ---------- ① Box: 아이콘 + 문제카드를 겹쳐 배치 ----------
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {


                        // -------------------- 아이콘 Overlay --------------------
                        val resultIcon = if (q.userAnswerIndex == q.correctIndex)
                            R.drawable.ic_correct else R.drawable.ic_wrong

                        Image(
                            painter = painterResource(id = resultIcon),
                            contentDescription = null,
                            modifier = Modifier
                                //.align(Alignment.Start)
                                .size(120.dp)
                                .offset(x = (-36).dp, y = (-28).dp)
                                .zIndex(10f)
                        )

                        // -------------------- 문제 카드 --------------------
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .zIndex(5f),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {

                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    "${index + 1}/${questions.size}",
                                    fontSize = 12.sp,
                                    fontFamily = Pretendard,
                                    color = LabelGray
                                )

                                Spacer(Modifier.height(8.dp))

                                Text(
                                    q.question,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = Pretendard,
                                    lineHeight = 26.sp,
                                    color = Color.Black
                                )

                                Spacer(Modifier.height(16.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    q.choices.forEachIndexed { i, choice ->
                                        ChoiceRowModern(
                                            text = choice,
                                            isCorrectAnswer = (i == q.correctIndex),
                                            isUserSelectedWrong =
                                                (i == q.userAnswerIndex && q.userAnswerIndex != q.correctIndex)
                                        )
                                    }
                                }
                            }
                        }

                        // -------------------- 문제 → 해설 (12dp) --------------------
                        Spacer(Modifier.height(12.dp))

                        // -------------------- 해설 카드 --------------------
                        AnswerExplanationBlock(
                            answerText = q.choices[q.correctIndex],
                            explanation = q.explanation
                        )

                        // -------------------- 해설 → 다음 문제 (32dp) --------------------
                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }

        // -------------------- 하단 버튼 --------------------
        Button(
            onClick = onFinishClick,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .height(42.dp)
                .width(160.dp)
                .align(Alignment.End)
        ) {
            Text("다음 단계", color = Color.White, fontFamily = Pretendard)
        }
    }
}

// --------------------- PREVIEW 1 : 기본 ---------------------
@Composable
@Preview(
    name = "StudyThirdResult - 기본",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
fun PreviewStudyThirdResultScreen_New() {

    val mockQuestions = listOf(
        StudyResultQuestion(
            question = "이 글의 핵심 내용을 가장 잘 요약한 것은?",
            choices = listOf("요약1", "요약2", "요약3", "요약4"),
            correctIndex = 0,
            userAnswerIndex = 1, // 오답
            explanation = "핵심은 '요약1'이며 글의 중심 문장과 일치합니다."
        ),
        StudyResultQuestion(
            question = "두 번째 문제입니다.",
            choices = listOf("A", "B", "C", "D"),
            correctIndex = 2,
            userAnswerIndex = 2, // 정답
            explanation = "문맥상 정답은 C가 자연스럽습니다."
        ),
        StudyResultQuestion(
            question = "세 번째 문제입니다.",
            choices = listOf("ㄱ", "ㄴ", "ㄷ", "ㄹ"),
            correctIndex = 1,
            userAnswerIndex = null, // 미응답
            explanation = "선택지 'ㄴ'이 지문 조건과 가장 일치합니다."
        )
    )

    StudyThirdResultScreen(
        questions = mockQuestions,
        onBackClick = {},
        onFinishClick = {}
    )
}



// --------------------- PREVIEW 2 : 긴 해설 테스트 ---------------------
@Composable
@Preview(
    name = "StudyThirdResult - 긴 해설",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
fun PreviewStudyThirdResultScreen_LongText() {

    val longExplanation =
        "해설이 길어지는 경우에도 BgBlue 영역에서 줄바꿈과 가독성이 유지됩니다. " +
                "중요 포인트: ① 핵심 문장 파악 ② 문맥 재확인 ③ 오답 제거 전략. " +
                "필요하면 예시 문장을 더 보면서 지문의 구조를 비교해보세요."

    val mockQuestions = listOf(
        StudyResultQuestion(
            question = "정답/해설 카드가 길어질 때도 UI가 깨지지 않는지 테스트합니다.",
            choices = listOf("항상 그렇다", "대략 그렇다", "상황에 따라 다르다", "아니다"),
            correctIndex = 0,
            userAnswerIndex = 3,
            explanation = longExplanation
        )
    )

    StudyThirdResultScreen(
        questions = mockQuestions,
        onBackClick = {},
        onFinishClick = {}
    )
}




//package com.malmungchi.feature.study.third
//
//
//import androidx.activity.compose.BackHandler
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateMapOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.zIndex
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.malmungchi.feature.study.Pretendard
//import com.malmungchi.feature.study.R
//import com.malmungchi.feature.study.StudyReadingViewModel
//import com.malmungchi.feature.study.second.StepProgressBarPreview
//import com.malmungchi.feature.study.second.TopBar
//import kotlinx.coroutines.launch
//
//
//// --------------------- DATA -------------------------
//
//data class StudyResultQuestion(
//    val question: String,
//    val choices: List<String>,
//    val correctIndex: Int,
//    val userAnswerIndex: Int?,
//    val explanation: String
//)
//
//// --------------------- COLORS -------------------------
//
//private val BrandBlue = Color(0xFF195FCF)
//private val BgBlue = Color(0xFFEFF4FB)
//private val CorrectFill = Color(0xFFD1DFF5)
//private val WrongRed = Color(0xFFFF0000)
//private val WrongFill = Color(0xFFFFCCCC)
//private val LabelGray = Color(0xFF616161)
//
//// --------------------- CHOICE OPTION -------------------------
//
//@Composable
//private fun ChoiceRowModern(
//    text: String,
//    isCorrectAnswer: Boolean,
//    isUserSelectedWrong: Boolean
//) {
//    val bgColor = when {
//        isCorrectAnswer -> CorrectFill
//        isUserSelectedWrong -> WrongFill
//        else -> Color.White
//    }
//
//    val borderColor = when {
//        isCorrectAnswer -> BrandBlue
//        isUserSelectedWrong -> WrongRed
//        else -> Color(0xFFE0E0E0)
//    }
//
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(64.dp)
//            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
//        shape = RoundedCornerShape(12.dp),
//        color = bgColor
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 14.dp),
//            contentAlignment = Alignment.CenterStart
//        ) {
//            Text(
//                text = text,
//                fontSize = 16.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black,
//                lineHeight = 24.sp
//            )
//        }
//    }
//}
//
//// --------------------- ANSWER + EXPLANATION -------------------------
//
//@Composable
//private fun AnswerExplanationBlock(
//    answerText: String,
//    explanation: String
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(
//                RoundedCornerShape(
//                    topStart = 0.dp, topEnd = 0.dp,
//                    bottomStart = 12.dp, bottomEnd = 12.dp
//                )
//            )
//            .background(BgBlue)
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(top = 32.dp, bottom = 24.dp)
//                .padding(horizontal = 20.dp)
//        ) {
//            Text("정답", fontSize = 12.sp, fontFamily = Pretendard, color = LabelGray)
//            Spacer(Modifier.height(8.dp))
//
//            Text(
//                text = answerText,
//                fontSize = 14.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            Text("해설", fontSize = 12.sp, fontFamily = Pretendard, color = LabelGray)
//            Spacer(Modifier.height(4.dp))
//
//            Text(
//                text = explanation,
//                fontSize = 14.sp,
//                fontFamily = Pretendard,
//                color = Color.Black
//            )
//        }
//    }
//}
//
//
///**
// * API 연동을 위한 Wrapper Composable
// * UI 코드는 절대 건들지 않고, 서버에서 퀴즈 불러오고
// * 사용자의 정답을 ViewModel 통해 서버에 저장하는 로직만 추가
// */
//@Composable
//fun StudyThirdResultScreenWrapper(
//    //token: String,
//    studyId: Int,
//    viewModel: StudyReadingViewModel = hiltViewModel(),
//    onBackClick: () -> Unit = {},
//    onFinishClick: () -> Unit = {}
//) {
//    //android.util.Log.d("QUIZ_RESULT", "🟢 ResultScreen 들어옴 token=${token.take(8)}..., studyId=$studyId")
//    android.util.Log.d("QUIZ_RESULT", "🟢 ResultScreen 들어옴 studyId=$studyId")
//    val scope = rememberCoroutineScope()
//
//    BackHandler { onBackClick() }
//
//    // 서버에서 불러온 퀴즈 리스트 (QuizItem)
//    val quizList by viewModel.quizList.collectAsState()
//
//    // 사용자 선택 저장용 Map(questionIndex -> 선택된 답 String)
//    val userAnswers = remember { mutableStateMapOf<Int, String>() }
//
////    // 화면 진입 시 서버에서 퀴즈 불러오기 요청 (한 번만 실행)
////    LaunchedEffect(studyId, token) {
////        android.util.Log.d("QUIZ_RESULT", "📡 loadQuizList 호출: studyId=$studyId")
////        viewModel.loadQuizList(token, studyId)
////    }
//    // ✅ 변경
////    LaunchedEffect(studyId, token) {
////        android.util.Log.d("QUIZ_RESULT", "📡 loadQuizList 호출: studyId=$studyId")
////        viewModel.loadQuizList(token, studyId)
////    }
//    LaunchedEffect(studyId) {
//        android.util.Log.d("QUIZ_RESULT", "📡 loadQuizList 호출: studyId=$studyId")
//        viewModel.loadQuizList(studyId)
//    }
//    // 퀴즈가 없으면 로딩 UI 보여줌
//    if (quizList.isEmpty()) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            CircularProgressIndicator()
//        }
//        return
//    }
//
//    // 서버에서 받은 QuizItem 리스트를 UI용 StudyResultQuestion 리스트로 변환
//    val resultQuestions = remember(quizList) {
//        quizList.map { quiz ->
//            val correctIndex = quiz.options.indexOf(quiz.answer).coerceAtLeast(0)
//            val userIndex = quiz.userChoice
//                ?.let { quiz.options.indexOf(it) }
//                ?.takeIf { it >= 0 }
//
//            StudyResultQuestion(
//                question = quiz.question,
//                choices = quiz.options,
//                correctIndex = correctIndex,
//                userAnswerIndex = userIndex,
//                explanation = quiz.explanation
//            )
//        }
//    }
//
//    // 기존 UI 컴포저블 호출 (절대 수정 없음)
//    StudyThirdResultScreen(
//        questions = resultQuestions,
//        onBackClick = onBackClick,
//        onFinishClick = onFinishClick
//    )
//
//    /**
//     * 사용자 답변 선택 시 호출 함수
//     * 서버에 정답 저장 API 호출 및 로컬 상태 업데이트
//     */
//    fun submitAnswer(localIndex: Int, selectedChoice: String) {
//        val quizItem = quizList[localIndex] // UI index → quiz 데이터
//        val serverIndex = quizItem.questionIndex // 서버에서 준 1-based 값
//
//        val isCorrect = quizItem.answer == selectedChoice
//
//        scope.launch {
//            viewModel.submitQuizAnswer(
//                studyId = studyId,
//                index = serverIndex, // 서버의 questionIndex 사용
//                userChoice = selectedChoice
//            )
//        }
//
//        userAnswers[serverIndex] = selectedChoice
//    }
////    fun submitAnswer(questionIndex: Int, selectedChoice: String) {
////        // 답변이 맞는지 확인
////        val isCorrect = quizList.find { it.questionIndex == questionIndex }?.answer == selectedChoice
////
////        // ViewModel에 정답 저장 요청
////        scope.launch {
////            viewModel.submitQuizAnswer(
////                studyId = studyId,
////                index = questionIndex,      // ⚠️ 서버의 questionIndex(1-based) 사용 권장
////                userChoice = selectedChoice
////            )
////        }
////
////        // UI용 상태에 사용자 선택 저장 (화면 갱신용)
////        userAnswers[questionIndex] = selectedChoice
////    }
//
//    // ※ UI 내부의 선택지 Surface 클릭 시 이 submitAnswer(questionIndex, choice) 함수를 호출
//    // ※ UI 변경 금지라 함수만 정의해두고, 실제 클릭 핸들러 연결은 별도 구현 필요
//}
//
///**
// * UI 코드는 절대 수정하지 않음
// */
////@Composable
////fun StudyThirdResultScreen(
////    questions: List<StudyResultQuestion>,
////    onBackClick: () -> Unit = {},
////    onFinishClick: () -> Unit = {}
////) {
////
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(Color.White)
////            .padding(horizontal = 20.dp, vertical = 48.dp)
////    ) {
////        TopBar(title = "오늘의 학습", onBackClick = onBackClick)
////
////        Spacer(modifier = Modifier.height(24.dp))
////        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
////        Spacer(modifier = Modifier.height(12.dp))
////        StepProgressBarPreview(totalSteps = 3, currentStep = 3)
////        Spacer(modifier = Modifier.height(16.dp))
////
////        LazyColumn(modifier = Modifier.weight(1f)) {
////            itemsIndexed(questions) { index, question ->
////                val isCorrect = question.correctIndex == question.userAnswerIndex
////                val resultIcon = if (isCorrect) R.drawable.ic_correct else R.drawable.ic_wrong
////
////                Box(
////                    modifier = Modifier
////                        .fillMaxWidth()
////                        .padding(vertical = 12.dp)
////                ) {
////                    // ✅ 동그라미 or 브이표 이미지 (카드 위에 뜨도록 zIndex 설정)
////                    Image(
////                        painter = painterResource(id = resultIcon),
////                        contentDescription = null,
////                        modifier = Modifier
////                            .size(100.dp)
////                            .offset(x = (-12).dp, y = (-8).dp)
////                            .zIndex(1f) // 🎯 카드보다 위로
////                    )
////
////                    // ✅ 카드 콘텐츠
////                    Column(
////                        modifier = Modifier
////                            .fillMaxWidth()
////                            .padding(top = 20.dp) // 🎯 아이콘과 겹치지 않도록 약간 내림
////                            .background(Color.White, RoundedCornerShape(12.dp))
////                            .padding(16.dp)
////                            .zIndex(0f) // 카드 아래쪽 레이어
////                    ) {
////                        Text(
////                            text = "${index + 1}/${questions.size}",
////                            fontSize = 12.sp,
////                            fontFamily = Pretendard,
////                            color = Color.Gray
////                        )
////
////                        Spacer(modifier = Modifier.height(8.dp))
////
////                        Text(
////                            text = question.question,
////                            fontSize = 18.sp,
////                            fontWeight = FontWeight.SemiBold,
////                            fontFamily = Pretendard,
////                            color = Color.Black,
////                            lineHeight = 26.sp
////                        )
////
////                        Spacer(modifier = Modifier.height(16.dp))
////
////                        question.choices.forEachIndexed { i, choice ->
////                            val isSelected = question.userAnswerIndex == i
////                            val isCorrectAnswer = question.correctIndex == i
////
////                            Surface(
////                                modifier = Modifier
////                                    .fillMaxWidth()
////                                    .padding(vertical = 4.dp),
////                                shape = RoundedCornerShape(12.dp),
////                                color = when {
////                                    isCorrectAnswer -> Color(0xFF195FCF)
////                                    isSelected -> Color(0xFFE0E0E0)
////                                    else -> Color(0xFFF7F7F7)
////                                },
////                                shadowElevation = 2.dp
////                            ) {
////                                Box(
////                                    modifier = Modifier.padding(12.dp)
////                                ) {
////                                    Text(
////                                        text = choice,
////                                        fontSize = 14.sp,
////                                        fontFamily = Pretendard,
////                                        color = if (isCorrectAnswer) Color.White else Color.Black
////                                    )
////                                }
////                            }
////                        }
////
////                        Spacer(modifier = Modifier.height(16.dp))
////
////                        Text(
////                            text = "정답",
////                            fontSize = 12.sp,
////                            fontFamily = Pretendard,
////                            color = Color.Gray
////                        )
////                        Text(
////                            text = question.choices[question.correctIndex],
////                            fontSize = 14.sp,
////                            fontFamily = Pretendard,
////                            color = Color.DarkGray
////                        )
////
////                        Spacer(modifier = Modifier.height(12.dp))
////
////                        Text(
////                            text = "해설",
////                            fontSize = 12.sp,
////                            fontFamily = Pretendard,
////                            color = Color.Gray
////                        )
////                        Text(
////                            text = question.explanation,
////                            fontSize = 14.sp,
////                            fontFamily = Pretendard,
////                            color = Color.DarkGray
////                        )
////                    }
////                }
////            }
////        }
////
////        Spacer(modifier = Modifier.height(16.dp))
////
////        // ✅ 하단 버튼 (2단계와 동일한 스타일, 텍스트만 "메인으로")
////        Row(
////            Modifier
////                .fillMaxWidth()
////                .offset(y = (-20).dp)           // 20dp 위로 올림
////                .padding(end = 20.dp),          // 🔹 오른쪽에서 24dp 여백
////            horizontalArrangement = Arrangement.SpaceBetween
////        ) {
////            Spacer(modifier = Modifier.width(150.dp)) // 왼쪽 빈칸 확보용
////
////            Button(
////                onClick = onFinishClick,
////                shape = RoundedCornerShape(50),
////                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
////                modifier = Modifier
////                    .height(42.dp)
////                    .width(160.dp)
////            ) {
////                Text(
////                    text = "다음 단계",
////                    fontSize = 16.sp,
////                    fontFamily = Pretendard,
////                    color = Color.White
////                )
////            }
////        }
////    }
////}
//
///* ---------- ⬇️ 교체: StudyThirdResultScreen (UI만 수정, 기능/연동 그대로) ---------- */
//@Composable
//fun StudyThirdResultScreen(
//    questions: List<StudyResultQuestion>,
//    onBackClick: () -> Unit = {},
//    onFinishClick: () -> Unit = {}
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(horizontal = 20.dp, vertical = 48.dp)
//    ) {
//        TopBar(title = "오늘의 학습", onBackClick = onBackClick)
//
//        Spacer(modifier = Modifier.height(24.dp))
//        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
//        Spacer(modifier = Modifier.height(12.dp))
//        StepProgressBarPreview(totalSteps = 3, currentStep = 3)
//        Spacer(modifier = Modifier.height(16.dp))
//
//        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(top = 8.dp)) {
//            itemsIndexed(questions) { index, q ->
//                val isCorrect = q.userAnswerIndex != null && q.userAnswerIndex == q.correctIndex
//                val resultIcon = if (isCorrect) R.drawable.ic_correct else R.drawable.ic_wrong
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 180.dp)
//                        //.padding(vertical = 50.dp)
//                ) {
//                    Image(
//                        painter = painterResource(id = resultIcon),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .align(Alignment.TopStart)   // ⭐ 필수!!
//                            .size(140.dp)
//                            .offset(x = (-46).dp, y = (-42).dp)
//                            .zIndex(10f)
//                    )
//                    // ① 해설 블록 (뒤쪽)
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 32.dp)  // 문제+해설 전체 단위 간격
//                    ) {
//
//                        // ⬆ 문제 카드 + 정답 아이콘
//                        Box(
//                            modifier = Modifier.fillMaxWidth()
//                        ) {
//                            // 정답/오답 아이콘
//                            Image(
//                                painter = painterResource(id = resultIcon),
//                                contentDescription = null,
//                                modifier = Modifier
//                                    .align(Alignment.TopStart)
//                                    .size(120.dp)
//                                    .offset(x = (-36).dp, y = (-28).dp)
//                                    .zIndex(10f)
//                            )
//
//                            // 문제 카드
//                            Card(
//                                shape = RoundedCornerShape(12.dp),
//                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
//                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .zIndex(1f)
//                            ) {
//                                Column(Modifier.padding(16.dp)) {
//                                    Text(
//                                        text = "${index + 1}/${questions.size}",
//                                        fontFamily = Pretendard,
//                                        fontSize = 12.sp,
//                                        fontWeight = FontWeight.Medium,
//                                        color = LabelGray
//                                    )
//                                    Spacer(Modifier.height(8.dp))
//                                    Text(
//                                        text = q.question,
//                                        fontFamily = Pretendard,
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = Color.Black,
//                                        lineHeight = 26.sp
//                                    )
//                                    Spacer(Modifier.height(16.dp))
//
//                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                                        q.choices.forEachIndexed { i, choice ->
//                                            val isCorrectAnswer = (i == q.correctIndex)
//                                            val isUserSelectedWrong = (i == q.userAnswerIndex) && (q.userAnswerIndex != q.correctIndex)
//
//                                            ChoiceRowModern(
//                                                text = choice,
//                                                isCorrectAnswer = isCorrectAnswer,
//                                                isUserSelectedWrong = isUserSelectedWrong
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//
//                        // ⬇ 문제 카드 바로 아래 해설 카드
//                        AnswerExplanationBlock(
//                            answerText = q.choices.getOrNull(q.correctIndex).orEmpty(),
//                            explanation = q.explanation
//                        )
//                    }
//
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // 하단 버튼(그대로 유지)
//        Row(
//            Modifier
//                .fillMaxWidth()
//                .offset(y = (-20).dp)
//                .padding(end = 20.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Spacer(modifier = Modifier.width(150.dp))
//
//            Button(
//                onClick = onFinishClick,
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
//                modifier = Modifier
//                    .height(42.dp)
//                    .width(160.dp)
//            ) {
//                Text(
//                    text = "다음 단계",
//                    fontSize = 16.sp,
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color.White
//                )
//            }
//        }
//    }
//}
//
//@Composable
//@Preview(
//    name = "StudyThirdResult - 기본",
//    showBackground = true,
//    backgroundColor = 0xFFFFFFFF
//)
//fun PreviewStudyThirdResultScreen_New() {
//    val mockQuestions = listOf(
//        StudyResultQuestion(
//            question = "이 글의 핵심 내용을 가장 잘 요약한 것은?",
//            choices = listOf("요약1", "요약2", "요약3", "요약4"),
//            correctIndex = 0,
//            userAnswerIndex = 1, // ❌ 오답 선택 → 빨강 표시
//            explanation = "핵심은 '요약1'이며, 글의 중심 문장과 일치합니다."
//        ),
//        StudyResultQuestion(
//            question = "두 번째 문제입니다.",
//            choices = listOf("A", "B", "C", "D"),
//            correctIndex = 2,
//            userAnswerIndex = 2, // ✅ 정답 선택 → 파랑 표시
//            explanation = "문맥상 정답은 C가 자연스럽습니다."
//        ),
//        StudyResultQuestion(
//            question = "세 번째 문제입니다.",
//            choices = listOf("ㄱ", "ㄴ", "ㄷ", "ㄹ"),
//            correctIndex = 1,
//            userAnswerIndex = null, // 미응답 → 회색 표시
//            explanation = "선택지 'ㄴ'이 지문 조건과 일치합니다."
//        )
//    )
//
//    StudyThirdResultScreen(
//        questions = mockQuestions,
//        onBackClick = {},
//        onFinishClick = {}
//    )
//}
//
//@Composable
//@Preview(
//    name = "StudyThirdResult - 긴 해설/정답",
//    showBackground = true,
//    backgroundColor = 0xFFFFFFFF
//)
//fun PreviewStudyThirdResultScreen_LongText() {
//    val longExp = "해설이 길어지는 경우에도 BgBlue 영역에서 줄바꿈과 가독성이 유지됩니다. " +
//            "중요 포인트: ① 키워드 확인 ② 문맥 재확인 ③ 오답 제거. " +
//            "추가 예시를 통해 이해를 돕습니다."
//    val mockQuestions = listOf(
//        StudyResultQuestion(
//            question = "정답/해설 카드의 레이아웃이 길어질 때도 잘 붙어 있나요?",
//            choices = listOf("항상 그렇다", "대개 그렇다", "상황에 따라 다르다", "아니다"),
//            correctIndex = 0,
//            userAnswerIndex = 3,
//            explanation = longExp
//        )
//    )
//
//    StudyThirdResultScreen(
//        questions = mockQuestions,
//        onBackClick = {},
//        onFinishClick = {}
//    )
//}
//
