package com.malmungchi.feature.login

import android.app.Activity
import android.util.Log
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
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.auth.model.OAuthToken

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
    onKakao: (String, String?) -> Unit,
    onNaver: () -> Unit = {},
    onGoogle: () -> Unit = {},       // (사용 안함)
    onSignUp: () -> Unit = {},       // 회원가입 텍스트 클릭
    onResetPassword: () -> Unit = {} // 비밀번호 재설정 텍스트 클릭
) {
    val ctx = LocalContext.current
    val activity = ctx as Activity

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
                Spacer(Modifier.width(12.dp))
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
                    SocialIcon(R.drawable.ic_kakao)
                    {

                        //  카카오 로그인 수행
                        // 카카오톡 로그인 버튼 클릭
                        UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                            if (error != null) {
                                // 카카오톡 실패 → 카카오계정 로그인
                                UserApiClient.instance.loginWithKakaoAccount(activity) { token2, error2 ->
                                    if (error2 != null) {
                                        Log.e("LOGIN", "카카오 계정 로그인 실패: $error2")
                                    } else {
                                        requestMissingScopes(activity, token2!!.accessToken, onKakao)
                                    }
                                }
                            } else if (token != null) {
                                // 카카오톡 로그인 성공 → 스코프 확인 후 처리
                                requestMissingScopes(activity, token.accessToken, onKakao)
                            }
                        }
                    }
                    SocialIcon(R.drawable.ic_naver) { onNaver() }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun handleKakaoLogin(accessToken: String, onKakao: (String, String?) -> Unit) {
    UserApiClient.instance.me { user, error ->
        if (error != null) {
            Log.e("KAKAO", "사용자 정보 가져오기 실패: ${error.message}")
            onKakao(accessToken, null) // fallback
            return@me
        }

        val nickname = user?.kakaoAccount?.profile?.nickname
        val profileUrl = user?.kakaoAccount?.profile?.profileImageUrl

        Log.d("KAKAO", "nickname = $nickname")
        Log.d("KAKAO", "profile = $profileUrl")

        // 서버로 accessToken + 프로필 정보 전달
        onKakao(accessToken, nickname)
    }
}

private fun requestMissingScopes(
    activity: Activity,
    accessToken: String,
    onKakao: (String, String?) -> Unit
) {
    // 먼저 사용자 정보 불러오기
    UserApiClient.instance.me { user, error ->
        if (error != null) {
            Log.e("KAKAO", "사용자 정보 조회 실패: $error")
            handleKakaoLogin(accessToken, onKakao)
            return@me
        }

        val scopesNeeded = mutableListOf<String>()

        // 닉네임 없으면 요청 필요
        val needsNickname = user?.kakaoAccount?.profile?.nickname == null
        if (needsNickname) scopesNeeded.add("profile_nickname")

        // 프로필 이미지 없으면 요청 필요
        val needsProfile = user?.kakaoAccount?.profile?.profileImageUrl == null
        if (needsProfile) scopesNeeded.add("profile_image")

        // 스코프가 모두 이미 있으면 그대로 진행
        if (scopesNeeded.isEmpty()) {
            handleKakaoLogin(accessToken, onKakao)
            return@me
        }

        // 스코프 재요청
        UserApiClient.instance.loginWithNewScopes(
            activity,
            scopesNeeded
        ) { token, e ->
            if (e != null) {
                Log.e("KAKAO", "추가 스코프 요청 실패: $e")
                handleKakaoLogin(accessToken, onKakao)
            } else if (token != null) {
                Log.d("KAKAO", "추가 스코프 동의 성공!")
                handleKakaoLogin(token.accessToken, onKakao)
            }
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

//@Preview(showBackground = true, showSystemUi = true, name = "LoginScreenPreview")
//@Composable
//fun LoginScreenPreview() {
//    MaterialTheme {
//        LoginScreen(
//            onEmailLogin = {},
//            onKakao = {},
//            onNaver = {},
//            onGoogle = {},
//            onSignUp = {},
//            onResetPassword = {}
//        )
//    }
//}
