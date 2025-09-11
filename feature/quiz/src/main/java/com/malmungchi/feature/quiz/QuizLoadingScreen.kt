package com.malmungchi.feature.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import com.malmungchi.feature.quiz.R

@Composable
fun QuizLoadingScreen(
    category: QuizCategory,
    onBack: () -> Unit,
    onLoaded: (QuizCategory) -> Unit
) {
    // 👉 서버 연동 시 여기서 API 호출하고, 성공 시 onLoaded(category) 호출하면 됨
    LaunchedEffect(category) {
        delay(1200) // 오늘은 그냥 로딩 느낌만
        onLoaded(category)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        // 상단 뒤로가기
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back), // 없으면 다른 back 아이콘으로 교체
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                text = "${category.displayName} 문제 로딩 중 ···",
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "말뭉치 웹에서는 정해진 글감으로 나만의 글쓰기가 가능해요 :)",
                fontFamily = Pretendard,
                fontSize = 14.sp,
                color = Color(0xFF989898)
            )
        }


    }
}
