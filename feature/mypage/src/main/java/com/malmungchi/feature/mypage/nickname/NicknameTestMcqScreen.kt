package com.malmungchi.feature.mypage.nickname

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

@Composable
fun NicknameTestMcqScreen(
    questions: List<McqQuestion>,
    answeredGlobalCount: Int,
    onBackClick: () -> Unit = {},
    onFinishVocabulary: (tier: VocabularyTier, correctCount: Int) -> Unit = { _, _ -> },
    initialIndex: Int = 0
) {
    var index by rememberSaveable { mutableStateOf(initialIndex) }
    // 각 문항 선택 저장 (-1 = 미선택)
    val selections = rememberSaveable(
        questions,
        saver = listSaver<SnapshotStateList<Int>, Int>(
            save = { it.toList() },                // Bundle에 저장 가능한 List<Int>로 변환
            restore = { it.toMutableStateList() }  // 다시 SnapshotStateList<Int>로 복원
        )
    ) {
        MutableList(questions.size) { -1 }.toMutableStateList()
    }

    // ⬇️ 여기 추가
    BackHandler {
        if (index > 0) {
            index -= 1          // 이전 문항으로
        } else {
            onBackClick()       // 첫 문항이면 Flow에서 알럿 띄우기
        }
    }

    val q = questions.getOrNull(index)

    Column(Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 48.dp)) {
        Spacer(Modifier.height(48.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // 첫 문제가 아니면 < 가 "이전 문항" 역할, 첫 문제면 onBackClick
            BackChevron(onClick = {
                if (index > 0) index -= 1 else onBackClick()
            })
        }

        Spacer(Modifier.height(20.dp))

        // 진행바
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
                text = q?.text.orEmpty(),
                fontFamily = Pretendard, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                color = Color.Black, lineHeight = 33.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // 선택 렌더링 (번호는 안 보여줌)
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
        Button(
            onClick = {
                if (index < questions.lastIndex) {
                    index += 1
                } else {
                    // 섹션 끝 → 일괄 채점
                    val correctCount = questions.indices.count { i ->
                        selections[i] == questions[i].answerOptionId
                    }
                    val tier = when (correctCount) {
                        in 7..9 -> VocabularyTier.상
                        in 4..6 -> VocabularyTier.중
                        else -> VocabularyTier.하
                    }
                    onFinishVocabulary(tier, correctCount)
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
            Text("정답 제출", fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}


//@Composable
//fun NicknameTestMcqScreen(
//    questions: List<McqQuestion>,
//    answeredGlobalCount: Int,
//    onBackClick: () -> Unit = {},
//    onSubmitAnswer: (questionId: Int, selectedOptionId: Int, isCorrect: Boolean) -> Unit = { _,_,_ -> },
//    onFinishVocabulary: (tier: VocabularyTier, correctCount: Int) -> Unit = { _, _ -> }
//) {
//    var index by remember { mutableStateOf(0) }
//    var selectedId by remember { mutableStateOf<Int?>(null) }
//    var correctCount by remember { mutableStateOf(0) }
//    val q = questions.getOrNull(index)
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(start = 20.dp, end = 20.dp, bottom = 48.dp)
//    ) {
//        Spacer(Modifier.height(48.dp))
//
//        // 상단 Back (정확히 좌 20dp 지점)
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            BackChevron(onClick = onBackClick)
//        }
//
//        Spacer(Modifier.height(20.dp))
//
//        // === Progress (전체 18문항 기준) ===
//        val totalQuestions = 18
//        val solved = answeredGlobalCount + index
//        val progress = solved.toFloat() / totalQuestions.toFloat()
//
//        ProgressBarLarge(
//            progress = progress,
//            trackColor = TrackGray,
//            progressColor = BrandBlue,
//            height = 10.dp
//        )
//
//        // 본문 영역은 좌우 6dp 추가 인셋
//        Column(modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 6.dp)) {
//
//            Spacer(Modifier.height(42.dp))
//
//            // Q 번호 (가운데)
//            Text(
//                text = q?.numberLabel ?: "",
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
//            // 문제 텍스트
//            Text(
//                text = q?.text.orEmpty(),
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
//            // 4지선다
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
//        Button(
//            onClick = {
//                val sel = selectedId ?: return@Button
//                val correct = (sel == q!!.answerOptionId)
//                if (correct) correctCount += 1
//                onSubmitAnswer(q.id, sel, correct)
//
//                if (index < questions.lastIndex) {
//                    index += 1
//                    selectedId = null
//                } else {
//                    val tier = when (correctCount) {
//                        in 7..9 -> VocabularyTier.상
//                        in 4..6 -> VocabularyTier.중
//                        else -> VocabularyTier.하
//                    }
//                    onFinishVocabulary(tier, correctCount)
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
//                .padding(horizontal = 80.dp)   // 좌우 40만큼 여백        // 가로폭을 이전 대비 1/3
//                .height(48.dp)
//                .align(Alignment.CenterHorizontally) // 가운데 정렬
//        ) {
//            Text(
//                text = "정답 제출",
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//        // 하단 48dp는 Column padding으로 보장
//    }
//}

@Composable
private fun BackChevron(onClick: () -> Unit) {
    // 내부 패딩 없이 정확히 좌 20dp에서 아이콘이 보이도록
    Box(
        modifier = Modifier
            .size(24.dp)                // 아이콘 시각 크기
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.Icon(
            painter = painterResource(id = MyPageR.drawable.ic_back),
            contentDescription = "뒤로가기",
            tint = Color.Unspecified
        )
    }
}

// ==== Components ====
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

// ==== Preview ====
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun PreviewNicknameTestMcqScreen() {
//    val sample = List(9) { i ->
//        McqQuestion(
//            id = i + 1,
//            numberLabel = "Q${i + 1}",
//            text = "다른 사람의 감정을 이해 및 공감하는 능력을 뜻하는 단어는?",
//            options = listOf(
//                McqOption(1, "공감"),
//                McqOption(2, "직관"),
//                McqOption(3, "분석"),
//                McqOption(4, "판단")
//            ),
//            answerOptionId = 1
//        )
//    }
//    MaterialTheme {
//        Surface {
//            NicknameTestMcqScreen(
//                questions = sample,
//                answeredGlobalCount = 3,
//                onBackClick = {},
//                onSubmitAnswer = { _,_,_ -> },
//                onFinishVocabulary = { _, _ -> }
//            )
//        }
//    }
//}
