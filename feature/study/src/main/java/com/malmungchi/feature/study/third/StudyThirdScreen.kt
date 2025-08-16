package com.malmungchi.feature.study.third


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.model.QuizItem
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.StudyReadingViewModel
import com.malmungchi.feature.study.second.StepProgressBarPreview
import com.malmungchi.feature.study.second.TopBar

// ✅ 간단한 문제 데이터 모델
data class StudyQuestion(
    val question: String,
    val choices: List<String>,
    val correctIndex: Int // 정답 인덱스 (API 연동 시 유연하게 처리 가능)
)
@Composable
fun StudyThirdScreen(
    //token: String,
    studyId: Int,
    text: String,
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    // ✅ ViewModel에서 가져온 퀴즈 리스트
    val quizList by viewModel.quizList.collectAsState()

    // ✅ 퀴즈 생성: 최초 진입 시 1회만
    LaunchedEffect(Unit) {
        viewModel.generateQuiz(text, studyId)  // ✅ 토큰 제거, 순서: text → studyId
    }

    // ✅ 퀴즈가 아직 안 불러와졌다면 로딩 UI
    if (quizList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // ✅ 현재 문제 인덱스 & 유저 선택 관리
    var currentIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }

    val currentQuestion = quizList[currentIndex]
    val selectedAnswer = selectedAnswers[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
                top = 32.dp      // ✅ 위는 32, 나머지는 16
            )
    ) {
        // ✅ 상단바
        TopBar(title = "오늘의 학습", onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(24.dp))
        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.height(16.dp))
        StepProgressBarPreview(totalSteps = 3, currentStep = 3)
        Spacer(modifier = Modifier.height(24.dp))

        // ✅ 전체 카드 박스
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${currentIndex + 1}/${quizList.size}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontFamily = Pretendard
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentQuestion.question,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Pretendard,
                    lineHeight = 26.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                currentQuestion.options.forEachIndexed { index, choice ->
                    val isSelected = selectedAnswer == index
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF195FCF) else Color.White,
                        shadowElevation = 2.dp,
                        onClick = {
                            selectedAnswers[currentIndex] = index
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = choice,
                                fontSize = 16.sp,
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF333333)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ✅ 하단 버튼 (다음 문제 or 다음 단계)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    if (currentIndex > 0) currentIndex--
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                modifier = Modifier.height(42.dp).width(160.dp)
            ) {
                Text("이전 문제", fontSize = 16.sp, fontFamily = Pretendard)
            }

            Button(
                onClick = {
                    val selectedIndex = selectedAnswers[currentIndex]
                    val userChoice = selectedIndex?.let { currentQuestion.options[it] }

                    // ✅ 사용자가 선택한 보기와 정답을 서버에 저장
                    if (userChoice != null) {
                        viewModel.submitQuizAnswer(
                            studyId = studyId,
                            index = currentQuestion.questionIndex,  // ✅ 서버 1-based 인덱스
                            userChoice = userChoice
                        )
                    }

                    // ✅ 다음 문제 or 마지막 문제 → 다음 단계
                    if (currentIndex < quizList.lastIndex) {
                        currentIndex++
                    } else {
                        onNextClick()
                    }
                },
                enabled = selectedAnswer != null,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                modifier = Modifier.height(42.dp).width(160.dp)
            ) {
                Text(
                    text = if (currentIndex < quizList.lastIndex) "다음 문제" else "다음 단계",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStudyThirdScreen() {
    val dummyQuestions = listOf(
        QuizItem(
            questionIndex = 1,
            question = "이 글의 핵심 내용을 가장 잘 요약한 것은?",
            options = listOf(
                "말뭉치는 어휘력과 문해력 향상에 도움을 준다",
                "말뭉치는 뭉치와 말치라는 캐릭터가 있으며, 사용자는 치치라고 부른다",
                "디지털미디어학과의 4학년 하이라이트는 캡스톤이다",
                "우리 팀의 캡스톤은 비행기 타고 날아가는 중이다"
            ),
            answer = "말뭉치는 어휘력과 문해력 향상에 도움을 준다",
            explanation = "글의 전반적인 목적이 문해력 향상임"
        ),
        QuizItem(
            questionIndex = 2,
            question = "두 번째 문제 예시입니다",
            options = listOf("보기1", "보기2", "보기3", "보기4"),
            answer = "보기2",
            explanation = "정답은 보기2입니다"
        ),
        QuizItem(
            questionIndex = 3,
            question = "세 번째 문제 예시입니다",
            options = listOf("A", "B", "C", "D"),
            answer = "C",
            explanation = "정답은 C입니다"
        )
    )


}
//
//@Composable
//fun StudyThirdScreen(
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    val questions = remember {
//        listOf(
//            StudyQuestion(
//                question = "이 글의 핵심 내용을 가장 잘 요약한 것은?",
//                choices = listOf(
//                    "말뭉치는 어휘력과 문해력 향상에 도움을 준다",
//                    "말뭉치는 뭉치와 말치라는 캐릭터가 있으며, 사용자는 치치라고 부른다",
//                    "디지털미디어학과의 4학년 하이라이트는 캡스톤이다",
//                    "우리 팀의 캡스톤은 비행기 타고 날아가는 중이다"
//                ),
//                correctIndex = 0
//            ),
//            StudyQuestion("두 번째 문제 예시입니다", listOf("1", "2", "3", "4"), 2),
//            StudyQuestion("세 번째 문제 예시입니다", listOf("A", "B", "C", "D"), 3)
//        )
//    }
//
//    var currentIndex by remember { mutableStateOf(0) }
//    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
//
//    val currentQuestion = questions[currentIndex]
//    val selectedAnswer = selectedAnswers[currentIndex]
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        // ✅ 상단바
//        TopBar(title = "오늘의 학습", onBackClick = onBackClick)
//
//        Spacer(modifier = Modifier.height(24.dp))
//        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
//        Spacer(modifier = Modifier.height(16.dp))
//        StepProgressBarPreview(totalSteps = 3, currentStep = 3)
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // ✅ 전체 카드 박스
//        Surface(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 4.dp),
//            shape = RoundedCornerShape(12.dp),
//            color = Color.White,
//            shadowElevation = 6.dp // ✅ 그림자만 있음
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(
//                    text = "${currentIndex + 1}/${questions.size}",
//                    fontSize = 12.sp,
//                    color = Color.Gray,
//                    fontFamily = Pretendard
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(
//                    text = currentQuestion.question,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    fontFamily = Pretendard,
//                    lineHeight = 26.sp,
//                    color = Color.Black
//                )
//                Spacer(modifier = Modifier.height(16.dp))
//
//                currentQuestion.choices.forEachIndexed { index, choice ->
//                    val isSelected = selectedAnswer == index
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(vertical = 6.dp),
//                        shape = RoundedCornerShape(12.dp),
//                        color = if (isSelected) Color(0xFF195FCF) else Color.White,
//                        shadowElevation = 2.dp,
//                        onClick = {
//                            selectedAnswers[currentIndex] = index
//                        }
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            contentAlignment = Alignment.CenterStart
//                        ) {
//                            Text(
//                                text = choice,
//                                fontSize = 16.sp,
//                                fontFamily = Pretendard,
//                                fontWeight = FontWeight.Medium,
//                                color = if (isSelected) Color.White else Color(0xFF333333)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(32.dp))
//
//        // ✅ 하단 버튼 (2단계와 동일한 위치/스타일)
//        Row(
//            Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            OutlinedButton(
//                onClick = {
//                    if (currentIndex > 0) currentIndex--
//                },
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("이전 문제", fontSize = 16.sp, fontFamily = Pretendard)
//            }
//
//            Button(
//                onClick = {
//                    if (currentIndex < questions.lastIndex) {
//                        currentIndex++
//                    } else {
//                        onNextClick() // 마지막 문제 → 다음 단계
//                    }
//                },
//                enabled = selectedAnswer != null,
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text(
//                    text = if (currentIndex < questions.lastIndex) "다음 문제" else "다음 단계",
//                    fontSize = 16.sp,
//                    fontFamily = Pretendard,
//                    color = Color.White
//                )
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewStudyThirdScreen() {
//    StudyThirdScreen()
//}
