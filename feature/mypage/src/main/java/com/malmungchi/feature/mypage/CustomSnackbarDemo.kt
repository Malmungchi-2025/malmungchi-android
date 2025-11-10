package com.malmungchi.feature.mypage.ui.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import kotlinx.coroutines.launch

// ✅ 메인 컬러 정의
private val Blue_195FCF = Color(0xFF195FCF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSnackbarDemo() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            ) { data ->
                // ✅ 애니메이션 + 커스텀 카드 디자인
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = fadeOut()
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Blue_195FCF),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = data.visuals.message,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            style = TextStyle(
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar("리마인드 시간이 저장됐어요 💙")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue_195FCF)
            ) {
                Text("스낵바 보기", color = Color.White, fontFamily = Pretendard)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEFF4FB)
@Composable
fun PreviewCustomSnackbar() {
    MaterialTheme {
        CustomSnackbarDemo()
    }
}
