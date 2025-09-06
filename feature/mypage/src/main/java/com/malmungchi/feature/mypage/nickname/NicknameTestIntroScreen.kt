package com.malmungchi.feature.mypage.nickname

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import kotlinx.coroutines.delay
import com.malmungchi.feature.mypage.R as MyPageR

// ===== Colors =====
private val BrandBlue = Color(0xFF195FCF)
private val Gray_989898 = Color(0xFF989898)

/**
 * # 별명 테스트 - 인트로
 *
 * - 제목: 24sp, Pretendard, SemiBold, 195FCF
 * - 본문: 16sp, Pretendard, Medium, Black, 줄간격 150% (lineHeight = 24.sp)
 * - 버튼: "시작하기" 16sp, Pretendard, SemiBold, White
 */
@Composable
fun NicknameTestIntroScreen(
    userName: String = "사용자명",
    onBackClick: () -> Unit = {},
    onStartClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
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
                    Icon(
                        painter = painterResource(id = MyPageR.drawable.ic_back),
                        contentDescription = "뒤로가기",
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = "별명테스트를 시작할까요?",
                fontFamily = Pretendard,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "별명테스트를 통해 ${userName}님만의\n별명을 부여받고 귀여운 캐릭터 카드를 얻으세요!",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(Modifier.weight(1f))
        }

        // 👇 바닥에서 48dp 위에 고정
        Button(
            onClick = onStartClick,
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .height(48.dp)
                .width(200.dp)
        ) {
            Text(
                text = "시작하기",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}
/**
 * # 별명 테스트 - 로딩
 *
 * - 상단 back 아이콘 유지
 * - 가운데 일러스트: ing_nickname_loading (프로그래스+캐릭터 통합 이미지)
 * - 타이틀: "문제 로딩 중 ···" 22sp, Pretendard, SemiBold, Black
 * - 서브: 12sp, Pretendard, Medium, #989898
 */
@Composable
fun NicknameTestLoadingScreen(
    onBackClick: () -> Unit,
    onNavigateNext: () -> Unit   // ✅ 추가
) {
    // 로딩이 끝나면 자동 진입 (원하는 시간으로 조절)
    LaunchedEffect(Unit) {
        delay(1200)
        onNavigateNext()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
                Icon(
                    painter = painterResource(id = MyPageR.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Image(
            painter = painterResource(id = MyPageR.drawable.img_nickname_loading),
            contentDescription = "문제 로딩 일러스트",
            modifier = Modifier
                .padding(top = 8.dp)
                .size(200.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "문제 로딩 중 ···",
            fontFamily = Pretendard,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "말뭉치 웹에서는 정해진 글감으로 나만의 글쓰기가 가능해요 :)",
            fontFamily = Pretendard,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Gray_989898,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.weight(1f))
    }
}

// ===== Previews =====
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNicknameTestIntroScreen() {
    MaterialTheme {
        Surface { NicknameTestIntroScreen(userName = "말뭉치") }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNicknameTestLoadingScreen() {
    MaterialTheme {
        Surface {
            NicknameTestLoadingScreen(
                onBackClick = {},
                onNavigateNext = {}
            )
        }
    }
}
