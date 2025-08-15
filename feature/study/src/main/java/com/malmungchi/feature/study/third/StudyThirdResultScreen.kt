package com.malmungchi.feature.study.third


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
            .padding(horizontal = 16.dp)
    ) {
        TopBar(title = "오늘의 학습", onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(24.dp))
        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.height(12.dp))
        StepProgressBarPreview(totalSteps = 3, currentStep = 3)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(questions) { index, question ->
                val isCorrect = question.correctIndex == question.userAnswerIndex
                val resultIcon = if (isCorrect) R.drawable.ic_correct else R.drawable.ic_wrong

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    // ✅ 동그라미 or 브이표 이미지 (카드 위에 뜨도록 zIndex 설정)
                    Image(
                        painter = painterResource(id = resultIcon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-12).dp, y = (-8).dp)
                            .zIndex(1f) // 🎯 카드보다 위로
                    )

                    // ✅ 카드 콘텐츠
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp) // 🎯 아이콘과 겹치지 않도록 약간 내림
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                            .zIndex(0f) // 카드 아래쪽 레이어
                    ) {
                        Text(
                            text = "${index + 1}/${questions.size}",
                            fontSize = 12.sp,
                            fontFamily = Pretendard,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = question.question,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Pretendard,
                            color = Color.Black,
                            lineHeight = 26.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        question.choices.forEachIndexed { i, choice ->
                            val isSelected = question.userAnswerIndex == i
                            val isCorrectAnswer = question.correctIndex == i

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    isCorrectAnswer -> Color(0xFF195FCF)
                                    isSelected -> Color(0xFFE0E0E0)
                                    else -> Color(0xFFF7F7F7)
                                },
                                shadowElevation = 2.dp
                            ) {
                                Box(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = choice,
                                        fontSize = 14.sp,
                                        fontFamily = Pretendard,
                                        color = if (isCorrectAnswer) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "정답",
                            fontSize = 12.sp,
                            fontFamily = Pretendard,
                            color = Color.Gray
                        )
                        Text(
                            text = question.choices[question.correctIndex],
                            fontSize = 14.sp,
                            fontFamily = Pretendard,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "해설",
                            fontSize = 12.sp,
                            fontFamily = Pretendard,
                            color = Color.Gray
                        )
                        Text(
                            text = question.explanation,
                            fontSize = 14.sp,
                            fontFamily = Pretendard,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ 하단 버튼 (2단계와 동일한 스타일, 텍스트만 "메인으로")
        Row(
            Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp)           // 20dp 위로 올림
                .padding(end = 24.dp),          // 🔹 오른쪽에서 24dp 여백
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.width(150.dp)) // 왼쪽 빈칸 확보용

            Button(
                onClick = onFinishClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                modifier = Modifier
                    .height(42.dp)
                    .width(160.dp)
            ) {
                Text(
                    text = "다음 단계",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewStudyThirdResultScreen() {
    val mockQuestions = listOf(
        StudyResultQuestion(
            question = "이 글의 핵심 내용을 가장 잘 요약한 것은?",
            choices = listOf("요약1", "요약2", "요약3", "요약4"),
            correctIndex = 0,
            userAnswerIndex = 0,
            explanation = "이 글은 어휘력 향상에 관한 글입니다."
        ),
        StudyResultQuestion(
            question = "두 번째 문제입니다.",
            choices = listOf("A", "B", "C", "D"),
            correctIndex = 2,
            userAnswerIndex = 1,
            explanation = "B가 아닌 C가 적절한 이유는 ~~ 때문입니다."
        ),
        StudyResultQuestion(
            question = "세 번째 문제입니다.",
            choices = listOf("ㄱ", "ㄴ", "ㄷ", "ㄹ"),
            correctIndex = 1,
            userAnswerIndex = null,
            explanation = "ㄴ을 고르는 것이 적절합니다."
        )
    )

    StudyThirdResultScreen(questions = mockQuestions)
}



//// 결과용 문제 모델
//data class StudyResultQuestion(
//    val question: String,
//    val choices: List<String>,
//    val correctIndex: Int,
//    val userAnswerIndex: Int?,
//    val explanation: String
//)
//
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
//            .padding(horizontal = 16.dp)
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
//            Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Spacer(modifier = Modifier.width(160.dp)) // 왼쪽 빈칸 확보용
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
//
//@Composable
//@Preview(showBackground = true)
//fun PreviewStudyThirdResultScreen() {
//    val mockQuestions = listOf(
//        StudyResultQuestion(
//            question = "이 글의 핵심 내용을 가장 잘 요약한 것은?",
//            choices = listOf("요약1", "요약2", "요약3", "요약4"),
//            correctIndex = 0,
//            userAnswerIndex = 0,
//            explanation = "이 글은 어휘력 향상에 관한 글입니다."
//        ),
//        StudyResultQuestion(
//            question = "두 번째 문제입니다.",
//            choices = listOf("A", "B", "C", "D"),
//            correctIndex = 2,
//            userAnswerIndex = 1,
//            explanation = "B가 아닌 C가 적절한 이유는 ~~ 때문입니다."
//        ),
//        StudyResultQuestion(
//            question = "세 번째 문제입니다.",
//            choices = listOf("ㄱ", "ㄴ", "ㄷ", "ㄹ"),
//            correctIndex = 1,
//            userAnswerIndex = null,
//            explanation = "ㄴ을 고르는 것이 적절합니다."
//        )
//    )
//
//    StudyThirdResultScreen(questions = mockQuestions)
//}