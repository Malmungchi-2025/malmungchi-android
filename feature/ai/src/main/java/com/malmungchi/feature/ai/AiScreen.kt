package com.malmungchi.feature.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

// ===== Colors =====
private val Blue_195FCF = Color(0xFF195FCF)
private val Gray_F7F7F7 = Color(0xFFF7F7F7)
private val Gray_EFF4FB = Color(0xFFEFF4FB)

private val ScreenPadding = 20.dp

@Composable
fun AiScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onStartAiChat: () -> Unit = {},
    onFreeChat: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(48.dp))

        // Top Title
        Text(
            text = "AI 대화연습",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        )

        Spacer(Modifier.height(40.dp))

        // Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp), // ← 카드 키우기 (원하면 380~400dp로 조정)
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Gray_F7F7F7),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "취준생 맞춤 상황",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Blue_195FCF
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "자소서, 면접 질문 등\n취준생 맞춤 AI 상대와 대화해 보세요",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onStartAiChat,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)   // 전체 폭의 50%만 사용
                            .align(Alignment.Start), // 왼쪽 정렬
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Blue_195FCF,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            "AI 대화 시작하기",
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // 혹은
        Text(
            text = "혹은",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Blue_195FCF
            )
        )

        Spacer(Modifier.height(16.dp))

        // 자유롭게 대화하기 (반폭 + 가운데)
        Button(
            onClick = onFreeChat,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = Gray_EFF4FB,
                contentColor = Blue_195FCF
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                "자유롭게 대화하기",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Blue_195FCF
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AiScreenPreview() {
    MaterialTheme { AiScreen() }
}
