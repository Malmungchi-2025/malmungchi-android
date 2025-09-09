package com.malmungchi.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard


// 색상 상수: 195FCF
private val OnboardingBlue = Color(0xFF195FCF)

// 버튼 텍스트 스타일 (회원가입 플로우와 동일 톤)
private fun onboardingButtonText() = TextStyle(
    fontFamily = Pretendard,     // ← 디자인 시스템 폰트
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    color = Color.White
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    autoAdvanceMillis: Long = 1500L,
) {
    val images = remember {
        listOf(
            R.drawable.img_onboard1,
            R.drawable.img_onboard2,
            R.drawable.img_onboard3,
            R.drawable.img_onboard4,
            R.drawable.img_onboard5,
            R.drawable.img_onboard6,
            R.drawable.img_onboard7,
            R.drawable.img_onboard8,
            R.drawable.img_onboard9,
        )
    }



//    var index by remember { mutableStateOf(0) }
//    var isSkipping by remember { mutableStateOf(false) }   // ★ 추가
//
//    LaunchedEffect(index) {
//        if (index < images.lastIndex) {
//            delay(autoAdvanceMillis)
//            index += 1
//        } else {
//            delay(autoAdvanceMillis)
//            onFinish()
//        }
//    }
    var index by remember { mutableStateOf(0) }
    var isSkipping by remember { mutableStateOf(false) }

    // 자동 진행: skip 중이면 즉시 중단
    LaunchedEffect(index, isSkipping) {
        if (isSkipping) return@LaunchedEffect
        if (index < images.lastIndex) {
            delay(autoAdvanceMillis)
            if (!isSkipping) index += 1
        } else {
            delay(autoAdvanceMillis)
            if (!isSkipping) onFinish()
        }
    }


    // 탭 진행: 라벨(next@)을 붙여 조기 리턴
    val onNext: () -> Unit = next@{
        if (isSkipping) return@next
        if (index < images.lastIndex) index += 1 else onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // ripple 제거 (원치 않으면 빼도 됨)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onNext() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = images[index]),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            // ✅ 화면을 빈틈 없이 꽉 채움 (비율 유지, 가장자리 약간 크롭)
            contentScale = ContentScale.Crop
        )
        // 👉 Skip 버튼 (OTP "인증 완료" 버튼과 동일 스타일)
        Button(
            onClick = {
                isSkipping = true                   // ★ 자동/탭 진행 모두 차단
                onFinish()                          // ★ 곧바로 다음 화면으로
            },
            enabled = !isSkipping, //중복 클릭 방지
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp)     // 오른쪽 여백만
                .offset(y = 640.dp)
                .height(52.dp)
                .defaultMinSize(minWidth = 100.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OnboardingBlue,   // ← 교체
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text("Skip", style = onboardingButtonText()) // ← 교체
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onFinish = {})
}