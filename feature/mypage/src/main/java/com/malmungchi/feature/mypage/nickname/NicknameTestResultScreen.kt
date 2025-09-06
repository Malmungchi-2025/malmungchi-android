package com.malmungchi.feature.mypage.nickname


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

@Composable
fun NicknameTestResultScreen(
    nickname: String,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp) // 좌우 20
    ) {
        // 중앙 별명 카드 (임시 플레이스홀더)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp) // 상단 살짝 여유
                .align(Alignment.TopCenter)
                .heightIn(min = 280.dp) // 적당한 카드 높이
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF0F4FA)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = nickname, // "해당 별명 카드" → 실제 별명 텍스트
                fontFamily = Pretendard,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }

        // 하단 버튼
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF195FCF)
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF195FCF))
                ),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) {
                Text(
                    "다시하기",
                    fontSize = 16.sp,
                    fontFamily = Pretendard
                )
            }

            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF195FCF)
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) {
                Text(
                    "나가기",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewNicknameTestResultScreen() {
    MaterialTheme {
        Surface {
            NicknameTestResultScreen(
                nickname = "언어연금술사",
                onRetry = {},
                onExit = {}
            )
        }
    }
}