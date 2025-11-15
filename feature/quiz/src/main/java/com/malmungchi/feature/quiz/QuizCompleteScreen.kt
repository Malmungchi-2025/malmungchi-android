package com.malmungchi.feature.quiz


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.malmungchi.core.designsystem.Pretendard


// ✅ 완료 화면 UI
@Composable
fun QuizCompleteScreen(
    vm: QuizFlowViewModel? = null,
    onNextClick: () -> Unit = {},
    pretendard: FontFamily = FontFamily.Default, // 🎯 기본 폰트 fallback
    showImage: Boolean = true, // 🎯 Preview에서는 이미지 생략 가능

) {
    // ✅ 화면 진입 시 1회 자동 지급 (실제 뷰모델 있을 때만 동작)
    LaunchedEffect(Unit) {

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
                top = 32.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(64.dp))

        // 🎯 상단 텍스트
        Text(
            text = "퀴즈 한뭉치 완료!",
            fontSize = 24.sp,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF195FCF),
            textAlign = TextAlign.Center
        )

        // 🎯 캐릭터 이미지 & XP
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (showImage) {
                Image(
                    painter = painterResource(id = R.drawable.ic_complete_character),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp), // ✅ 1.5배 확대
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "15XP 획득",
                fontSize = 22.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // 🎯 하단 버튼
        Button(
            onClick = {
                // ⬇️ 종료하기 누르면 포인트 지급 → 성공 후 onNextClick()
                vm?.rewardCurrentAttempt(
                    onSuccess = { /* it.rewardPoint, it.totalPoint 사용 가능 */
                        onNextClick()
                    },
                    onError = {
                        // 실패해도 일단 다음으로 이동하고 싶다면:
                        onNextClick()
                        // 또는 Snackbar/Toast를 띄우고 멈추려면 여기서 처리
                    }
                ) ?: onNextClick()
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
            modifier = Modifier
                .fillMaxWidth(0.5f) // ✅ 폭을 부모의 절반으로
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 48.dp)
                .height(48.dp)
        ) {
            Text(
                text = "종료하기",
                fontSize = 16.sp,
                fontFamily = Pretendard,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewStudyCompleteScreen() {
    // Pretendard → 시스템 폰트 대체, Preview에서는 이미지 출력
    QuizCompleteScreen(
        pretendard = FontFamily.SansSerif,
        showImage = true,
        //viewModel = null // Preview에서는 뷰모델 null
    )
}