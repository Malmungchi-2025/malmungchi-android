package com.malmungchi.feature.study.intro


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.model.QuizItem
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.StudyReadingViewModel

// ---------- UI 모델 ----------
data class PastQuizUI(
    val question: String,
    val choices: List<String>,
    val correctIndex: Int,
    val userAnswerIndex: Int?,
    val explanation: String
)

// ---------- Route / Wrapper ----------
@Composable
fun PastStudyScreenRoute(
    dateLabel: String,                                   // ex) "2025.04.01"
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onLoad: (suspend () -> Unit)? = null,                // ← 호출부에서 API 로드 전달
    onBackClick: () -> Unit = {}
) {
    // 필요 시 로딩 실행
    LaunchedEffect(Unit) { onLoad?.invoke() }

    val quote: String? by viewModel.quote.collectAsState()
    val quizItems: List<QuizItem> by viewModel.quizList.collectAsState(initial = emptyList())

    val uiQuizzes: List<PastQuizUI> = remember(quizItems) {
        quizItems.map { q ->
            val correctIdx = q.options.indexOf(q.answer).coerceAtLeast(0)
            val userIdx = q.userChoice?.let { uc -> q.options.indexOf(uc).takeIf { it >= 0 } }
            PastQuizUI(
                question = q.question,
                choices = q.options,
                correctIndex = correctIdx,
                userAnswerIndex = userIdx,
                explanation = q.explanation
            )
        }
    }

    PastStudyScreen(
        dateLabel = dateLabel,
        bodyText = quote.orEmpty(),
        quizzes = uiQuizzes,
        onBackClick = onBackClick
    )
}

// ---------- 순수 UI ----------
@Composable
fun PastStudyScreen(
    dateLabel: String,
    bodyText: String,
    quizzes: List<PastQuizUI>,
    onBackClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 32.dp),
        verticalArrangement = Arrangement.Top,
        contentPadding = PaddingValues(bottom = 32.dp) // 하단 여백
    ) {
        // ---------- TopBar ----------
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "지난 학습",
                    fontSize = 20.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "<",
                    fontSize = 20.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .noRippleClickable { onBackClick() }
                )
            }

            Spacer(Modifier.height(24.dp))

            // 날짜 칩
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF195FCF))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = dateLabel,
                        fontSize = 12.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionTitle("본문")
            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF7F7F7),
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (bodyText.isBlank()) "불러올 본문이 없습니다." else bodyText,
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    lineHeight = 25.6.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            SectionTitle("이해 퀴즈")
            Spacer(Modifier.height(8.dp))
        }

        // ---------- Quiz List ----------
        if (quizzes.isEmpty()) {
            item {
                Text(
                    text = "이해 퀴즈가 없습니다.",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666)
                )
            }
        } else {
            itemsIndexed(quizzes) { index, item ->
                QuizResultCard(index = index, total = quizzes.size, data = item)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontFamily = Pretendard,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    )
}

@Composable
private fun QuizResultCard(index: Int, total: Int, data: PastQuizUI) {
    val isCorrect = data.userAnswerIndex != null && data.userAnswerIndex == data.correctIndex
    val resultIcon = if (isCorrect) R.drawable.ic_correct else R.drawable.ic_wrong

    Box(Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = resultIcon),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp)
                .align(Alignment.TopStart)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "${index + 1}/$total",
                fontSize = 12.sp,
                fontFamily = Pretendard,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = data.question,
                fontSize = 18.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 26.sp,
                color = Color.Black
            )
            Spacer(Modifier.height(12.dp))

            data.choices.forEachIndexed { i, choice ->
                val isSelected = data.userAnswerIndex == i
                val isCorrectAnswer = data.correctIndex == i

                val bg = when {
                    isCorrectAnswer -> Color(0xFF195FCF)
                    isSelected -> Color(0xFFE0E0E0)
                    else -> Color(0xFFF7F7F7)
                }
                val fg = if (isCorrectAnswer) Color.White else Color.Black

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = bg,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = choice,
                        fontSize = 14.sp,
                        fontFamily = Pretendard,
                        color = fg,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("정답", fontSize = 12.sp, fontFamily = Pretendard, color = Color.Gray)
            Text(
                text = data.choices[data.correctIndex],
                fontSize = 14.sp,
                fontFamily = Pretendard,
                color = Color.DarkGray
            )
            Spacer(Modifier.height(8.dp))
            Text("해설", fontSize = 12.sp, fontFamily = Pretendard, color = Color.Gray)
            Text(
                text = data.explanation,
                fontSize = 14.sp,
                fontFamily = Pretendard,
                color = Color.DarkGray
            )
        }
    }
}

// ripple 없는 클릭 확장
fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    this.clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    ) { onClick() }
}

// ---------- Preview ----------
@Preview(showBackground = true)
@Composable
private fun PreviewPastStudyScreen_NoQuiz() {
    PastStudyScreen(
        dateLabel = "2025.04.01",
        bodyText = "예시 본문",
        quizzes = emptyList()
    )
}
@Preview(showBackground = true)
@Composable
private fun PreviewPastStudyScreen_Centered() {
    val sample = listOf(
        PastQuizUI("이 글의 핵심을 가장 잘 요약한 것은?", listOf("요약1","요약2","요약3","요약4"), 1, 1, "핵심은 ~~ 입니다."),
        PastQuizUI("두 번째 문제입니다.", listOf("A","B","C","D"), 3, 2, "D가 맞는 이유는 ~~"),
        PastQuizUI("세 번째 문제입니다.", listOf("ㄱ","ㄴ","ㄷ","ㄹ"), 0, null, "ㄱ을 고르는 것이 적절합니다.")
    )
    PastStudyScreen(
        dateLabel = "2025.04.01",
        bodyText = "예시 본문",
        quizzes = sample
    )
}