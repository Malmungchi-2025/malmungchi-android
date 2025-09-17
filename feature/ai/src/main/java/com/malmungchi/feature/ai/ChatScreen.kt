package com.malmungchi.feature.ai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.ai.R

// ----------------------
// 말풍선(왼쪽 위만 직각, 나머지 라운드)
// ----------------------
@Composable
private fun ChatBubbleRectTopLeftSharp(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color(0xFFF7F7F7),
    corner: Dp = 16.dp
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val c = corner.toPx()

            // 몸통: 왼쪽 위만 0, 나머지 라운드
            // Compose Canvas엔 개별 코너 반경 지원이 없어 Path로 직접 그림
            val w = size.width
            val h = size.height

            val path = Path().apply {
                moveTo(0f, 0f)              // 좌상단(직각)
                lineTo(w - c, 0f)
                quadraticBezierTo(w, 0f, w, c) // 우상단 라운드
                lineTo(w, h - c)
                quadraticBezierTo(w, h, w - c, h) // 우하단 라운드
                lineTo(c, h)
                quadraticBezierTo(0f, h, 0f, h - c) // 좌하단 라운드
                lineTo(0f, 0f)             // 좌상단으로
                close()
            }
            drawPath(path = path, color = bgColor)
        }

        // 내용 패딩
        Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
            Text(
                text = text,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,   // Pretendard 16 Medium
                fontSize = 16.sp,
                color = Color(0xFF222222)
            )
        }
    }
}

@Composable
fun ChatScreen(
    onBack: () -> Unit = {},
    onMicClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3E9F3)) // 전체 배경: E3E9F3
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // 헤더: Back(48) + 제목(프리텐다드 20 세미볼드)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp), // Back 버튼 높이와 동일하게 맞춤
                contentAlignment = Alignment.Center
            ) {
                // Back 버튼 (왼쪽)
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.CenterStart) // 왼쪽 정렬
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back"
                    )
                }

                // 가운데 제목
                Text(
                    text = "취준생 맞춤 상황",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF222222),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 날짜: 흰색 배경의 둥근 배지
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(color = Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "2025.04.05",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 챗봇 말풍선
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_chatbot_malchi),
                    contentDescription = "Chatbot",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // 말풍선: 나머지 공간을 전부 차지하도록 weight(1f)
                ChatBubbleRectTopLeftSharp(
                    text = "[면접 상황]\n: 본인의 장단점이 무엇인가요?",
                    modifier = Modifier
                        .weight(1f) // <-- 이미지 빼고 남은 영역 꽉 차게
                        .wrapContentHeight()
                )
            }
        }

        // 마이크 버튼: 하단에서 48
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_chat_mike),
                contentDescription = "Mic",
                modifier = Modifier
                    .size(56.dp)
                    .clickable { onMicClick() }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
fun ChatScreenPreview() {
    ChatScreen()
}
