package com.malmungchi.feature.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

private val BrandBlue = Color(0xFF195FCF)

@Composable
fun LevelTestStartScreen(
    onBackClick: () -> Unit = {},
    onStartClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        // 상단 바 (뒤로가기)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                // 프로젝트의 뒤로가기 아이콘으로 교체하세요.
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }
        }

        // 가운데 콘텐츠
        Spacer(Modifier.weight(1f))

        Text(
            text = "말뭉치 첫 여정 시작!",
            fontFamily = Pretendard,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandBlue,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "당신의 언어 수준을 확인해볼까요?",
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        // 하단 버튼
        Button(
            onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = MaterialTheme.shapes.extraLarge, // 충분히 둥글게
            modifier = Modifier
                .padding(bottom = 32.dp)
                .height(48.dp)
                .width(200.dp) // 스크린샷처럼 적당한 폭으로 고정 (원하면 fillMaxWidth().padding(horizontal = 24.dp)로 변경)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "여정 떠나기",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewLevelTestStartScreen() {
    MaterialTheme {
        Surface { LevelTestStartScreen() }
    }
}