package com.malmungchi.feature.study.third


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch


// 결과용 문제 모델, UI 그대로 유지
data class StudyResultQuestion(
    val question: String,
    val choices: List<String>,
    val correctIndex: Int,
    val userAnswerIndex: Int?,
    val explanation: String
)
/* ---------- 팔레트 (QuizRetry 스타일과 동일) ---------- */
private val BrandBlue   = Color(0xFF195FCF)
private val BgBlue      = Color(0xFFEFF4FB)
private val CorrectFill = Color(0xFFD1DFF5) // 정답 내부색
private val WrongRed    = Color(0xFFFF0000) // 오답 테두리
private val WrongFill   = Color(0xFFFFCCCC) // 오답 내부색
private val ChipGray    = Color(0xFFFFFFFF)
private val LabelGray   = Color(0xFF616161)

/* ---------- 옵션 렌더 (4지선다: 정답 파랑 테두리/연한 파랑 배경, 오답 선택 빨강) ---------- */
@Composable
private fun ChoiceRowModern(
    text: String,
    isCorrectAnswer: Boolean,
    isUserSelectedWrong: Boolean
) {
    val bgColor = when {
        isCorrectAnswer     -> CorrectFill
        isUserSelectedWrong -> WrongFill
        else                -> Color(0xFFFFFFFF) // 기본 옵션 배경 흰색
        //else                -> ChipGray
    }
    val borderColor = when {
        isCorrectAnswer     -> BrandBlue
        isUserSelectedWrong -> WrongRed
        else                -> Color(0xFFE0E0E0)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp) // 옵션 높이
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),  // 좌우 패딩만 유지
            contentAlignment = Alignment.CenterStart  // ← 텍스트 정확히 가운데 정렬
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                lineHeight = 24.sp   // ← 줄 간격 150%
            )
        }
    }
}

