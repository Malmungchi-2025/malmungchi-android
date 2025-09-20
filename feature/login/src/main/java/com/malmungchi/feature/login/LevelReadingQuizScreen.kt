package com.malmungchi.feature.login

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.login.R

// ===== Tokens =====
private val BrandBlue = Color(0xFF195FCF)
private val Gray989898 = Color(0xFF989898)
private val ScreenPadding = 20.dp
private val BackIconInset = 12.dp

// ===== Demo Data =====
private val sampleParagraph = """
“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀가 있듯이, 너희들은 시간을 느끼기 위해 가슴을 갖고 있단다. ...
""".trim()

private data class QuizItem(
    val question: String,
    val options: List<String>,
    val answerIndex: Int
)

private val sampleQuizzes = listOf(
    QuizItem(
        question = "이 글의 핵심 내용을 가장 잘 요약한 것은?",
        options = listOf(
            "말뭉치는 어휘력과 문해력 향상에 도움을 준다",
            "말뭉치는 뭉치와 말치라는 캐릭터가 있으며, 사용자는 치치라고 부른다",
            "디지털미디어학과의 4학년 하이라이트는 캡스톤이다",
            "우리 팀의 캡스톤은 비행기 타고 날라가는 중이다"
        ),
        answerIndex = 0
    ),
    QuizItem(
        question = "문맥상 ‘가슴이 멈춘다’의 의미로 가장 알맞은 것은?",
        options = listOf("생물학적 심장 정지","감정적 공감과 감수성의 상실","시간의 흐름이 빨라짐","현대 사회의 소음 증가"),
        answerIndex = 1
    ),
    QuizItem(
        question = "글의 전개 방식으로 가장 알맞은 것은?",
        options = listOf("사실 나열","비유와 대화의 혼합","통계 설명","원인·결과의 논증"),
        answerIndex = 1
    )
)

// ===== Screen =====
enum class Stage { READING, QUIZ, RESULT }


