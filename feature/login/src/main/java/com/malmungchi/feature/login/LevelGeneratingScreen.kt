package com.malmungchi.feature.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

// ──────────────────────────────────────────────────────────────
// 로딩 화면: 선택한 수준에 맞는 오늘의 글을 생성중…
// ──────────────────────────────────────────────────────────────

private val BrandBlue = Color(0xFF195FCF)
private val ScreenPadding = 20.dp

@Composable
fun LevelGeneratingScreen(
    progressPercent: Int,          // API 연동 시 전달되는 퍼센트 (0..100)
    onCancel: (() -> Unit)? = null // 필요하면 취소 콜백
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center   // ✅ 화면 세로 중앙 배치
    ) {
        // 상단 타이틀 (가운데 정렬)
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = BrandBlue)) { append("선택한 수준") }
                append("에 맞는\n오늘의 글을 생성하고 있어요")
            },
            fontFamily = Pretendard,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // 서브 텍스트 (가운데 정렬)
        Text(
            text = "잠시만 기다려주세요!",
            fontFamily = Pretendard,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        // ⬇️ 타이틀/부타이틀과 프로그레스바 사이 24dp 고정
        Spacer(Modifier.height(24.dp))

        // 진행 표시 (얇은 바 + 바 아래 퍼센트)
        ProgressBarWithPercent(
            percent = progressPercent.coerceIn(0, 100),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        // 필요 시 하단 요소가 있다면 아래에 추가
        if (onCancel != null) {
            // TextButton(onClick = onCancel) { Text("진행 취소") }
        }
    }
}

/**
 * 커스텀 프로그레스바:
 * - 배경: 흰색
 * - 테두리: 옅은 회색
 * - 채움: BrandBlue, 진행률에 따라 가로폭 증가
 * - 퍼센트 텍스트는 바 "아래"에 표시 (Pretendard, Medium, 16sp, BrandBlue)
 */
@Composable
private fun ProgressBarWithPercent(
    percent: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 얇은 프로그레스바
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color(0xFFE5E5E5)),
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp) // 🔹 얇게
                .clip(RoundedCornerShape(6.dp))
        ) {
            // 배경(흰색) 위에 파란색 진행 막대
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percent / 100f)
                        .background(BrandBlue)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 바 아래 퍼센트
        Text(
            text = "$percent%",
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = BrandBlue,
            textAlign = TextAlign.Center
        )
    }
}

// ──────────────────────────────────────────────────────────────
// Preview
// ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewLevelGeneratingScreen50() {
    MaterialTheme { Surface { LevelGeneratingScreen(progressPercent = 50) } }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewLevelGeneratingScreen10() {
    MaterialTheme { Surface { LevelGeneratingScreen(progressPercent = 10) } }
}
