package com.malmungchi.feature.mypage.nickname

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.mypage.R as MyPageR

// ===== Palette =====
private val BrandBlue = Color(0xFF195FCF)
private val TrackGray = Color(0xFFFAFAFA)
private val TextGray = Color(0xFF989898)




/**
 * OX 화면의 "문제/본문" 레이아웃 + MCQ 화면의 "4지선다/제출"을 결합한 화면.
 * 2문제를 풀면 onFinishReadingMcq() 호출.
 */
@Composable
fun NicknameTestReadingMcqScreen(
    questions: List<ReadingMcqQuestion>,
    answeredGlobalCount: Int,
    onBackClick: () -> Unit = {},
    onFinishReadingMcq: (tier: VocabularyTier, correctCount: Int) -> Unit = { _, _ -> },
    initialIndex: Int = 0
) {
    var index by rememberSaveable { mutableStateOf(initialIndex) }
    val selections = rememberSaveable(
        questions,
        saver = listSaver<SnapshotStateList<Int>, Int>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        MutableList(questions.size) { -1 }.toMutableStateList()
    }

    // ⬇️ 여기 추가
    BackHandler {
        if (index > 0) {
            index -= 1
        } else {
            onBackClick()
        }
    }

    val q = questions.getOrNull(index)

    Column(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 48.dp)) {
        Spacer(Modifier.height(48.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackChevron(onClick = {
                if (index > 0) index -= 1 else onBackClick()
            })
        }

        Spacer(Modifier.height(20.dp))

        val totalQuestions = 18
        val solved = answeredGlobalCount + index
        val progress = solved.toFloat() / totalQuestions.toFloat()
        ProgressBarLarge(progress, Color(0xFFFAFAFA), Color(0xFF195FCF), 10.dp)

        Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
            Spacer(Modifier.height(42.dp))
            Text(
                text = q?.numberLabel.orEmpty(),
                fontFamily = Pretendard, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = q?.questionText.orEmpty(),
                fontFamily = Pretendard, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                color = Color.Black, lineHeight = 33.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            StatementCard(text = q?.statement.orEmpty(), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(Modifier.height(24.dp))

            q?.options?.forEach { opt ->
                val selected = selections[index] == opt.id
                OptionItem(
                    label = opt.label,
                    selected = selected,
                    onClick = { selections[index] = opt.id }
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        val enabled = selections[index] != -1
        val isLast = index == questions.lastIndex
        Button(
            onClick = {
                if (!isLast) {
                    index += 1
                } else {
                    val correctCount = questions.indices.count { i ->
                        selections[i] == questions[i].answerOptionId
                    }
                    val tier = when (correctCount) {
                        in 7..9 -> VocabularyTier.상
                        in 4..6 -> VocabularyTier.중
                        else -> VocabularyTier.하
                    }
                    onFinishReadingMcq(tier, correctCount)
                }
            },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) Color(0xFF195FCF) else Color(0xFFFAFAFA),
                contentColor = if (enabled) Color.White else Color(0xFF989898)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().padding(start = 80.dp, end = 80.dp, bottom = 48.dp).height(48.dp).align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = if (isLast) "결과 보기" else "정답 제출",
                fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
            )
        }
    }
}
//@Composable
//fun NicknameTestReadingMcqScreen(
//    questions: List<ReadingMcqQuestion>,
//    answeredGlobalCount: Int,
//    onBackClick: () -> Unit = {},
//    onSubmitAnswer: (questionId: Int, selectedOptionId: Int, isCorrect: Boolean) -> Unit = { _, _, _ -> },
//    onFinishReadingMcq: (tier: VocabularyTier, correctCount: Int) -> Unit = { _, _ -> }
//) {
//    var index by remember { mutableStateOf(0) }
//    var selectedId by remember { mutableStateOf<Int?>(null) }
//    var correctCount by remember { mutableStateOf(0) }
//
//    val q = questions.getOrNull(index)
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(start = 20.dp, end = 20.dp, bottom = 48.dp)
//    ) {
//        Spacer(Modifier.height(48.dp))
//
//        // 상단 Back
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            BackChevron(onClick = onBackClick)
//        }
//
//        Spacer(Modifier.height(42.dp))
//
//        // === Progress (전체 18문항 기준) ===
//        val totalQuestions = 18
//        val solved = answeredGlobalCount + index
//        val progress = solved.toFloat() / totalQuestions.toFloat()
//        ProgressBarLarge(
//            progress = progress,
//            trackColor = TrackGray,
//            progressColor = BrandBlue,
//            height = 10.dp
//        )
//
//        // 본문/선지 영역은 좌우 6dp 인셋
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 6.dp)
//        ) {
//            Spacer(Modifier.height(32.dp))
//
//            // Q 번호
//            Text(
//                text = q?.numberLabel.orEmpty(),
//                fontFamily = Pretendard,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color.Black,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            // 문제 문장 (OX 스타일)
//            Text(
//                text = q?.questionText.orEmpty(),
//                fontFamily = Pretendard,
//                fontSize = 22.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color.Black,
//                lineHeight = 33.sp, // 150%
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(Modifier.height(24.dp))
//
//            // 본문 카드 (OX 스타일)
//            StatementCard(
//                text = q?.statement.orEmpty(),
//                modifier = Modifier.padding(horizontal = 12.dp)
//            )
//
//            Spacer(Modifier.height(32.dp))
//
//            // 4지선다 (MCQ 스타일)
//            q?.options?.forEach { opt ->
//                val selected = selectedId == opt.id
//                OptionItem(
//                    label = opt.label,
//                    selected = selected,
//                    onClick = { selectedId = opt.id }
//                )
//                Spacer(Modifier.height(12.dp))
//            }
//        }
//
//        Spacer(Modifier.weight(1f))
//
//        val enabled = selectedId != null
//        val isLast = (index == questions.lastIndex)
//        Button(
//            onClick = {
//                val sel = selectedId ?: return@Button
//                val isCorrect = (sel == q!!.answerOptionId)
//                if (isCorrect) correctCount += 1
//                onSubmitAnswer(q.id, sel, isCorrect)
//
//                if (index < questions.lastIndex) {
//                    index += 1
//                    selectedId = null
//                } else {
//                    // 2문제 종료 → 티어 산정 로직(예: 2문제 기준 간단 버전)
//                    val tier = when (correctCount) {
//                        2 -> VocabularyTier.상
//                        1 -> VocabularyTier.중
//                        else -> VocabularyTier.하
//                    }
//                    onFinishReadingMcq(tier, correctCount)
//                }
//            },
//            enabled = enabled,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = if (enabled) BrandBlue else TrackGray,
//                contentColor = if (enabled) Color.White else TextGray
//            ),
//            shape = MaterialTheme.shapes.extraLarge,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 80.dp)
//                .height(48.dp)
//                .align(Alignment.CenterHorizontally)
//        ) {
//            Text(
//                text = if (isLast) "결과 보기" else "정답 제출",
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//    }
//}

// ===== 작은 부품들 (기존 OX/MCQ 스타일 그대로) =====
@Composable
private fun StatementCard(
    text: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Card(
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = text,
                fontFamily = Pretendard,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 27.sp, // 150%
                color = Color.Black
            )
        }
    }
}

