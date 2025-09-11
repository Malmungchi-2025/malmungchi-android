package com.malmungchi.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

@Composable
fun QuizRetryIntroScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "틀린 문제를 다시 풀어볼까요?",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold, // ✅ SemiBold
                fontSize = 22.sp,                 // ✅ 22
                color = Color.Black
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "문제를 천천히 읽고 다시 풀어보세요!",
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,   // ✅ Medium
                fontSize = 18.sp,                 // ✅ 18
                color = Color(0xFF989898)         // ✅ #989898
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable private fun PreviewQuizRetryIntroScreen() {
    MaterialTheme {
        Surface(color = Color.White) {
            QuizRetryIntroScreen()
        }
    }
}