@Composable
fun LevelReadingQuizScreen(
    onBackClick: () -> Unit = {},
    onShowResult: (answers: List<Int?>) -> Unit = {},
    onNext: () -> Unit = {},
    // 🔵 실데이터 연결용 파라미터
    passage: String? = null,
    questions: List<com.malmungchi.core.model.LevelTestQuestion>? = null,
    selectedAnswers: List<Int?>? = null,
    onSelectAnswer: (questionIndex: Int, choiceIndex: Int) -> Unit = { _, _ -> },
    startStage: Stage = Stage.READING,
    @DrawableRes correctIconRes: Int = R.drawable.ic_correct,
    @DrawableRes wrongIconRes: Int = R.drawable.ic_wrong,
    // 👇 신규: 서버 채점 결과 상세
    resultDetails: List<ResultDetail>? = null
) {
    var stage by remember { mutableStateOf(startStage) }
    var current by remember { mutableStateOf(0) }

    // 결과 보기로 넘어가기 직전의 "마지막 퀴즈 인덱스" 저장 → 결과화면에서 뒤로가기 시 복귀
    var lastQuizIndexBeforeResult by remember { mutableStateOf(0) }

    // 총 문항 수/현재 선택 상태 (실데이터 우선, 없으면 샘플)
    val totalCount = questions?.size ?: sampleQuizzes.size
    val sel = selectedAnswers ?: List(totalCount) { null }
    val allAnswered = sel.size == totalCount && sel.all { it != null }

    // ⬇️ Back 버튼 동작 변경
    val backHandler: () -> Unit = {
        when (stage) {
            Stage.READING -> onBackClick()                      // 인트로로
            Stage.QUIZ    -> { stage = Stage.READING; current = 0 }
            Stage.RESULT  -> { stage = Stage.QUIZ; current = lastQuizIndexBeforeResult }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = ScreenPadding)
    ) {
        val scroll = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(bottom = 120.dp)
        ) {
            Spacer(Modifier.height(48.dp))
            BackIconButton(onClick = backHandler)
            //BackIconButton(onClick = onBackClick)

            Text(
                text = when (stage) {
                    Stage.READING -> "오늘의 학습 맛보기"
                    Stage.QUIZ -> "이해도 퀴즈"
                    Stage.RESULT -> "학습 결과"
                },
                fontFamily = Pretendard,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(start = BackIconInset)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = when (stage) {
                    Stage.READING -> "아래의 글을 집중해서 읽어보세요."
                    Stage.QUIZ -> "읽은 글을 바탕으로 이해도 퀴즈를 풀어보세요."
                    Stage.RESULT -> "이해도 퀴즈의 답안을 확인해보세요."
                },
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Gray989898,
                modifier = Modifier.padding(start = BackIconInset)
            )

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    when (stage) {
                        // 🔵 READING: 실데이터 passage 우선
                        Stage.READING -> {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
                                Text(
                                    text = passage ?: sampleParagraph,
                                    fontFamily = Pretendard,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        // 🔵 QUIZ: 실데이터 질문/보기 우선
                        Stage.QUIZ -> {
                            val qText: String
                            val options: List<String>
                            if (questions != null && current in questions.indices) {
                                qText = questions[current].question
                                options = questions[current].options
                            } else {
                                qText = sampleQuizzes[current].question
                                options = sampleQuizzes[current].options
                            }

                            Column(Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
                                Text(
                                    text = qText,
                                    fontFamily = Pretendard,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Spacer(Modifier.height(10.dp))
                                options.forEachIndexed { idx, opt ->
                                    val isSelected = sel.getOrNull(current) == idx
                                    OptionCard(
                                        text = opt,
                                        selected = isSelected,
                                        onClick = { onSelectAnswer(current, idx) }
                                    )
                                }
                            }
                        }

                        // 🔵 RESULT: 실데이터일 땐 ‘사용자 선택’을 표시(정답 인덱스는 서버 채점)
                        //            샘플일 땐 기존처럼 정오표시 아이콘/하이라이트 유지
                        // RESULT 분기 (실데이터)
                        Stage.RESULT -> {
                            val q = questions?.getOrNull(current)
                            val options = q?.options.orEmpty()

                            // ✅ detail 매칭: 1) questionIndex 기반 2) 없으면 포지션 기반
                            val detailForCurrent: ResultDetail? = resultDetails?.getOrNull(current)
//                            val detailForCurrent: ResultDetail? = when {
//                                resultDetails == null -> null
//                                q != null && q.questionIndex != null -> {
//                                    resultDetails.firstOrNull { it.questionIndex == q.questionIndex }
//                                }
//                                else -> resultDetails.getOrNull(current)
//                            }

                            val selectedIdx = detailForCurrent?.userChoice ?: selectedAnswers?.getOrNull(current)
                            val answerIdx   = detailForCurrent?.answerIndex
                            val isCorrect   = detailForCurrent?.isCorrect

                            Box(Modifier.fillMaxWidth()) {
                                // ✅ 좌상단 큰 아이콘 표시(정답/오답)
                                if (isCorrect != null) {
                                    Image(
                                        painter = painterResource(id = if (isCorrect) correctIconRes else wrongIconRes),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .offset(x = (-12).dp, y = (-12).dp)
                                            .size(100.dp)
                                            .zIndex(1f)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 16.dp)
                                        .padding(top = if (isCorrect != null) 20.dp else 0.dp)
                                        .zIndex(0f)
                                ) {
                                    Text(
                                        text = q?.question.orEmpty(),
                                        fontFamily = Pretendard,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black,
                                        lineHeight = 26.sp
                                    )
                                    Spacer(Modifier.height(16.dp))

                                    options.forEachIndexed { idx, opt ->
                                        val isAnswer = (answerIdx != null && idx == answerIdx)
                                        val isSelectedWrong = (selectedIdx != null
                                                && idx == selectedIdx
                                                && answerIdx != null
                                                && selectedIdx != answerIdx)

                                        ChoiceRowResult(
                                            text = when {
                                                selectedIdx != null && idx == selectedIdx -> "• $opt"
                                                else -> opt
                                            },
                                            isAnswer = isAnswer,
                                            isSelectedWrong = isSelectedWrong
                                        )
                                        Spacer(Modifier.height(8.dp))
                                    }

                                    // (선택) 해설 노출
                                    detailForCurrent?.explanation?.let {
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = it,
                                            fontFamily = Pretendard,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Gray989898
                                        )
                                    }
                                }
                            }
                        }
//                        Stage.RESULT -> {
//                            val isSample = questions == null
//                            if (isSample) {
//                                val item = sampleQuizzes[current]
//                                val selected = sel[current]
//                                val isCorrect = selected == item.answerIndex
//
//                                Box(Modifier.fillMaxWidth()) {
//                                    Image(
//                                        painter = painterResource(
//                                            id = if (isCorrect) correctIconRes else wrongIconRes
//                                        ),
//                                        contentDescription = null,
//                                        modifier = Modifier
//                                            .align(Alignment.TopStart)
//                                            .offset(x = (-12).dp, y = (-12).dp)
//                                            .size(100.dp)
//                                            .zIndex(1f)
//                                    )
//                                    Column(
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(horizontal = 12.dp, vertical = 16.dp)
//                                            .padding(top = 20.dp)
//                                            .zIndex(0f)
//                                    ) {
//                                        Text(
//                                            text = item.question,
//                                            fontFamily = Pretendard,
//                                            fontSize = 18.sp,
//                                            fontWeight = FontWeight.SemiBold,
//                                            color = Color.Black,
//                                            lineHeight = 26.sp
//                                        )
//                                        Spacer(Modifier.height(16.dp))
//                                        item.options.forEachIndexed { idx, opt ->
//                                            val isAnswer = idx == item.answerIndex
//                                            val isSelectedWrong =
//                                                (idx == selected) && (selected != item.answerIndex)
//                                            ChoiceRowResult(
//                                                text = opt,
//                                                isAnswer = isAnswer,
//                                                isSelectedWrong = isSelectedWrong
//                                            )
//                                            Spacer(Modifier.height(8.dp))
//                                        }
//                                    }
//                                }
//                            } else {
//                                // 실데이터 결과 화면(정답 인덱스는 서버 채점/반영 완료 → 별도 완료 화면에서 안내)
//                                //val q = questions.getOrNull(current)
//                                val q = questions?.getOrNull(current)
//                                val options = q?.options.orEmpty()
//                                val selectedIdx = sel.getOrNull(current)
//
//                                Column(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(horizontal = 12.dp, vertical = 16.dp)
//                                ) {
//                                    Text(
//                                        text = q?.question.orEmpty(),
//                                        fontFamily = Pretendard,
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = Color.Black,
//                                        lineHeight = 26.sp
//                                    )
//                                    Spacer(Modifier.height(16.dp))
//                                    options.forEachIndexed { idx, opt ->
//                                        // 정답정보는 UI에 없으므로, 사용자 선택만 중립 표시
//                                        val picked = (idx == selectedIdx)
//                                        ChoiceRowResult(
//                                            text = if (picked) "• $opt" else opt,
//                                            isAnswer = false,
//                                            isSelectedWrong = false
//                                        )
//                                        Spacer(Modifier.height(8.dp))
//                                    }
//                                }
//                            }
//                        }
                    }
                }

                // 🔵 진행 배지: 실데이터 문항 수 반영
                ProgressBadge(
                    text = "${current + 1}/$totalCount",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = 8.dp, y = (-12).dp)
                )
            }

            // 🔵 네비 버튼 로직(실데이터 기준으로 enable 계산)
            if (stage == Stage.QUIZ || stage == Stage.RESULT) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val upEnabled = current > 0
                    val downEnabled = when (stage) {
                        Stage.QUIZ -> (sel[current] != null) && (current < totalCount - 1)
                        Stage.RESULT -> current < totalCount - 1
                        else -> false
                    }

                    CircleIconButton(
                        enabled = upEnabled,
                        isUp = true,
                        onClick = { if (upEnabled) current-- }
                    )
                    Spacer(Modifier.width(12.dp))
                    CircleIconButton(
                        enabled = downEnabled,
                        isUp = false,
                        onClick = { if (downEnabled) current++ }
                    )
                }
            }
        }

        // 하단 CTA
        when (stage) {
            Stage.READING -> {
                BlueButton(
                    text = "문제 풀기",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                        .height(48.dp)
                        .width(200.dp)
                ) { stage = Stage.QUIZ }
            }
            Stage.QUIZ -> {
                if (current == totalCount - 1 && allAnswered) {
                    BlueButton(
                        text = "결과 보기",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp)
                            .height(48.dp)
                            .width(200.dp)
                    ) {
                        lastQuizIndexBeforeResult = current
                        onShowResult(sel)      // 채점 요청
                        stage = Stage.RESULT   // 결과 검토 화면으로
//                        onShowResult(sel)      // 실선택 그대로 뷰모델에 제출
//                        stage = Stage.RESULT   // (Route에서 Result화면은 LevelSetCompleteScreen으로 처리)
                    }
                }
            }
            Stage.RESULT -> {
                BlueButton(
                    text = "다음으로",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp)
                        .height(48.dp)
                        .width(200.dp)
                ) { onNext() }
            }
        }
    }
}