@Composable
private fun OptionItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (selected) BrandBlue else Color(0xFFE0E0E0)
    val bg = if (selected) BrandBlue.copy(alpha = 0.2f) else Color.White
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(width = 2.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun BackChevron(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = MyPageR.drawable.ic_back),
            contentDescription = "뒤로가기",
            tint = Color.Unspecified
        )
    }
}

@Composable
private fun ProgressBarLarge(
    progress: Float,
    trackColor: Color,
    progressColor: Color,
    height: Dp
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(shape)
                .background(progressColor)
        )
    }
}

//// ===== Preview =====
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun PreviewNicknameTestReadingMcqScreen() {
//    val sample = listOf(
//        ReadingMcqQuestion(
//            id = 201,
//            numberLabel = "Q10",
//            statement = "SNS의 확산은 정보 접근성을 높이지만, 가짜 뉴스의 전파 또한 가속화시킨다. 이에 따라 현대인은 정보의 신뢰성을 판단하는 능력이 요구된다.",
//            questionText = "글의 핵심 주제는?",
//            options = listOf(
//                McqOption(1, "공감"),
//                McqOption(2, "직관"),
//                McqOption(3, "분석"),
//                McqOption(4, "판단")
//            ),
//            answerOptionId = 4
//        ),
//        ReadingMcqQuestion(
//            id = 202,
//            numberLabel = "Q11",
//            statement = "지진은 1차 피해뿐만 아니라 정전·가스 누출 등 2차 피해를 유발할 수 있다. 따라서 대피 요령과 더불어 화재 예방 훈련도 중요하다.",
//            questionText = "본문의 내용으로 알 수 있는 것은?",
//            options = listOf(
//                McqOption(1, "대피 요령만 익히면 충분하다"),
//                McqOption(2, "2차 피해는 무시해도 된다"),
//                McqOption(3, "화재 예방 교육이 필요하다"),
//                McqOption(4, "지진은 예측 가능하다")
//            ),
//            answerOptionId = 3
//        )
//    )
//
//    MaterialTheme {
//        Surface {
//            NicknameTestReadingMcqScreen(
//                questions = sample,
//                answeredGlobalCount = 9,
//                onBackClick = {},
//                onSubmitAnswer = { _, _, _ -> },
//                onFinishReadingMcq = { _, _ -> }
//            )
//        }
//    }
//}
