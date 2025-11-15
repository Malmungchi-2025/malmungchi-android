package com.malmungchi.feature.study.third


import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    studyId: Int,
    text: String,
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    // ✅ ViewModel에서 가져온 퀴즈 리스트
    val quizList by viewModel.quizList.collectAsState()

    // ✅ 최초 진입 시 1회 호출
    LaunchedEffect(studyId) {
        if (viewModel.quizList.value.isEmpty()) {
            viewModel.generateQuiz(text, studyId)
        }
    }
    //LaunchedEffect(Unit) { viewModel.generateQuiz(text, studyId) }

    // ✅ 로딩 플래그
    val isLoading = quizList.isEmpty()

    // ✅ 현재 문제 인덱스 & 선택값 (로딩 때도 state는 유지)
    var currentIndex by rememberSaveable { mutableStateOf(0) }
    //var currentIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }

    val currentQuestion = if (!isLoading) quizList[currentIndex] else null
    val selectedAnswer = if (!isLoading) selectedAnswers[currentIndex] else null

    // ✅ 시스템 백버튼도 동일하게 동작
    BackHandler { onBackClick() }

    // ✅ 세컨드처럼 Box로 감싸고, 버튼은 하단 고정
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            //.background(Color.White)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 48.dp)
    ) {
        // ✅ 상단바는 항상 보이게
        TopBar(title = "오늘의 학습", onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(24.dp))
        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, modifier = Modifier.padding(start = 8.dp))
        Spacer(modifier = Modifier.height(16.dp))
        StepProgressBarPreview(totalSteps = 3, currentStep = 3)
        Spacer(modifier = Modifier.height(24.dp))

        // ✅ 카드: 로딩 중이면 인디케이터만
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF7F7F7),
            shadowElevation = 2.dp
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp) // 로딩 시에도 레이아웃 안정
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF195FCF))
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${currentIndex + 1}/${quizList.size}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = Pretendard
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentQuestion!!.question,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        lineHeight = 26.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // ✅ 선택지 스타일 수정
                    currentQuestion.options.forEachIndexed { index, choice ->
                        val isSelected = selectedAnswer == index
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(0xFFEFF4FB) else Color.White,
                            border = if (isSelected)
                                BorderStroke(1.dp, Color(0xFF195FCF))
                            else
                                BorderStroke(1.dp, Color.Transparent),
                            shadowElevation = 1.dp,
                            onClick = { selectedAnswers[currentIndex] = index }
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
                                    color = Color(0xFF333333) // ✅ 항상 검정색
                                )
                            }
                        }
                    }
                }
            }
        }
    }

        Spacer(modifier = Modifier.height(32.dp))

        // ── ✅ 하단 고정 버튼: 세컨드와 동일한 위치/UI ──
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)          // 세컨드와 동일
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 64.dp),
                //.offset(y = (-64).dp),// 세컨드와 동일
            horizontalArrangement = Arrangement.spacedBy(12.dp) // 세컨드와 동일
        ) {
            OutlinedButton(
                onClick = { if (!isLoading && currentIndex > 0) currentIndex-- },
                enabled = !isLoading && currentIndex > 0,      // 동작만 조건, UI는 동일
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                border = BorderStroke(1.dp, Color(0xFF195FCF)),
                modifier = Modifier
                    .height(42.dp)   // 세컨드와 동일
                    .weight(1f)      // 세컨드와 동일
            ) {
                Text("이전 문제", fontSize = 16.sp, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    if (isLoading) return@Button
                    val selectedIndex = selectedAnswers[currentIndex]
                    val userChoice = selectedIndex?.let { currentQuestion!!.options[it] }
                    if (userChoice != null) {
                        viewModel.submitQuizAnswer(
                            studyId = studyId,
                            index = currentQuestion!!.questionIndex,
                            userChoice = userChoice
                        )
                    }
                    if (currentIndex < quizList.lastIndex) {
                        currentIndex++
                    } else {
                        onNextClick()
                    }
                },
                enabled = !isLoading && selectedAnswer != null, // 동작만 조건, UI는 동일
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .height(42.dp)   // 세컨드와 동일
                    .weight(1f)      // 세컨드와 동일
            ) {
                Text(
                    text = if (!isLoading && currentIndex < (quizList.size - 1)) "다음 문제" else "다음 단계",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
    }
//@Composable
//fun StudyThirdScreen(
//    //token: String,
//    studyId: Int,
//    text: String,
//    viewModel: StudyReadingViewModel = hiltViewModel(),
//    onBackClick: () -> Unit = {},
//    onNextClick: () -> Unit = {}
//) {
//    // ✅ ViewModel에서 가져온 퀴즈 리스트
//    val quizList by viewModel.quizList.collectAsState()
//
//    // ✅ 퀴즈 생성: 최초 진입 시 1회만
//    LaunchedEffect(Unit) {
//        viewModel.generateQuiz(text, studyId)  // ✅ 토큰 제거, 순서: text → studyId
//    }
//    // ✅ 로딩 여부
//    val isLoading = quizList.isEmpty()
//
//
//
//    // ✅ 퀴즈가 아직 안 불러와졌다면 로딩 UI
//    if (quizList.isEmpty()) {
//        Box(
//            modifier = Modifier.fillMaxSize(),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator()
//        }
//        return
//    }
//
//    // ✅ 현재 문제 인덱스 & 유저 선택 관리
//    var currentIndex by remember { mutableStateOf(0) }
//    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }
//
//    val currentQuestion = quizList[currentIndex]
//    val selectedAnswer = selectedAnswers[currentIndex]
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(
//                start = 16.dp,
//                end = 16.dp,
//                bottom = 16.dp,
//                top = 32.dp      // ✅ 위는 32, 나머지는 16
//            )
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
//            shadowElevation = 6.dp
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(
//                    text = "${currentIndex + 1}/${quizList.size}",
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
//                currentQuestion.options.forEachIndexed { index, choice ->
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
//        // ✅ 하단 버튼 (다음 문제 or 다음 단계)
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
//                    val selectedIndex = selectedAnswers[currentIndex]
//                    val userChoice = selectedIndex?.let { currentQuestion.options[it] }
//
//                    // ✅ 사용자가 선택한 보기와 정답을 서버에 저장
//                    if (userChoice != null) {
//                        viewModel.submitQuizAnswer(
//                            studyId = studyId,
//                            index = currentQuestion.questionIndex,  // ✅ 서버 1-based 인덱스
//                            userChoice = userChoice
//                        )
//                    }
//
//                    // ✅ 다음 문제 or 마지막 문제 → 다음 단계
//                    if (currentIndex < quizList.lastIndex) {
//                        currentIndex++
//                    } else {
//                        onNextClick()
//                    }
//                },
//                enabled = selectedAnswer != null,
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text(
//                    text = if (currentIndex < quizList.lastIndex) "다음 문제" else "다음 단계",
//                    fontSize = 16.sp,
//                    fontFamily = Pretendard,
//                    color = Color.White
//                )
//            }
//        }
//    }
//}

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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 800)
@Composable
fun Preview_StudyThirdScreen_UI() {
    // ✅ 더미 데이터
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
        )
    )

    // ✅ 상태 시뮬레이션
    var currentIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }

    val currentQuestion = dummyQuestions[currentIndex]
    val selectedAnswer = selectedAnswers[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 48.dp)
        ) {
            TopBar(title = "오늘의 학습", onBackClick = {})
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "학습 진행률",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            StepProgressBarPreview(totalSteps = 3, currentStep = 3)
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF7F7F7),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${currentIndex + 1}/${dummyQuestions.size}",
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
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF195FCF) else Color.White,
                            shadowElevation = 2.dp,
                            onClick = { selectedAnswers[currentIndex] = index }
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
        }

        // ✅ 하단 버튼 (세컨드 화면과 동일하게 64dp 위로)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-64).dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { if (currentIndex > 0) currentIndex-- },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                border = BorderStroke(1.dp, Color(0xFF195FCF)),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) {
                Text("이전 문제", fontSize = 16.sp, fontFamily = Pretendard)
            }

            Button(
                onClick = {
                    if (currentIndex < dummyQuestions.lastIndex) {
                        currentIndex++
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) {
                Text(
                    text = if (currentIndex < dummyQuestions.lastIndex) "다음 문제" else "다음 단계",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 800)
@Composable
fun Preview_StudyThirdScreen_Selected() {
    // ✅ 더미 데이터
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
        )
    )

    var currentIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<Int, Int>() }

    // ✅ 미리 선택된 상태 (0번 보기 선택)
    selectedAnswers[currentIndex] = 0

    val currentQuestion = dummyQuestions[currentIndex]
    val selectedAnswer = selectedAnswers[currentIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 48.dp)
        ) {
            TopBar(title = "오늘의 학습", onBackClick = {})
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "학습 진행률",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            StepProgressBarPreview(totalSteps = 3, currentStep = 3)
            Spacer(modifier = Modifier.height(24.dp))

            // ✅ 문제 카드
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF7F7F7),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${currentIndex + 1}/${dummyQuestions.size}",
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

                    // ✅ 보기 표시 (수정된 부분)
                    currentQuestion.options.forEachIndexed { index, choice ->
                        val isSelected = selectedAnswer == index
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFEFF4FB) else Color.White, // ✅ 연파랑 배경
                            border = if (isSelected)
                                BorderStroke(1.dp, Color(0xFF195FCF)) // ✅ 파랑 테두리
                            else
                                BorderStroke(1.dp, Color.Transparent),
                            shadowElevation = 1.dp
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
                                    color = Color(0xFF333333) // ✅ 항상 검정
                                )
                            }
                        }
                    }
                }
            }
        }

        // ✅ 하단 버튼
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-64).dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { if (currentIndex > 0) currentIndex-- },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                border = BorderStroke(1.dp, Color(0xFF195FCF)),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) {
                Text("이전 문제", fontSize = 16.sp, fontFamily = Pretendard)
            }

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) {
                Text("다음 문제", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
            }
        }
    }
}