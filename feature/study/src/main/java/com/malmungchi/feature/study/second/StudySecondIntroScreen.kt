package com.malmungchi.feature.study.second

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.feature.study.Pretendard
import kotlinx.coroutines.delay

@Composable
fun StudySecondIntroScreen(
    onNavigateNext: () -> Unit = {}
) {
    // ✅ 3초 후 자동으로 다음 화면으로 이동
    LaunchedEffect(Unit) {
        delay(3000)
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 🔹 단계 표시
            Text(
                text = "2단계",
                color = Color(0xFF195FCF),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 설명 문구
            Text(
                text = "읽었던 글에 대한 필사를 진행하며,\n다시 한 번 들어다보세요!",
                color = Color(0xFF333333),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStudySecondIntroScreen() {
    StudySecondIntroScreen()
}
