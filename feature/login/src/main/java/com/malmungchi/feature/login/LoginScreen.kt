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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.login.R

private val BrandBlue = Color(0xFF195FCF)
private val GAP = 16.dp

private val TopLabelTopPadding = GAP
private val TitlePushDown = 96.dp

private val EmailButtonHeight = 52.dp
private val EmailButtonCorner = 14.dp

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

@Composable
fun LoginScreen(
    onEmailLogin: () -> Unit,        // 이메일로 시작하기 → 로그인 이동
    onKakao: () -> Unit = {},
    onNaver: () -> Unit = {},
    onGoogle: () -> Unit = {},       // (사용 안함)
    onSignUp: () -> Unit = {},       // 회원가입 텍스트 클릭
    onResetPassword: () -> Unit = {} // 비밀번호 재설정 텍스트 클릭
) {
    val ctx = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlue)
            .systemBarsPadding()
    ) {
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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = GAP),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(TopLabelTopPadding))
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

            // ── 메인 CTA: 이메일로 시작하기 (위치는 현재가 딱 좋다고 해서 그대로) ──
            Button(
                onClick = onEmailLogin,
                shape = RoundedCornerShape(EmailButtonCorner),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(EmailButtonHeight)
                    .offset(y = (-64).dp), // 버튼 자체는 기존처럼 64dp 위로
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = BrandBlue
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_email),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "이메일로 시작하기",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // 버튼과 아래 블록 사이 기본 간격
            Spacer(Modifier.height(10.dp))

            // ✅ 아래 3개 블록(재설정|회원가입, Divider+텍스트, 소셜)을 한 덩어리로 32dp 위로
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset(y = (-56).dp)
            ) {
                // ── 비밀번호 재설정 | 회원가입 ──
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "비밀번호 재설정",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFFFFF)
                        ),
                        modifier = Modifier.clickable { onResetPassword() }
                    )
                    Text(
                        text = "  |  ",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0x80FFFFFF)
                        )
                    )
                    Text(
                        text = "회원가입",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFFFFFFF)
                        ),
                        modifier = Modifier.clickable { onSignUp() }
                    )
                }
                Spacer(Modifier.height(32.dp))

                //Spacer(Modifier.height(GAP))

                // ── “3초만에 시작하기” 좌우 선 ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "  3초만에 시작하기  ",
                        color = Color.White.copy(alpha = 0.95f),
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Divider(
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(GAP))

                // ── 소셜: 구글 제거, 카카오/네이버만 중앙 ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(GAP, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SocialIcon(R.drawable.ic_kakao) { onKakao() }
                    SocialIcon(R.drawable.ic_naver) { onNaver() }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SocialIcon(
    iconRes: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.FillBounds
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
//
//import android.widget.Toast
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.alpha
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.rotate
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.malmungchi.core.designsystem.Pretendard
//import com.malmungchi.feature.login.R
//
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
//                        "이메일로 회원가입 하기",
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
//                    SocialIcon(R.drawable.ic_google) { onGoogle() }
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