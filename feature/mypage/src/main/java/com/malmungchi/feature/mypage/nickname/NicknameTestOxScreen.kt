package com.malmungchi.feature.mypage.nickname

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList





// ===== Palette =====
private val BrandBlue = Color(0xFF195FCF)
private val TrackGray = Color(0xFFFAFAFA)
private val TextGray = Color(0xFF989898)

@Composable
fun NicknameTestOxScreen(
    questions: List<OxQuestion>,
    answeredGlobalCount: Int,
    onBackClick: () -> Unit = {},
    onFinishOx: (oxCorrectCount: Int) -> Unit = {},   // ✅ 변경: 총 정답수 전달
    initialIndex: Int = 0
) {
    var index by rememberSaveable { mutableStateOf(initialIndex) }
    // ✅ 안전한 초기화: 사이즈만큼 null로 채우기
    val selections = rememberSaveable(
        questions,
        saver = listSaver<SnapshotStateList<Boolean?>, Boolean?>(
            save = { it.toList() },              // Bundle에 들어갈 수 있는 List<Boolean?>로 저장
            restore = { it.toMutableStateList() } // 다시 SnapshotStateList로 복원
        )
    ) {
        MutableList(questions.size) { null }.toMutableStateList()
    }

    // ⬇️ 여기 추가
    BackHandler {
        if (index > 0) {
            index -= 1
        } else {
            onBackClick()
        }
    }
    //val selections = rememberSaveable(questions) { mutableStateListOf<Boolean?>( *Array(questions.size){ null } ) }

    val q = questions.getOrNull(index)

    Column(Modifier
        .fillMaxSize()
        .background(Color.White)
        .padding(start = 20.dp, end = 20.dp, bottom = 48.dp)) {
        Spacer(Modifier.height(48.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackChevron(onClick = {
                if (index > 0) index -= 1 else onBackClick()
            })
        }

        Spacer(Modifier.height(42.dp))

        val totalQuestions = 18
        val solved = answeredGlobalCount + index
        val progress = solved.toFloat() / totalQuestions.toFloat()
        ProgressBarLarge(progress, Color(0xFFFAFAFA), Color(0xFF195FCF), 10.dp)

        Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
            Spacer(Modifier.height(32.dp))
            Text(
                text = q?.numberLabel.orEmpty(),
                fontFamily = Pretendard, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "지문을 읽고 문장이 맞으면 O, 틀리면 X를 선택하세요.",
                fontFamily = Pretendard, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
                color = Color.Black, lineHeight = 33.sp, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            StatementCard(text = q?.statement.orEmpty(), modifier = Modifier.padding(horizontal = 12.dp))
            Spacer(Modifier.height(32.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OxOption(label = "O", selected = selections[index] == true, modifier = Modifier.weight(1f)) {
                    selections[index] = true
                }
                OxOption(label = "X", selected = selections[index] == false, modifier = Modifier.weight(1f)) {
                    selections[index] = false
                }
            }
        }

        Spacer(Modifier.weight(1f))

        val enabled = selections[index] != null
        Button(
            onClick = {
                if (index < questions.lastIndex) {
                    index += 1
                } else {
                    val correctCount = questions.indices.count { i ->
                        selections[i] == questions[i].answerIsO
                    }
                    onFinishOx(correctCount)
                }
            },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) Color(0xFF195FCF) else Color(0xFFFAFAFA),
                contentColor = if (enabled) Color.White else Color(0xFF989898)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 80.dp).height(48.dp).align(Alignment.CenterHorizontally)
        ) {
            Text("정답 제출", fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}



// ===== 작은 부품들 =====

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
private fun OxOption(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = if (selected) BrandBlue else Color(0xFFE0E0E0)
    val bg = if (selected) BrandBlue.copy(alpha = 0.2f) else Color.White

    Box(
        modifier = modifier
            .aspectRatio(1f) // 👈 가로 = 세로 비율 고정
            .height(120.dp)
            .clip(shape)
            .background(bg)
            .border(2.dp, borderColor, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontSize = 44.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

// ===== 공용 컴포넌트 =====
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNicknameTestOxScreen() {
    val sample = listOf(
        OxQuestion(
            id = 101,
            numberLabel = "Q10",
            statement = "지진이 발생하면 건물 붕괴뿐만 아니라 화재와 가스 누출 같은 2차 피해도 발생할 수 있다.\n" +
                    "따라서 지진 대비 훈련에서는 단순히 대피 방법뿐만 아니라 화재 예방 조치도 포함되어야 한다.",
            answerIsO = true
        ),
        OxQuestion(
            id = 102,
            numberLabel = "Q11",
            statement = "지진 대비 훈련은 연 1회만 해도 충분하므로 평상시 대피 요령을 복습할 필요가 없다.",
            answerIsO = false
        )
    )

    MaterialTheme {
        Surface(color = Color.White) {
            NicknameTestOxScreen(
                questions = sample,
                answeredGlobalCount = 9, // 앞의 섹션에서 9문항 푼 상태 가정
                onBackClick = {},
                onFinishOx = { /* 미리보기: 아무 작업 없음 */ },
                initialIndex = 0
            )
        }
    }
}