//@Composable
//fun LevelReadingQuizScreen(
//    onBackClick: () -> Unit = {},
//    onShowResult: (answers: List<Int?>) -> Unit = {},
//    onNext: () -> Unit = {},
//    // 🔵 추가: 실데이터 연결용 파라미터
//    passage: String? = null,
//    questions: List<com.malmungchi.core.model.LevelTestQuestion>? = null,
//    selectedAnswers: List<Int?>? = null,
//    onSelectAnswer: (questionIndex: Int, choiceIndex: Int) -> Unit = { _, _ -> },
//    startStage: Stage = Stage.READING,
//    // 로그인 모듈 PNG 사용 (ic_correct.png / ic_wrong.png)
//    @DrawableRes correctIconRes: Int = R.drawable.ic_correct,
//    @DrawableRes wrongIconRes: Int = R.drawable.ic_wrong,
//) {
//
//    var stage by remember { mutableStateOf(startStage) }
//    val answers = remember { mutableStateListOf<Int?>(null, null, null) }
//    var current by remember { mutableStateOf(0) }
//    val allAnswered = answers.all { it != null }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(horizontal = ScreenPadding)
//    ) {
//        val scroll = rememberScrollState()
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(scroll)
//                .padding(bottom = 120.dp) // 하단 고정 버튼과 겹침 방지
//        ) {
//            Spacer(Modifier.height(48.dp))
//
//            BackIconButton(onClick = onBackClick)
//
//            Text(
//                text = when (stage) {
//                    Stage.READING -> "오늘의 학습 맛보기"
//                    Stage.QUIZ -> "이해도 퀴즈"
//                    Stage.RESULT -> "학습 결과"
//                },
//                fontFamily = Pretendard,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color.Black,
//                modifier = Modifier.padding(start = BackIconInset)
//            )
//
//            Spacer(Modifier.height(24.dp))
//
//            Text(
//                text = when (stage) {
//                    Stage.READING -> "아래의 글을 집중해서 읽어보세요."
//                    Stage.QUIZ -> "읽은 글을 바탕으로 이해도 퀴즈를 풀어보세요."
//                    Stage.RESULT -> "이해도 퀴즈의 정답을 확인해보세요."
//                },
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Gray989898,
//                modifier = Modifier.padding(start = BackIconInset)
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            // ===== 카드 + 배지(1/3) 오버레이 레이아웃 =====
//            Box(modifier = Modifier.fillMaxWidth()) {
//
//                // (A) 카드 본체
//                Card(
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)),
//                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
//                    shape = RoundedCornerShape(12.dp),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 12.dp)
//                ) {
//                    when (stage) {
//                        Stage.READING -> {
//                            Column(Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
//                                Text(
//                                    text = sampleParagraph,
//                                    fontFamily = Pretendard,
//                                    fontSize = 16.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = Color.Black,
//                                    lineHeight = 22.sp
//                                )
//                            }
//                        }
//
//                        Stage.QUIZ -> {
//                            val item = sampleQuizzes[current]
//                            Column(Modifier.padding(horizontal = 12.dp, vertical = 16.dp)) {
//                                // 문제
//                                Text(
//                                    text = item.question,
//                                    fontFamily = Pretendard,
//                                    fontSize = 18.sp,
//                                    fontWeight = FontWeight.SemiBold,
//                                    color = Color.Black
//                                )
//                                Spacer(Modifier.height(10.dp))
//                                item.options.forEachIndexed { idx, opt ->
//                                    val selected = answers[current] == idx
//                                    OptionCard(
//                                        text = opt,
//                                        selected = selected,
//                                        onClick = { answers[current] = idx }
//                                    )
//                                }
//                            }
//                        }
//
//                        Stage.RESULT -> {
//                            val item = sampleQuizzes[current]
//                            val selected = answers[current]
//                            val isCorrect = selected == item.answerIndex
//
//                            // 결과 아이콘 & 내용 (카드 내부: 버튼 없음)
//                            Box(Modifier.fillMaxWidth()) {
//                                // 좌상단 큰 아이콘 (카드 위로)
//                                Image(
//                                    painter = painterResource(
//                                        id = if (isCorrect) correctIconRes else wrongIconRes
//                                    ),
//                                    contentDescription = null,
//                                    modifier = Modifier
//                                        .align(Alignment.TopStart)
//                                        .offset(x = (-12).dp, y = (-12).dp)
//                                        .size(100.dp)
//                                        .zIndex(1f)
//                                )
//
//                                // 카드 내부 컨텐츠
//                                Column(
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .padding(horizontal = 12.dp, vertical = 16.dp)
//                                        .padding(top = 20.dp) // 아이콘과 간섭 방지
//                                        .zIndex(0f)
//                                ) {
//                                    Text(
//                                        text = item.question,
//                                        fontFamily = Pretendard,
//                                        fontSize = 18.sp,
//                                        fontWeight = FontWeight.SemiBold,
//                                        color = Color.Black,
//                                        lineHeight = 26.sp
//                                    )
//                                    Spacer(Modifier.height(16.dp))
//
//                                    item.options.forEachIndexed { idx, opt ->
//                                        val isAnswer = idx == item.answerIndex
//                                        val isSelectedWrong =
//                                            (idx == selected) && (selected != item.answerIndex)
//                                        ChoiceRowResult(
//                                            text = opt,
//                                            isAnswer = isAnswer,
//                                            isSelectedWrong = isSelectedWrong
//                                        )
//                                        Spacer(Modifier.height(8.dp))
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // (D) 진행 배지 (피그마처럼 카드 좌상단 바깥에 크게)
//                ProgressBadge(
//                    text = "${current + 1}/${sampleQuizzes.size}",
//                    modifier = Modifier
//                        .align(Alignment.TopStart)
//                        .offset(x = 8.dp, y = (-12).dp) // 카드 밖으로 살짝 올려서 겹치게
//                )
//            }
//
//            // 하단 네비 (QUIZ / RESULT 공용)
//            if (stage == Stage.QUIZ || stage == Stage.RESULT) {
//                Spacer(Modifier.height(16.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    val upEnabled = current > 0
//                    val downEnabled = when (stage) {
//                        Stage.QUIZ -> (answers[current] != null) && (current < sampleQuizzes.lastIndex)
//                        Stage.RESULT -> current < sampleQuizzes.lastIndex
//                        else -> false
//                    }
//
//                    CircleIconButton(
//                        enabled = upEnabled,
//                        isUp = true,
//                        onClick = { if (upEnabled) current-- }
//                    )
//                    Spacer(Modifier.width(12.dp))
//                    CircleIconButton(
//                        enabled = downEnabled,
//                        isUp = false,
//                        onClick = { if (downEnabled) current++ }
//                    )
//                }
//            }
//        }
//
//        // ===== 하단 고정 CTA (화면 바닥 기준) =====
//        when (stage) {
//            Stage.READING -> {
//                BlueButton(
//                    text = "문제 풀기",
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(bottom = 48.dp)
//                        .height(48.dp)
//                        .width(200.dp)
//                ) { stage = Stage.QUIZ }
//            }
//
//            Stage.QUIZ -> {
//                if (current == sampleQuizzes.lastIndex && allAnswered) {
//                    BlueButton(
//                        text = "결과 보기",
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(bottom = 48.dp)
//                            .height(48.dp)
//                            .width(200.dp)
//                    ) {
//                        onShowResult(answers.toList())
//                        stage = Stage.RESULT
//                    }
//                }
//            }
//
//            Stage.RESULT -> {
//                BlueButton(
//                    text = "다음으로",
//                    modifier = Modifier
//                        .align(Alignment.BottomCenter)
//                        .padding(bottom = 48.dp)
//                        .height(48.dp)
//                        .width(200.dp)
//                ) { onNext() }  // ← 여기서 MainApp으로 네비게이션
//            }
//        }
//    }
//}

