package com.malmungchi.feature.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.quiz.R

@Composable
fun QuizLoadingScreen(
    vm: QuizFlowViewModel,
    onBackToHome: () -> Unit,
    onReadyToSolve: () -> Unit
) {
    val ui by vm.ui.collectAsState()

    // 세트 로드되면 풀이로 전환
    LaunchedEffect(ui.loading, ui.current) {
        if (!ui.loading && ui.current != null) onReadyToSolve()
    }

    // 로딩 중 뒤로 누르면 카테고리로
    BackHandler(enabled = true) { onBackToHome() }

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
                .padding(top = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackToHome) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
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
                text = "${ui.headerTitle} 문제 로딩 중 ···",
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
