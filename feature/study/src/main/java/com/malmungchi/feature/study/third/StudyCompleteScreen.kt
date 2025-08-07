package com.malmungchi.feature.study.third

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R

// ✅ 완료 화면 UI
@Composable
fun StudyCompleteScreen(
    onNextClick: () -> Unit = {},
    pretendard: FontFamily = FontFamily.Default, // 🎯 기본 폰트로 fallback 처리
    showImage: Boolean = true // 🎯 Preview에서는 이미지 생략 가능
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // 🎯 상단 텍스트
        Text(
            text = "오늘의 학습 완료!",
            fontSize = 24.sp,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            color = Color(0x195FCF),
            textAlign = TextAlign.Center
        )

        // 🎯 캐릭터 이미지 & XP
        Column(
            modifier = Modifier.padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_complete_character), // 🔁 실제 PNG로 교체
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "25XP 획득",
                fontSize = 22.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // 🎯 하단 버튼
        Button(
            onClick = onNextClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .height(48.dp)
        ) {
            Text(
                text = "다음 단계",
                fontSize = 16.sp,
                fontFamily = Pretendard,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStudyCompleteScreen() {
    // ❗ Pretendard → 시스템 폰트 대체 / 이미지 X
    StudyCompleteScreen(
        pretendard = FontFamily.SansSerif,
        showImage = false
    )
}