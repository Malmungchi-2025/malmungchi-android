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
import com.malmungchi.core.model.WordItem

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
    val studyId by viewModel.studyId.collectAsState(initial = null)
    val words by viewModel.savedWords.collectAsState()

    // ✅ studyId가 세팅되면 그날의 단어 목록 로드
    LaunchedEffect(studyId) {
        studyId?.let { viewModel.loadVocabularyList(it) }
    }

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
        words = words,                // ✅ 단어 전달
        onBackClick = onBackClick
    )
}

// ---------- 순수 UI ----------
@Composable
fun PastStudyScreen(
    dateLabel: String,
    bodyText: String,
    quizzes: List<PastQuizUI>,
    words: List<WordItem>,
    onBackClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, bottom = 48.dp, top = 48.dp),
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

            SectionCard {
                Text(
                    text = if (bodyText.isBlank()) "불러올 본문이 없습니다." else bodyText,
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333),
                    lineHeight = 25.6.sp
                )
            }

            Spacer(Modifier.height(16.dp))

//            Surface(
//                shape = RoundedCornerShape(12.dp),
//                color = Color(0xFFF7F7F7),
//                shadowElevation = 0.dp,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text(
//                    text = if (bodyText.isBlank()) "불러올 본문이 없습니다." else bodyText,
//                    fontSize = 16.sp,
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    color = Color(0xFF333333),
//                    lineHeight = 25.6.sp,
//                    modifier = Modifier.padding(16.dp)
//                )
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            //SectionTitle("이해 퀴즈")
//            Spacer(Modifier.height(8.dp))
        }

        // ---------- Quiz List ----------
        item {
            Spacer(Modifier.height(16.dp))
            SectionTitle("이해 퀴즈")
            Spacer(Modifier.height(8.dp))

            SectionCard {
                if (quizzes.isEmpty()) {
                    Text(
                        text = "이해 퀴즈가 없습니다.",
                        fontSize = 16.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                } else {
                    Column {
                        quizzes.forEachIndexed { index, item ->
                            QuizResultCard(index = index, total = quizzes.size, data = item)
                            if (index != quizzes.lastIndex) Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }


        // ----- 수집한 단어 -----
        item {
            Spacer(Modifier.height(16.dp))
            SectionTitle("수집한 단어")
            Spacer(Modifier.height(8.dp))

            SectionCard {
                if (words.isEmpty()) {
                    Text(
                        text = "수집한 단어가 없습니다.",
                        fontSize = 16.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF666666)
                    )
                } else {
                    Column {
                        words.forEachIndexed { i, w ->
                            WordCard(w)   // 각 카드 배경 F7F7F7
                            if (i != words.lastIndex) Spacer(Modifier.height(8.dp))
                        }
                    }
                }
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
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7F7F7), // ✅ 섹션 카드 배경 F7F7F7
        shadowElevation = 2.dp,     // ✅ 섹션 카드 그림자
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun QuizResultCard(index: Int, total: Int, data: PastQuizUI) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF7F7F7),   // 아이템 카드 배경
        shadowElevation = 2.dp,      // 아이템 카드 그림자
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("${index + 1}/$total", fontSize = 12.sp, fontFamily = Pretendard, color = Color(0xFF666666))
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

            data.choices.forEach { choice ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF7F7F7),
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = choice,
                        fontSize = 14.sp,
                        fontFamily = Pretendard,
                        color = Color(0xFF111111),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // ✅ 해설은 항상 노출 (정답/OX 없이)
            if (data.explanation.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text("해설", fontSize = 12.sp, fontFamily = Pretendard, color = Color(0xFF888888))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = data.explanation,
                    fontSize = 14.sp,
                    fontFamily = Pretendard,
                    color = Color(0xFF333333),
                    lineHeight = 22.sp
                )
            }
        }
    }
}


@Composable
fun WordCard(item: WordItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 0.dp, // 그림자 제거
        color = Color(0xFFF7F7F7), // ← 요구 색
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                item.word,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
                color = Color(0xFF333333)
            )
            Text(
                ": ${item.meaning}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                color = Color(0xFF333333),
                modifier = Modifier.padding(top = 4.dp)
            )
            if (!item.example.isNullOrEmpty()) {
                Text(
                    "예문) ${item.example}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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
        quizzes = emptyList(),
        words = emptyList(),

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
        quizzes = sample,
        words = emptyList(),
    )
}