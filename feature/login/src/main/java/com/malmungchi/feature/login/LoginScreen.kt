package com.malmungchi.feature.login
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.login.R

private val BrandBlue = Color(0xFF195FCF)
private val GAP = 16.dp

private val TitlePushDown = 96.dp

private val EmailButtonHeight = 52.dp
private val EmailButtonCorner = 14.dp

private val SocialIconGlyphSize = 64.dp

private val Char1Size = 304.dp
private val Char1OffsetX = (-40).dp
private val Char1OffsetY = 1.dp
private val Char1RotationDeg = 0f

private val Char2Size = 160.dp
private val Char2OffsetX = 28.dp
private val Char2OffsetY = 16.dp
private val Char2RotationDeg = 0f

private val Char1Alpha = 0.95f
private val Char2Alpha = 1.0f

private val SOCIAL_BUTTON_SIZE = 80.dp   // 버튼 정사각 크기(원하면 84.dp까지)
private val SOCIAL_GAP = 10.dp           // 아이콘 간격
private val SOCIAL_ICON_SCALE = 1.06f    // 아이콘 확대(살짝만 키움, 잘림 방지)



//private val SOCIAL_BUTTON_SIZE = 84.dp   // 버튼 박스 정사각형
//private val SOCIAL_ICON_SCALE = 0.7f    // 이미지가 박스 안에서 차지할 비율 (0.7 ~ 0.8 적당)

@Composable
fun LoginScreen(
    onEmailLogin: () -> Unit,
    onKakao: () -> Unit = {},
    onNaver: () -> Unit = {},
    onGoogle: () -> Unit = {},
    onSignUp: () -> Unit = {},
    onResetPassword: () -> Unit = {},   // 비밀번호 재설정
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlue)
            .systemBarsPadding()
            .padding(horizontal = GAP)
    ) {
        // 상단/우측 캐릭터
        Image(
            painter = painterResource(R.drawable.img_char1),
            contentDescription = null,
            modifier = Modifier
                .size(Char1Size)
                .offset(x = Char1OffsetX, y = Char1OffsetY)
                .rotate(Char1RotationDeg)
                .align(Alignment.TopStart)
                .alpha(Char1Alpha),
            contentScale = ContentScale.Fit
        )
        Image(
            painter = painterResource(R.drawable.img_char2),
            contentDescription = null,
            modifier = Modifier
                .size(Char2Size)
                .align(Alignment.CenterEnd)
                .offset(x = Char2OffsetX, y = Char2OffsetY)
                .rotate(Char2RotationDeg)
                .alpha(Char2Alpha),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(TitlePushDown + 154.dp))

            Text(
                text = "말뭉치,\n언어의 힘을 기르는\n공간",
                color = Color.White,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 36.sp
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(Modifier.weight(1f))

            // 하단 블록
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(top = 32.dp, bottom = 80.dp), // ▼ 전체 영역을 32dp 아래로
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(72.dp))  // 👈 버튼을 32dp 밑으로 내림
                // 이메일로 시작하기 → 로그인 진입
                Button(
                    onClick = onEmailLogin,
                    shape = RoundedCornerShape(EmailButtonCorner),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(EmailButtonHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BrandBlue
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        "✉",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                    Spacer(Modifier.width(GAP))
                    Text(
                        "이메일로 시작하기",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // 링크: 비밀번호 재설정 | 회원가입 (Pretendard 16, Medium)
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "비밀번호 재설정",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .padding(6.dp)
                            .clickable { onResetPassword() }
                    )
                    Text(
                        text = " | ",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                    Text(
                        text = "회원가입",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .padding(6.dp)
                            .clickable { onSignUp() }
                    )
                }

                // 구분선 있는 "3초만에 시작하기"
                Spacer(Modifier.height(GAP))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .alpha(0.4f)
                            .background(Color.White)
                    )
                    Spacer(Modifier.width(36.dp))
                    Text(
                        text = "3초만에 시작하기",
                        color = Color.White.copy(alpha = 0.95f),
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,               // 16, 미디엄
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .alpha(0.4f)
                            .background(Color.White)
                    )
                }
                Spacer(Modifier.height(12.dp))

                // ===== 소셜 아이콘 묶음 =====
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(SOCIAL_GAP),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialIcon(R.drawable.ic_kakao) { onKakao() }
                    SocialIcon(R.drawable.ic_naver) { onNaver() }
                }

                //Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ===== 아이콘 컴포저블 =====
@Composable
private fun SocialIcon(
    iconRes: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(SOCIAL_BUTTON_SIZE)      // 정사각 버튼 히트영역
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = SOCIAL_ICON_SCALE   // 살짝 확대
                    scaleY = SOCIAL_ICON_SCALE
                    // clip = false (기본값)  ✅ 바깥으로 나가도 자르지 않음 → 안 잘림
                },
            contentScale = ContentScale.Fit      // 비율 유지하며 박스 안에 맞춤(잘리지 않음)
        )
    }
}
@Preview(showBackground = true, showSystemUi = true, name = "LoginScreenPreview")
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            onEmailLogin = {},
            onKakao = {},
            onNaver = {},
            onGoogle = {},
            onSignUp = {},
            onResetPassword = {}
        )
    }
}

