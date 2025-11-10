package com.malmungchi.feature.login


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

private val BrandBlue = Color(0xFF195FCF)
private val Gray989898 = Color(0xFF989898)
private val ScreenPadding = 20.dp

///enum class VocabLevel { BASIC, PRACTICAL, ADVANCED, EXPERT }

@Composable
fun LevelTestIntroScreen(
    onBackClick: () -> Unit = {},
    onLevelChosen: (VocabLevel) -> Unit = {},
    isSubmitting: Boolean = false  // 🔵 추가: /levels/start 진행 중 로딩 상태
) {
    var selected by remember { mutableStateOf<VocabLevel?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(48.dp))

        // 상단바 (뒤로가기)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }

            Text(
                text = "수준설정",
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        // 부제: 어휘력·문해력 실력을 선택해주세요
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = BrandBlue)) { append("어휘력·문해력") }
                append(" 실력을\n선택해주세요")
            },
            fontFamily = Pretendard,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )

        Spacer(Modifier.height(16.dp))

        // 카드 목록
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            LevelCard(
                title = "기초 어휘",
                description1 = "가장 기본적인 단계예요",
                description2 = "짧고 쉬운 문장은 알지만, 조금만 길어지면 헷갈려요",
                selected = selected == VocabLevel.BASIC,
                onClick = { selected = VocabLevel.BASIC }
            )
            LevelCard(
                title = "실용 어휘",
                description1 = "일상 대화는 잘해요",
                description2 = "하지만 뉴스 기사나 보고서 문장은 어렵게 느껴져요",
                selected = selected == VocabLevel.PRACTICAL,
                onClick = { selected = VocabLevel.PRACTICAL }
            )
            LevelCard(
                title = "심화 어휘",
                description1 = "공적인 문서나 대화는 잘 이해해요",
                description2 = "하지만 분석이나 논리적인 글은 따라가기 힘들어요",
                selected = selected == VocabLevel.ADVANCED,
                onClick = { selected = VocabLevel.ADVANCED }
            )
            LevelCard(
                title = "고급 어휘",
                description1 = "전문적인 글이나 사회 문제 글도 읽을 수 있어요",
                description2 = "하지만 비판적으로 이해해 해석하는 건 아직 어려워요",
                selected = selected == VocabLevel.EXPERT,
                onClick = { selected = VocabLevel.EXPERT }
            )

            Spacer(Modifier.height(12.dp))
        }

        // 하단 버튼 (가운데 정렬, 텍스트: 선택하기)
        Button(
            onClick = { selected?.let(onLevelChosen) },
            enabled = selected != null && !isSubmitting,           // 🔵 로딩 중 비활성화
//            enabled = selected != null,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .align(Alignment.CenterHorizontally) // 가운데 정렬
                .padding(bottom = 48.dp)             // ⬅️ 아래에서 48dp 띄우기
                .height(48.dp)                       // 높이 유지
                .width(140.dp)                       // 폭만 넓히기 (원하는 값으로 조정)
        ) {
            Text(
                text = "선택하기",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LevelCard(
    title: String,
    description1: String,
    description2: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val outline = if (selected) BrandBlue else Color(0x14000000)
    val elevation = if (selected) 4.dp else 2.dp

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFD)),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = BorderStroke(1.dp, outline),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontFamily = Pretendard,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = description1,
                fontFamily = Pretendard,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Gray989898
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description2,
                fontFamily = Pretendard,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Gray989898
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewLevelTestIntroScreen() {
    MaterialTheme { Surface { LevelTestIntroScreen() } }
}
