package com.malmungchi.feature.mypage.nickname


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.malmungchi.feature.mypage.R as MyPageR


@Composable
fun NicknameTestResultScreen(
    userName: String? = null,   // ✅ 기본값 추가
    nickname: String?,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // 별명(=이미지 리소스)이 준비될 때만 카드 렌더링
        val imgRes = getNicknameCardImageResOrNull(nickname)
        if (imgRes != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 80.dp)
                    .height(600.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.TopCenter)
            ) {
                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = "별명 카드 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                if (!userName.isNullOrBlank()) {
                    Text(
                        text = "$userName 님의 별명은",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 24.dp)
                    )
                }
            }
        } else {
            // ← 로딩/스켈레톤 (원하면 다른 UI로 교체 가능)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .padding(horizontal = 20.dp)
                    .padding(top = 80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFF5F6F9)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // 버튼 바: 하단에서 48dp 띄우고, 시스템 내비게이션 인셋도 확보
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()               // ← 시스템바 피하기
                .padding(start = 20.dp, end = 20.dp, bottom = 48.dp) // ← 아래로 48
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF195FCF))
                ),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) { Text("다시하기", fontSize = 16.sp, fontFamily = Pretendard) }

            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) {
                Text(
                    "나가기",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}


/** 별명 → 이미지 리소스 매핑 (NicknameCardScreen과 동일) */
private fun getNicknameCardImageResOrNull(nickname: String?): Int? = when (nickname) {
    "언어연금술사" -> MyPageR.drawable.img_word_magician
    "눈치번역가"  -> MyPageR.drawable.img_sense
    "감각해석가"  -> MyPageR.drawable.img_sense2
    "맥락추리자"  -> MyPageR.drawable.img_context
    "언어균형술사"-> MyPageR.drawable.img_language
    "낱말여행자"  -> MyPageR.drawable.img_word2
    "단어수집가"  -> MyPageR.drawable.img_word3
    "의미해석가"  -> MyPageR.drawable.img_context2
    "언어모험가"  -> MyPageR.drawable.img_language2
    else -> MyPageR.drawable.img_nickname_loading        // ← ★ 기본(프리뷰용) 이미지 리턴 금지
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNicknameTestResultScreen_Magician() {
    MaterialTheme {
        Surface {
            NicknameTestResultScreen(
                userName = "김뭉치",     // ← 추가
                nickname = "언어연금술사",
                onRetry = {},
                onExit = {}
            )
        }
    }
}