// ===== 신규: 수준 설정 완료 / 시작 화면 =====
@Composable
fun LevelSetCompleteScreen(
    onRetry: () -> Unit = {},
    onStart: () -> Unit = {},
    @DrawableRes characterRes: Int = R.drawable.ic_complete_character,
    levelTitle: String // 👈 추가: "기초/실용/심화/고급"
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = ScreenPadding)
    ) {
        // 상단 본문
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp) // 하단 버튼과 겹침 방지
        ) {
            Spacer(Modifier.height(80.dp))

            Text(
                text = "수준 설정 완료!",
                fontFamily = Pretendard,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandBlue
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${levelTitle} 학습을 시작해볼까요?",  // 👈 동적 문구
                fontFamily = Pretendard,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            // 캐릭터 이미지 중앙 배치
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = characterRes),
                    contentDescription = null,
                    modifier = Modifier.wrapContentSize()
                )
            }
        }

        // 하단 고정: 다시하기(아웃라인) / 시작하기(블루)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedBlueButton(
                text = "다시하기",
                modifier = Modifier
                    .height(48.dp)
                    .width(160.dp),
                onClick = onRetry
            )
            BlueButton(
                text = "시작하기",
                modifier = Modifier
                    .height(48.dp)
                    .width(160.dp),
                onClick = onStart
            )
        }
    }
}

