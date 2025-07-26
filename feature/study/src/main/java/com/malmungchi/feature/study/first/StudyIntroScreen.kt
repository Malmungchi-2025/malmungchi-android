package com.malmungchi.feature.study.first


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import kotlinx.coroutines.delay

@Composable
fun StudyIntroScreen(
    levelText: String = "1단계",
    onNavigateNext: () -> Unit = {} // ✅ 콜백으로 다음 화면 이동 처리
) {
    // 🔹 화면 진입 시 3초 후 자동 이동
    LaunchedEffect(Unit) {
        delay(3000) // 3초 대기
        onNavigateNext() // ✅ 다음 화면 콜백 실행
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
            Text(
                text = levelText,
                color = Color(0xFF3F51B5),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "글을 집중해서 읽으며,\n모르는 단어에 체크해 보세요!",
                color = Color(0xFF333333),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStudyIntroScreen() {
    StudyIntroScreen(levelText = "1단계")
}