//private val BrandBlue = Color(0xFF195FCF)
//private val GAP = 16.dp
//
//private val TopLabelTopPadding = GAP
//private val TitlePushDown = 96.dp
//private val BottomBlockOffsetY = (-112).dp
//
//private val EmailButtonHeight = 52.dp
//private val EmailButtonCorner = 14.dp
//
//private val SocialIconGlyphSize = 64.dp   // 아이콘 크기(모두 동일)
//
//private val Char1Size = 304.dp
//private val Char1OffsetX = (-40).dp
//private val Char1OffsetY = 1.dp
//private val Char1RotationDeg = 0f
//
//private val Char2Size = 160.dp
//private val Char2OffsetX = 28.dp
//private val Char2OffsetY = 16.dp
//private val Char2RotationDeg = 0f
//
//private val Char1Alpha = 0.95f
//private val Char2Alpha = 1.0f
//
//@Composable
//fun LoginScreen(
//    onEmailLogin: () -> Unit,
//    onKakao: () -> Unit = {},
//    onNaver: () -> Unit = {},
//    onGoogle: () -> Unit = {},
//    onSignUp: () -> Unit = {}
//) {
//    val ctx = LocalContext.current
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(BrandBlue)
//            .systemBarsPadding()
//    ) {
//        Image(
//            painter = painterResource(R.drawable.img_char1),
//            contentDescription = null,
//            modifier = Modifier
//                .size(Char1Size)
//                .offset(x = Char1OffsetX, y = Char1OffsetY)
//                .rotate(Char1RotationDeg)
//                .align(Alignment.TopStart)
//                .alpha(Char1Alpha),
//            contentScale = ContentScale.Fit
//        )
//
//        Image(
//            painter = painterResource(R.drawable.img_char2),
//            contentDescription = null,
//            modifier = Modifier
//                .size(Char2Size)
//                .align(Alignment.CenterEnd)
//                .offset(x = Char2OffsetX, y = Char2OffsetY)
//                .rotate(Char2RotationDeg)
//                .alpha(Char2Alpha),
//            contentScale = ContentScale.Fit
//        )
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = GAP),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(Modifier.height(TopLabelTopPadding))
//
//            Spacer(Modifier.height(TitlePushDown + 154.dp))
//
//            Text(
//                text = "말뭉치,\n언어의 힘을 기르는\n공간",
//                color = Color.White,
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    lineHeight = 36.sp
//                ),
//                modifier = Modifier.align(Alignment.Start)
//            )
//
//            Spacer(Modifier.weight(1f))
//
//            // 2) 아래 블록의 offset 제거
//            Column(
//                // modifier = Modifier.offset(y = BottomBlockOffsetY),
//                modifier = Modifier
//                    .navigationBarsPadding()   // 소프트키 높이만큼 자동 여백
//                    .padding(bottom = 80.dp),  // 필요하면 여백 더 주기 (예: 24 → 190 조절)
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                // 버튼: 아이콘/텍스트 간격 조절은 여기서
//                Button(
//                    onClick = onSignUp,
//                    shape = RoundedCornerShape(EmailButtonCorner),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(EmailButtonHeight),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color.White,
//                        contentColor = BrandBlue
//                    ),
//                    elevation = ButtonDefaults.buttonElevation(0.dp)
//                ) {
//                    Text(
//                        "✉",
//                        style = TextStyle(
//                            fontFamily = Pretendard,
//                            fontSize = 20.sp,
//                            fontWeight = FontWeight.ExtraBold
//                        ),
//                        modifier = Modifier.offset(y = (-2).dp) // 아이콘만 살짝 위로
//                    )
//                    Spacer(Modifier.width(GAP))
//                    Text(
//                        "이메일로 시작하기",
//                        style = TextStyle(
//                            fontFamily = Pretendard,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    )
//                }
//
//                Spacer(Modifier.height(GAP))
//                Text(
//                    text = "3초만에 시작하기",
//                    color = Color.White.copy(alpha = 0.95f),
//                    style = TextStyle(
//                        fontFamily = Pretendard,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                )
//
//                Spacer(Modifier.height(GAP))
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(GAP, Alignment.CenterHorizontally),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    SocialIcon(R.drawable.ic_kakao) { onKakao() }
//                    SocialIcon(R.drawable.ic_naver) { onNaver() }
//                    //SocialIcon(R.drawable.ic_google) { onGoogle() }
//                }
//
//
//                Spacer(Modifier.height(24.dp)) // 24dp 간격
//
//                // 안내 텍스트
//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text(
//                        text = "아미 회원이시라면?",
//                        style = TextStyle(
//                            fontFamily = Pretendard,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color(0xFFFFFFFF)
//                        )
//                    )
//                    Spacer(Modifier.width(6.dp))
//                    Text(
//                        text = "로그인하기",
//                        style = TextStyle(
//                            fontFamily = Pretendard,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = Color(0xFFFFFFFF)
//                        ),
//                        modifier = Modifier
//                            .padding(horizontal = 4.dp, vertical = 6.dp) // 탭 영역 확대
//                            .clickable { onEmailLogin() }                 //  이메일 로그인 페이지로 이동
//                    )
//
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun SocialIcon(
//    iconRes: Int,
//    onClick: () -> Unit
//) {
//    // 배경/클립 제거, 정확히 64dp 상자에 꽉 채워 넣기
//    Box(
//        modifier = Modifier
//            .size(64.dp)
//            .clickable(onClick = onClick),
//        contentAlignment = Alignment.Center
//    ) {
//        Image(
//            painter = painterResource(iconRes),
//            contentDescription = null,
//            modifier = Modifier
//                .size(64.dp),                 // 상자와 동일
//            contentScale = ContentScale.FillBounds // 상자를 꽉 채움 (비율 미보장)
//        )
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true, name = "LoginScreenPreview")
//@Composable
//fun LoginScreenPreview() {
//    MaterialTheme {
//        LoginScreen(
//            onEmailLogin = {},
//            onKakao = {},
//            onNaver = {},
//            onGoogle = {}
//        )
//    }
//}