// ===== 신규: 수준 설정 중단 경고 Alert =====
@Composable
fun LevelExitAlert(
    onCancelClick: () -> Unit,    // 왼쪽(취소하기) 클릭: 중단 취소 / 닫기
    onContinueClick: () -> Unit,  // 오른쪽(계속하기) 클릭: 중단 진행
    onDismissRequest: () -> Unit = onCancelClick
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "수준 설정을 마치지 않으면 학습을 진행할 수 없어요 :(",
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF616161),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "정말 취소하시겠어요?",
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {},
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 14-1 취소하기: 밝은 회색 버튼 (중단 취소)
                Button(
                    onClick = onCancelClick,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEFF2F7),
                        contentColor = Color(0xFF9AA5B1)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                ) {
                    Text("취소하기", fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                // 14-2 계속하기: 블루 버튼
                Button(
                    onClick = onContinueClick,
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(1f)
                ) {
                    Text("계속하기", fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        },
        dismissButton = {},
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
//        // ✅ 여기서 크기 조절
//        modifier = Modifier
//            .fillMaxWidth(0.8f)   // 화면 너비의 90%까지 확장
//            .padding(horizontal = 24.dp)
    )
}

// ===== 공용 위젯들 =====
@Composable
private fun ProgressBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0x14000000)),
        modifier = modifier
            .height(28.dp)
            .wrapContentWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Text(
                text = text,
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Gray989898
            )
        }
    }
}