/* ---------- 정답/해설 정보 블록 (BgBlue, 좌우 6dp 패딩, 라벨/값 스타일) ---------- */
@Composable
private fun AnswerExplanationBlock(
    answerText: String,
    explanation: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = 0.dp, topEnd = 0.dp,
                    bottomStart = 12.dp, bottomEnd = 12.dp
                )
            )
            .background(BgBlue)
    ) {
        Column(
            modifier = Modifier
                .padding(top = 56.dp, bottom = 24.dp) // 세로 여백
                .padding(start = 20.dp, end = 12.dp)   // ▶ 좌우 6dp (좌측 치우침 방지)
        ) {
            Text(
                text = "정답",
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = LabelGray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = answerText,
                fontFamily = Pretendard,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "해설",
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = LabelGray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = explanation,
                fontFamily = Pretendard,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

/**
 * API 연동을 위한 Wrapper Composable
 * UI 코드는 절대 건들지 않고, 서버에서 퀴즈 불러오고
 * 사용자의 정답을 ViewModel 통해 서버에 저장하는 로직만 추가
 */
@Composable
fun StudyThirdResultScreenWrapper(
    //token: String,
    studyId: Int,
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onFinishClick: () -> Unit = {}
) {
    //android.util.Log.d("QUIZ_RESULT", "🟢 ResultScreen 들어옴 token=${token.take(8)}..., studyId=$studyId")
    android.util.Log.d("QUIZ_RESULT", "🟢 ResultScreen 들어옴 studyId=$studyId")
    val scope = rememberCoroutineScope()

    BackHandler { onBackClick() }

    // 서버에서 불러온 퀴즈 리스트 (QuizItem)
    val quizList by viewModel.quizList.collectAsState()

    // 사용자 선택 저장용 Map(questionIndex -> 선택된 답 String)
    val userAnswers = remember { mutableStateMapOf<Int, String>() }

//    // 화면 진입 시 서버에서 퀴즈 불러오기 요청 (한 번만 실행)
//    LaunchedEffect(studyId, token) {
//        android.util.Log.d("QUIZ_RESULT", "📡 loadQuizList 호출: studyId=$studyId")
//        viewModel.loadQuizList(token, studyId)
//    }
    // ✅ 변경
//    LaunchedEffect(studyId, token) {
//        android.util.Log.d("QUIZ_RESULT", "📡 loadQuizList 호출: studyId=$studyId")
//        viewModel.loadQuizList(token, studyId)
//    }
    LaunchedEffect(studyId) {
        android.util.Log.d("QUIZ_RESULT", "📡 loadQuizList 호출: studyId=$studyId")
        viewModel.loadQuizList(studyId)
    }
    // 퀴즈가 없으면 로딩 UI 보여줌
    if (quizList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // 서버에서 받은 QuizItem 리스트를 UI용 StudyResultQuestion 리스트로 변환
    val resultQuestions = remember(quizList) {
        quizList.map { quiz ->
            val correctIndex = quiz.options.indexOf(quiz.answer).coerceAtLeast(0)
            val userIndex = quiz.userChoice
                ?.let { quiz.options.indexOf(it) }
                ?.takeIf { it >= 0 }

            StudyResultQuestion(
                question = quiz.question,
                choices = quiz.options,
                correctIndex = correctIndex,
                userAnswerIndex = userIndex,
                explanation = quiz.explanation
            )
        }
    }

    // 기존 UI 컴포저블 호출 (절대 수정 없음)
    StudyThirdResultScreen(
        questions = resultQuestions,
        onBackClick = onBackClick,
        onFinishClick = onFinishClick
    )

    /**
     * 사용자 답변 선택 시 호출 함수
     * 서버에 정답 저장 API 호출 및 로컬 상태 업데이트
     */
    fun submitAnswer(localIndex: Int, selectedChoice: String) {
        val quizItem = quizList[localIndex] // UI index → quiz 데이터
        val serverIndex = quizItem.questionIndex // 서버에서 준 1-based 값

        val isCorrect = quizItem.answer == selectedChoice

        scope.launch {
            viewModel.submitQuizAnswer(
                studyId = studyId,
                index = serverIndex, // 서버의 questionIndex 사용
                userChoice = selectedChoice
            )
        }

        userAnswers[serverIndex] = selectedChoice
    }
//    fun submitAnswer(questionIndex: Int, selectedChoice: String) {
//        // 답변이 맞는지 확인
//        val isCorrect = quizList.find { it.questionIndex == questionIndex }?.answer == selectedChoice
//
//        // ViewModel에 정답 저장 요청
//        scope.launch {
//            viewModel.submitQuizAnswer(
//                studyId = studyId,
//                index = questionIndex,      // ⚠️ 서버의 questionIndex(1-based) 사용 권장
//                userChoice = selectedChoice
//            )
//        }
//
//        // UI용 상태에 사용자 선택 저장 (화면 갱신용)
//        userAnswers[questionIndex] = selectedChoice
//    }

    // ※ UI 내부의 선택지 Surface 클릭 시 이 submitAnswer(questionIndex, choice) 함수를 호출
    // ※ UI 변경 금지라 함수만 정의해두고, 실제 클릭 핸들러 연결은 별도 구현 필요
}

/**
 * UI 코드는 절대 수정하지 않음
 */
//@Composable
//fun StudyThirdResultScreen(
//    questions: List<StudyResultQuestion>,
//    onBackClick: () -> Unit = {},
//    onFinishClick: () -> Unit = {}
//) {
//
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
//        LazyColumn(modifier = Modifier.weight(1f)) {
//            itemsIndexed(questions) { index, question ->
//                val isCorrect = question.correctIndex == question.userAnswerIndex
//                val resultIcon = if (isCorrect) R.drawable.ic_correct else R.drawable.ic_wrong
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 12.dp)
//                ) {
//                    // ✅ 동그라미 or 브이표 이미지 (카드 위에 뜨도록 zIndex 설정)
//                    Image(
//                        painter = painterResource(id = resultIcon),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .size(100.dp)
//                            .offset(x = (-12).dp, y = (-8).dp)
//                            .zIndex(1f) // 🎯 카드보다 위로
//                    )
//
//                    // ✅ 카드 콘텐츠
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 20.dp) // 🎯 아이콘과 겹치지 않도록 약간 내림
//                            .background(Color.White, RoundedCornerShape(12.dp))
//                            .padding(16.dp)
//                            .zIndex(0f) // 카드 아래쪽 레이어
//                    ) {
//                        Text(
//                            text = "${index + 1}/${questions.size}",
//                            fontSize = 12.sp,
//                            fontFamily = Pretendard,
//                            color = Color.Gray
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        Text(
//                            text = question.question,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            fontFamily = Pretendard,
//                            color = Color.Black,
//                            lineHeight = 26.sp
//                        )
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        question.choices.forEachIndexed { i, choice ->
//                            val isSelected = question.userAnswerIndex == i
//                            val isCorrectAnswer = question.correctIndex == i
//
//                            Surface(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .padding(vertical = 4.dp),
//                                shape = RoundedCornerShape(12.dp),
//                                color = when {
//                                    isCorrectAnswer -> Color(0xFF195FCF)
//                                    isSelected -> Color(0xFFE0E0E0)
//                                    else -> Color(0xFFF7F7F7)
//                                },
//                                shadowElevation = 2.dp
//                            ) {
//                                Box(
//                                    modifier = Modifier.padding(12.dp)
//                                ) {
//                                    Text(
//                                        text = choice,
//                                        fontSize = 14.sp,
//                                        fontFamily = Pretendard,
//                                        color = if (isCorrectAnswer) Color.White else Color.Black
//                                    )
//                                }
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(16.dp))
//
//                        Text(
//                            text = "정답",
//                            fontSize = 12.sp,
//                            fontFamily = Pretendard,
//                            color = Color.Gray
//                        )
//                        Text(
//                            text = question.choices[question.correctIndex],
//                            fontSize = 14.sp,
//                            fontFamily = Pretendard,
//                            color = Color.DarkGray
//                        )
//
//                        Spacer(modifier = Modifier.height(12.dp))
//
//                        Text(
//                            text = "해설",
//                            fontSize = 12.sp,
//                            fontFamily = Pretendard,
//                            color = Color.Gray
//                        )
//                        Text(
//                            text = question.explanation,
//                            fontSize = 14.sp,
//                            fontFamily = Pretendard,
//                            color = Color.DarkGray
//                        )
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // ✅ 하단 버튼 (2단계와 동일한 스타일, 텍스트만 "메인으로")
//        Row(
//            Modifier
//                .fillMaxWidth()
//                .offset(y = (-20).dp)           // 20dp 위로 올림
//                .padding(end = 20.dp),          // 🔹 오른쪽에서 24dp 여백
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Spacer(modifier = Modifier.width(150.dp)) // 왼쪽 빈칸 확보용
//
//            Button(
//                onClick = onFinishClick,
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                modifier = Modifier
//                    .height(42.dp)
//                    .width(160.dp)
//            ) {
//                Text(
//                    text = "다음 단계",
//                    fontSize = 16.sp,
//                    fontFamily = Pretendard,
//                    color = Color.White
//                )
//            }
//        }
//    }
//}

/* ---------- ⬇️ 교체: StudyThirdResultScreen (UI만 수정, 기능/연동 그대로) ---------- */
@Composable
fun StudyThirdResultScreen(
    questions: List<StudyResultQuestion>,
    onBackClick: () -> Unit = {},
    onFinishClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 48.dp)
    ) {
        TopBar(title = "오늘의 학습", onBackClick = onBackClick)

        Spacer(Modifier.height(24.dp))
        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.height(12.dp))
        StepProgressBarPreview(totalSteps = 3, currentStep = 3)
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            itemsIndexed(questions) { index, q ->
                val isCorrect = q.userAnswerIndex == q.correctIndex
                val iconRes = if (isCorrect) R.drawable.ic_correct else R.drawable.ic_wrong

                Box(Modifier.fillMaxWidth()) {

                    Column(Modifier.fillMaxWidth()) {

                        /* ---------- 문제 카드 ---------- */
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(1f)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = "${index + 1}/${questions.size}",
                                    fontFamily = Pretendard,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LabelGray
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = q.question,
                                    fontFamily = Pretendard,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black,
                                    lineHeight = 26.sp
                                )
                                Spacer(Modifier.height(16.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    q.choices.forEachIndexed { i, choice ->
                                        ChoiceRowModern(
                                            text = choice,
                                            isCorrectAnswer = i == q.correctIndex,
                                            isUserSelectedWrong =
                                                (i == q.userAnswerIndex && q.userAnswerIndex != q.correctIndex)
                                        )
                                    }
                                }
                            }
                        }

                        /* ---------- 정답 / 해설 (QuizRetry와 동일) ---------- */
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .zIndex(0f)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 0.dp, topEnd = 0.dp,
                                        bottomStart = 12.dp, bottomEnd = 12.dp
                                    )
                                )
                                .background(BgBlue)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 12.dp, bottom = 24.dp)
                                    .padding(start = 20.dp, end = 12.dp)
                            ) {
                                Text(
                                    text = "정답",
                                    fontFamily = Pretendard,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LabelGray
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = q.choices[q.correctIndex],
                                    fontFamily = Pretendard,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )

                                Spacer(Modifier.height(12.dp))

                                Text(
                                    text = "해설",
                                    fontFamily = Pretendard,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = LabelGray
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = q.explanation,
                                    fontFamily = Pretendard,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    /* ---------- 결과 아이콘 ---------- */
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (-12).dp, y = (-12).dp)
                            .size(100.dp)
                            .zIndex(2f)
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onFinishClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                modifier = Modifier.height(42.dp).width(160.dp)
            ) {
                Text(
                    text = "다음 단계",
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}
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
//                    //.padding(vertical = 50.dp)
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
//                    Box(
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .offset(
//                                y = run {
//                                    val isPrevExplanationLong =
//                                        if (index > 0) questions[index - 1].explanation.length > 40 else false
//
//                                    // aseY는 반드시 run 블록 안에서 선언해야 한다
//                                    val baseY = if (isPrevExplanationLong) 172.dp else 152.dp
//
//                                    // 두 번째 문제만 18dp 당김
//                                    if (index == 1) {
//                                        baseY - 10.dp
//                                    } else {
//                                        baseY
//                                    }
//                                }
//                            )
//                            .zIndex(0f)
//                    ) {
//                        AnswerExplanationBlock(
//                            answerText = q.choices.getOrNull(q.correctIndex).orEmpty(),
//                            explanation = q.explanation
//                        )
//                    }
//
//                    // ② 문제 카드 (앞쪽)
//                    Card(
//                        shape = RoundedCornerShape(12.dp),
//                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
//                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .align(Alignment.TopCenter)
//                            .zIndex(1f)       // 해설 위로
//                            .offset(y = 12.dp) // 시각적으로 맞닿게 아래로 살짝
//                    ) {
//                        Column(Modifier.padding(16.dp)) {
//                            Text(
//                                text = "${index + 1}/${questions.size}",
//                                fontFamily = Pretendard,
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = LabelGray
//                            )
//                            Spacer(Modifier.height(8.dp))
//                            Text(
//                                text = q.question,
//                                fontFamily = Pretendard,
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.Black,
//                                lineHeight = 26.sp
//                            )
//                            Spacer(Modifier.height(16.dp))
//                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                                q.choices.forEachIndexed { i, choice ->
//                                    val isCorrectAnswer = (i == q.correctIndex)
//                                    val isUserSelectedWrong =
//                                        (i == q.userAnswerIndex) && (q.userAnswerIndex != q.correctIndex)
//                                    ChoiceRowModern(
//                                        text = choice,
//                                        isCorrectAnswer = isCorrectAnswer,
//                                        isUserSelectedWrong = isUserSelectedWrong
//                                    )
//                                }
//                            }
//                        }
//                    }
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
            userAnswerIndex = 1, // ❌ 오답 선택 → 빨강 표시
            explanation = "핵심은 '요약1'이며, 글의 중심 문장과 일치합니다."
        ),
        StudyResultQuestion(
            question = "두 번째 문제입니다.",
            choices = listOf("A", "B", "C", "D"),
            correctIndex = 2,
            userAnswerIndex = 2, // ✅ 정답 선택 → 파랑 표시
            explanation = "문맥상 정답은 C가 자연스럽습니다."
        ),
        StudyResultQuestion(
            question = "세 번째 문제입니다.",
            choices = listOf("ㄱ", "ㄴ", "ㄷ", "ㄹ"),
            correctIndex = 1,
            userAnswerIndex = null, // 미응답 → 회색 표시
            explanation = "선택지 'ㄴ'이 지문 조건과 일치합니다."
        )
    )

    StudyThirdResultScreen(
        questions = mockQuestions,
        onBackClick = {},
        onFinishClick = {}
    )
}

@Composable
@Preview(
    name = "StudyThirdResult - 긴 해설/정답",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
fun PreviewStudyThirdResultScreen_LongText() {
    val longExp = "해설이 길어지는 경우에도 BgBlue 영역에서 줄바꿈과 가독성이 유지됩니다. " +
            "중요 포인트: ① 키워드 확인 ② 문맥 재확인 ③ 오답 제거. " +
            "추가 예시를 통해 이해를 돕습니다."
    val mockQuestions = listOf(
        StudyResultQuestion(
            question = "정답/해설 카드의 레이아웃이 길어질 때도 잘 붙어 있나요?",
            choices = listOf("항상 그렇다", "대개 그렇다", "상황에 따라 다르다", "아니다"),
            correctIndex = 0,
            userAnswerIndex = 3,
            explanation = longExp
        )
    )

    StudyThirdResultScreen(
        questions = mockQuestions,
        onBackClick = {},
        onFinishClick = {}
    )
}

