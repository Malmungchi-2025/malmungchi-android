package com.malmungchi.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.feature.login.R
import com.malmungchi.core.designsystem.Pretendard
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.hilt.navigation.compose.hiltViewModel


private val BrandBlue = Color(0xFF195FCF)
private val GrayField = Color(0xFFC9CAD4)
private val MidGray = Color(0xFF616161)

//에러시
private val ErrorRed = Color(0xFFFF2F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailLoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: (userId: Int, token: String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var pw by remember { mutableStateOf("") }
    var saveId by remember { mutableStateOf(false) }
    var autoLogin by remember { mutableStateOf(false) }
    //var showPw by remember { mutableStateOf(false) }

    // ⬇️ 로그인 실패/검증 실패 시 보여줄 에러 상태
    var authError by remember { mutableStateOf<String?>(null) }

    // 입력 바꿀 때는 에러 숨김
    val onEmailChange: (String) -> Unit = {
        email = it
        if (authError != null) authError = null
    }
    val onPwChange: (String) -> Unit = {
        pw = it
        if (authError != null) authError = null
    }
    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val formValid = email.isNotBlank() && pw.isNotBlank() && isEmailValid

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "로그인",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.Black)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(12.dp))

            Text(
                "이메일",
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = {
                    Text(
                        "가입하신 이메일을 입력하세요.",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GrayField,
                    unfocusedBorderColor = GrayField,
                    disabledBorderColor = GrayField,
                    cursorColor = Color.Black,
                    focusedPlaceholderColor = GrayField,
                    unfocusedPlaceholderColor = GrayField,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "비밀번호",
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    color = Color.Black
                )
            )

            Spacer(Modifier.height(12.dp))
            var showPw by remember { mutableStateOf(false) }

            OutlinedTextField(
                value = pw,
                onValueChange = { pw = it },
                placeholder = {
                    Text(
                        "비밀번호를 입력하세요.",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    )
                },
                singleLine = true,
                visualTransformation = if (showPw) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showPw = !showPw }) {
                        Icon(
                            imageVector = if (showPw) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (showPw) "비밀번호 숨기기" else "비밀번호 보기",
                            tint = if (showPw) BrandBlue else GrayField
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GrayField,
                    unfocusedBorderColor = GrayField,
                    disabledBorderColor = GrayField,
                    cursorColor = Color.Black,
                    focusedPlaceholderColor = GrayField,
                    unfocusedPlaceholderColor = GrayField,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )

            // ⬇️ 비밀번호 필드 하단 4dp 위치에 에러 표시
            Spacer(Modifier.height(4.dp))
            if (authError != null) {
                Text(
                    text = authError!!,
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = ErrorRed
                    )
                )
            }

            Spacer(Modifier.height(16.dp))



            // 체크박스: 오른쪽 정렬, 간격 촘촘하게 + 라벨 탭 가능
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { saveId = !saveId }
                ) {
                    Checkbox(
                        checked = saveId,
                        onCheckedChange = { saveId = it },
                        modifier = Modifier.padding(0.dp), // 기본 padding 제거
                        colors = CheckboxDefaults.colors(
                            checkedColor = BrandBlue,
                            uncheckedColor = GrayField,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        "아이디 저장",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    )
                }

                Spacer(Modifier.width(2.dp)) // 두 그룹 간 간격 (필요에 따라 조정)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { autoLogin = !autoLogin }
                ) {
                    Checkbox(
                        checked = autoLogin,
                        onCheckedChange = { autoLogin = it },
                        modifier = Modifier.padding(0.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = BrandBlue,
                            uncheckedColor = GrayField,
                            checkmarkColor = Color.White
                        )
                    )
                    Text(
                        "자동 로그인",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                //onClick = { onLoginSuccess() }, // TODO 실제 로그인 로직
                onClick = {
                    authError = null
                    viewModel.login(email, pw) { ok, userId, token, msg ->
                        if (ok && userId != null && token != null) {
                            // 네비게이션(or 콜백)으로 메인에 id/토큰 전달
                            onLoginSuccess(userId, token)
                        } else {
                            authError = msg ?: "이메일주소 혹은 비밀번호를 다시 확인해주세요!"
                        }
                    }
                },enabled = formValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                //enabled = formValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (formValid) BrandBlue else GrayField,
                    contentColor = Color.White,
                    disabledContainerColor = GrayField,
                    disabledContentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    "로그인",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                )
            }

            // 더 아래로: 피그마처럼 간격 확보
            Spacer(Modifier.height(40.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "비밀번호 찾기",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = MidGray
                    )
                )
                Text(
                    "  |  ",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = MidGray
                    )
                )
                Text(
                    "회원가입",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = MidGray
                    )
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                SocialIcon64(resId = R.drawable.ic_kakao) { /* TODO */ }
                Spacer(Modifier.width(20.dp))
                SocialIcon64(resId = R.drawable.ic_naver) { /* TODO */ }
                Spacer(Modifier.width(20.dp))
                SocialIcon64(resId = R.drawable.ic_google) { /* TODO */ }
            }
        }
    }
}

/** 64dp 소셜 아이콘 (간단 버전) */
@Composable
fun SocialIcon64(
    resId: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "EmailLoginScreen")
@Composable
private fun EmailLoginScreenPreview() {
    MaterialTheme {
        EmailLoginScreen(
            onBack = {},
            onLoginSuccess = { _, _ -> }   // ← 파라미터 2개 받는 람다로 수정
        )
    }
}