@Composable
private fun BackIconButton(onClick: () -> Unit) {
    val isPreview = LocalInspectionMode.current
    IconButton(onClick = onClick) {
        if (isPreview) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.Unspecified
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified
            )
        }
    }
}

@Composable
private fun OptionCard(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) BrandBlue else Color(0x14000000)
    val bgColor = if (selected) Color(0xFFEFF4FB) else Color(0xFFFFFFFF)

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(if (selected) 3.dp else 2.dp),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun ChoiceRowResult(
    text: String,
    isAnswer: Boolean,
    isSelectedWrong: Boolean
) {
    val bgColor = when {
        isAnswer -> Color(0xFFD1DFF5) // CorrectFill
        isSelectedWrong -> Color(0xFFFFCCCC) // WrongFill
        else -> Color(0xFFF7F7F7) // ChipGray
    }
    val borderColor = when {
        isAnswer -> BrandBlue
        isSelectedWrong -> Color(0xFFFF0000)
        else -> Color(0xFFE0E0E0)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = 0.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            fontSize = 14.sp,
            fontFamily = Pretendard,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun CircleIconButton(
    enabled: Boolean,
    isUp: Boolean,
    onClick: () -> Unit
) {
    val isPreview = LocalInspectionMode.current

    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0x11000000)),
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isPreview) {
                Icon(
                    imageVector = if (isUp) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = if (enabled) Color(0xFF666666) else Color(0xFFBDBDBD)
                )
            } else {
                val res = when {
                    isUp && enabled -> R.drawable.ic_up_button
                    isUp && !enabled -> R.drawable.ic_up_button_null
                    !isUp && enabled -> R.drawable.ic_down_button
                    else -> R.drawable.ic_down_button_null
                }
                Icon(
                    painter = painterResource(id = res),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
private fun BlueButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun OutlinedBlueButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, BrandBlue),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = BrandBlue
        ),
        modifier = modifier // ← 호출부에서 width/weight를 직접 지정 가능
    ) {
        Text(
            text = text,
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ===== Previews =====
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewReading() {
    MaterialTheme { Surface { LevelReadingQuizScreen(startStage = Stage.READING) } }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewQuizNotAnswered() {
    MaterialTheme { Surface { LevelReadingQuizScreen(startStage = Stage.QUIZ) } }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "Result (login 리소스)")
@Composable
private fun PreviewResult() {
    MaterialTheme {
        Surface {
            LevelReadingQuizScreen(
                startStage = Stage.RESULT,
                correctIconRes = com.malmungchi.feature.login.R.drawable.ic_correct,
                wrongIconRes   = com.malmungchi.feature.login.R.drawable.ic_wrong
            )
        }
    }
}

//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "수준설정완료/시작 화면")
//@Composable
//private fun PreviewLevelSetComplete() {
//    MaterialTheme {
//        Surface {
//            LevelSetCompleteScreen(
//                onRetry = {},
//                onStart = {},
//                // 프리뷰 안전용 시스템 드로어블(리소스 깨짐 방지)
////                characterRes = android.R.drawable.ic_menu_gallery
////            )
//        }
//    }
//}

@Preview(showBackground = true, name = "수준 설정 중단 경고 Alert")
@Composable
private fun PreviewLevelExitAlert() {
    MaterialTheme {
        Surface {
            LevelExitAlert(
                onCancelClick = {},
                onContinueClick = {}
            )
        }
    }
}
