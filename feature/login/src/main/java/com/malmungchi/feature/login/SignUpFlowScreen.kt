package com.malmungchi.feature.login

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import kotlinx.coroutines.launch

private val BrandBlue = Color(0xFF195FCF)
private val DisabledGray = Color(0xFFC9CAD4)
private val ErrorRed = Color(0xFFFF0D0D)
private val HintGray = Color(0xFF989898)

//버튼 크기 규격
private val EmailButtonHeight = 52.dp
private val EmailButtonCorner = 14.dp

private enum class Step { NAME, EMAIL, EMAIL_OTP, PASSWORD, PASSWORD_CONFIRM }
private fun Step.reached(target: Step) = this.ordinal >= target.ordinal

//입력 스타일
private fun sectionHeader(color: Color = BrandBlue) =
    TextStyle(fontFamily = Pretendard, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = color)

private val SmallLabel = TextStyle(fontFamily = Pretendard, fontSize = 12.sp, fontWeight = FontWeight.Medium)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpFlowScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRequestEmailOtp: suspend (email: String) -> Boolean = { true },
    onVerifyEmailOtp: suspend (email: String, code: String) -> Boolean = { _, _ -> true },
    onDone: (name: String, email: String, password: String) -> Unit = { _, _, _ -> }
) {
    val kb = LocalSoftwareKeyboardController.current
    var step by rememberSaveable { mutableStateOf(Step.NAME) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var password by rememberSaveable { mutableStateOf("") }
    var password2 by rememberSaveable { mutableStateOf("") }
    var showOtpSheet by rememberSaveable { mutableStateOf(false) }
    var pwVisible by rememberSaveable { mutableStateOf(false) }
    var pw2Visible by rememberSaveable { mutableStateOf(false) }

    val emailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val hasUpper   = remember(password) { password.any { it.isUpperCase() } }
    val hasLower   = remember(password) { password.any { it.isLowerCase() } }
    val hasSpecial = remember(password) { password.any { !it.isLetterOrDigit() } }
    val hasMinLen  = remember(password) { password.length >= 8 }

    // 최종 유효성
    val pwValid = hasUpper && hasLower && hasSpecial && hasMinLen
    val pw2Valid = remember(password, password2) { password2.isNotBlank() && password2 == password }

    //LaunchedEffect(name) { if (name.isNotBlank() && step == Step.NAME) step = Step.EMAIL }
    LaunchedEffect(pwValid) { if (pwValid && step == Step.PASSWORD) step = Step.PASSWORD_CONFIRM }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("회원가입", style = TextStyle(fontFamily = Pretendard, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)) }
            )
        },
        bottomBar = {
            val enabled = pwValid && pw2Valid

            AnimatedVisibility(visible = step == Step.PASSWORD_CONFIRM) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { onDone(name.trim(), email.trim(), password) },
                        enabled = enabled,
                        modifier = Modifier
                            .fillMaxWidth(0.33f)
                            .height(44.dp),
                        shape = RoundedCornerShape(22.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (enabled) BrandBlue else DisabledGray,
                            disabledContainerColor = DisabledGray
                        )
                    ) {
                        Text(
                            "가입 완료",
                            style = TextStyle(
                                fontFamily = Pretendard,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(Modifier.height(64.dp))

            val emailStage = step.reached(Step.EMAIL)

            val canGoNext = name.isNotBlank()

            if (!emailStage) {
                // ① 최초: 이름 활성, 이메일은 아래 비활성
                SectionInput(
                    headerText = "이름",
                    fieldLabel = "이름",
                    placeholderText = "이름을 입력해주세요.",
                    value = name,
                    onValueChange = { name = it },
                    enabled = step == Step.NAME,
                    // 엔터(완료) 눌러도 단계가 바뀌지 않게: 키보드만 닫기
                    onImeDone = { kb?.hide() },
                    trailing = {
                        TextButton(
                            onClick = { if (canGoNext) step = Step.EMAIL },
                            enabled = canGoNext,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (canGoNext) BrandBlue else DisabledGray
                            )
                        ) { Text("다음", style = SmallLabel) }
                    }
                )
                Spacer(Modifier.height(12.dp))

                SectionInput(
                    headerText = "이메일 주소 입력",
                    fieldLabel = "이메일 주소",
                    placeholderText = "이메일 주소를 입력해주세요.",
                    value = email,
                    onValueChange = { },
                    enabled = false
                )
            } else {
                when (step) {
                    Step.EMAIL, Step.EMAIL_OTP -> {
                        // ② OTP 전: 이메일이 위(활성), 이름은 아래(비활성)
                        SectionInput(
                            headerText = "이메일 주소 입력",
                            fieldLabel = "이메일 주소",
                            placeholderText = "이메일 주소를 입력해주세요.",
                            value = email,
                            onValueChange = { email = it },
                            enabled = step == Step.EMAIL,
                            trailing = {
                                if (step == Step.EMAIL) {
                                    TextButton(
                                        onClick = {
                                            scope.launch {
                                                val ok = onRequestEmailOtp(email.trim())
                                                if (ok) {
                                                    showOtpSheet = true
                                                    step = Step.EMAIL_OTP
                                                } else {
                                                    // 실패 UX 필요 시 추가
                                                }
                                            }
                                        },
                                        enabled = emailValid,
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = if (emailValid) BrandBlue else DisabledGray
                                        )
                                    ) { Text("인증번호", style = SmallLabel) }
                                }
                            }
                        )
                        Spacer(Modifier.height(12.dp))
                        SectionInput(
                            headerText = "이름",
                            fieldLabel = "이름",
                            placeholderText = "",
                            value = name,
                            onValueChange = { },
                            enabled = false
                        )
                    }

                    Step.PASSWORD -> {
                        // ③ OTP 성공 직후: 비밀번호가 가장 위 (활성)
                        PasswordField(
                            label = "비밀번호 입력",
                            value = password,
                            onValueChange = { password = it },
                            visible = pwVisible,
                            onToggleVisible = { pwVisible = !pwVisible },
                            enabled = true,
                            helper = null,
                            isError = password.isNotBlank() && !pwValid,
                            smallFieldLabel = "비밀번호 입력"
                        )
                        Spacer(Modifier.height(8.dp))
                        PasswordChecklist(
                            hasComplexity = hasUpper && hasLower && hasSpecial,
                            hasMinLen = hasMinLen
                        )
                        Spacer(Modifier.height(12.dp))

                        // 아래로 이메일/이름 비활성
                        SectionInput(
                            headerText = "이메일 주소 입력",
                            fieldLabel = "이메일 주소",
                            placeholderText = "",
                            value = email,
                            onValueChange = { },
                            enabled = false
                        )
                        Spacer(Modifier.height(12.dp))
                        SectionInput(
                            headerText = "이름",
                            fieldLabel = "이름",
                            placeholderText = "",
                            value = name,
                            onValueChange = { },
                            enabled = false
                        )
                    }

                    Step.PASSWORD_CONFIRM, Step.NAME -> {
                        // ④ 비밀번호 유효 → 확인 단계
                        PasswordField(
                            label = "비밀번호 확인",
                            value = password2,
                            onValueChange = { password2 = it },
                            visible = pw2Visible,
                            onToggleVisible = { pw2Visible = !pw2Visible },
                            enabled = true,
                            helper = if (password2.isBlank() || pw2Valid) null else "비밀번호가 일치하지 않아요",
                            isError = password2.isNotBlank() && !pw2Valid,
                            smallFieldLabel = "비밀번호 확인"
                        )
                        Spacer(Modifier.height(12.dp))

                        PasswordField(
                            label = "비밀번호 입력",
                            value = password,
                            onValueChange = { },
                            visible = pwVisible,
                            onToggleVisible = { pwVisible = !pwVisible },
                            enabled = false,
                            helper = null,
                            isError = false,
                            smallFieldLabel = "비밀번호 입력"
                        )
                        Spacer(Modifier.height(12.dp))

                        SectionInput(
                            headerText = "이메일 주소 입력",
                            fieldLabel = "이메일 주소",
                            placeholderText = "",
                            value = email,
                            onValueChange = { },
                            enabled = false
                        )
                        Spacer(Modifier.height(12.dp))
                        SectionInput(
                            headerText = "이름",
                            fieldLabel = "이름",
                            placeholderText = "",
                            value = name,
                            onValueChange = { },
                            enabled = false
                        )
                    }
                }
            }
        }
    }

    if (showOtpSheet) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showOtpSheet = false },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            // 가운데 카드
            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    // 타이틀 + 재요청
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "코드번호 입력",
                            style = TextStyle(
                                fontFamily = Pretendard,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandBlue
                            )
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(
                            onClick = { scope.launch { onRequestEmailOtp(email.trim()) } }
                        ) {
                            Text(
                                "재요청",
                                style = TextStyle(
                                    fontFamily = Pretendard,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BrandBlue
                                )
                            )
                        }
                    }

                    // 이메일 표시
                    if (email.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            email,
                            style = TextStyle(
                                fontFamily = Pretendard,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFB8BAC3)
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // OTP 입력 필드
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                        singleLine = true,
                        placeholder = { Text("6자리", style = placeholder()) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = fieldText().color
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { kb?.hide() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = DisabledGray
                        )
                    )

                    Spacer(Modifier.height(12.dp))

                    // 인증 완료 버튼
                    Button(
                        onClick = {
                            scope.launch {
                                if (otp.length == 6) {
                                    val ok = onVerifyEmailOtp(email.trim(), otp)
                                    if (ok) {
                                        step = Step.PASSWORD
                                        showOtpSheet = false
                                        otp = ""
                                    } else {
                                        // 실패 UX 필요 시 추가
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(EmailButtonHeight),
                        shape = RoundedCornerShape(EmailButtonCorner),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("인증 완료", style = buttonText())
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionInput(
    headerText: String,
    fieldLabel: String,
    placeholderText: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    trailing: (@Composable () -> Unit)? = null,
    onImeDone: () -> Unit = {}
) {
    val labelColor = if (enabled) BrandBlue else DisabledGray
    val textColor  = if (enabled) Color(0xFF111111) else DisabledGray

    Text(headerText, style = sectionHeader(if (enabled) BrandBlue else DisabledGray))
    Spacer(Modifier.height(6.dp))

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(fieldLabel, style = SmallLabel.copy(color = labelColor)) },
        placeholder = { Text(placeholderText, style = placeholder()) },
        textStyle = fieldText().copy(color = textColor),
        trailingIcon = trailing,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onImeDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (enabled) BrandBlue else DisabledGray,
            unfocusedBorderColor = DisabledGray,
            errorBorderColor = ErrorRed,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            disabledTextColor = DisabledGray,
            disabledLabelColor = DisabledGray,
            disabledPlaceholderColor = DisabledGray
        )
    )
}

@Composable
private fun LabeledField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    trailing: (@Composable () -> Unit)? = null,
    onImeDone: () -> Unit = {}
) {
    val labelColor = if (enabled) BrandBlue else DisabledGray
    val textColor  = if (enabled) Color(0xFF111111) else DisabledGray

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label, style = TextStyle(fontFamily = Pretendard, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = labelColor)) },
            placeholder = { Text(placeholder, style = placeholder()) },
            textStyle = fieldText().copy(color = textColor),
            trailingIcon = trailing,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onImeDone() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (enabled) BrandBlue else DisabledGray,
                unfocusedBorderColor = DisabledGray,
                errorBorderColor = ErrorRed,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledTextColor = DisabledGray,
                disabledLabelColor = DisabledGray,
                disabledPlaceholderColor = DisabledGray
            )
        )
    }
}

@Composable
private fun PasswordChecklist(
    hasComplexity: Boolean,
    hasMinLen: Boolean
) {
    val row: @Composable (String, Boolean) -> Unit = { text, ok ->
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (ok) {
                Text(
                    "✓",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = HintGray
                    )
                )
                Spacer(Modifier.width(6.dp))
            } else {
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (ok) HintGray else ErrorRed
                )
            )
        }
    }

    row("대, 소문자, 숫자, 특수문자 포함", hasComplexity)
    Spacer(Modifier.height(4.dp))
    row("최소 8자리 수", hasMinLen)
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    enabled: Boolean,
    helper: String? = null,
    isError: Boolean = false,
    smallFieldLabel: String = "비밀번호 입력",
    showCheck: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    val active = focused || value.isNotBlank()

    val headerStyle = TextStyle(
        fontFamily = Pretendard,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (enabled) BrandBlue else DisabledGray
    )

    val smallLabelStyle = SmallLabel.copy(color = if (enabled) BrandBlue else DisabledGray)

    Column(Modifier.fillMaxWidth()) {
        Text(label, style = headerStyle)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
            label = { Text(smallFieldLabel, style = smallLabelStyle) },
            placeholder = { Text("비밀번호를 입력해주세요.", style = placeholder()) },
            textStyle = fieldText().copy(fontSize = 18.sp, color = if (enabled) Color(0xFF111111) else DisabledGray),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                if (showCheck) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_password_check),
                        contentDescription = "확인됨",
                        tint = BrandBlue
                    )
                } else {
                    IconButton(onClick = onToggleVisible, enabled = enabled) {
                        Icon(
                            imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (visible) "숨김" else "표시",
                            tint = if (enabled) BrandBlue else DisabledGray
                        )
                    }
                }
            },
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (enabled) BrandBlue else DisabledGray,
                unfocusedBorderColor = DisabledGray,
                errorBorderColor = ErrorRed,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledTextColor = DisabledGray,
                disabledLabelColor = DisabledGray,
                disabledPlaceholderColor = DisabledGray
            )
        )
        if (!helper.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(helper, style = SmallLabel.copy(color = if (isError) ErrorRed else HintGray))
        }
    }
}

private fun h1() = TextStyle(fontFamily = Pretendard, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
private fun title() = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
private fun label(color: Color = Color(0xFF6B6E7A)) = TextStyle(fontFamily = Pretendard, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
private fun placeholder() = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF195FCF))
private fun fieldText() = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111111))
private fun buttonText() = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PreviewInitial() {
    SignUpFlowScreen()
}


//import android.util.Patterns
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.malmungchi.core.designsystem.Pretendard
//import androidx.compose.material3.TextFieldDefaults
//import androidx.compose.material.icons.filled.Visibility
//import androidx.compose.material.icons.filled.VisibilityOff
//import androidx.compose.ui.focus.onFocusChanged
//import androidx.compose.ui.res.painterResource
//
//private val BrandBlue = Color(0xFF195FCF)
//private val DisabledGray = Color(0xFFC9CAD4)
//private val ErrorRed = Color(0xFFFF0D0D)
//private val HintGray = Color(0xFF989898)
//
////버튼 크기 규격
//private val EmailButtonHeight = 52.dp
//private val EmailButtonCorner = 14.dp
//
//private enum class Step { NAME, EMAIL, EMAIL_OTP, PASSWORD, PASSWORD_CONFIRM }
//private fun Step.reached(target: Step) = this.ordinal >= target.ordinal
//
////입력 스타일
//private fun sectionHeader(color: Color = BrandBlue) =
//    TextStyle(fontFamily = Pretendard, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = color)
//
//private val SmallLabel = TextStyle(fontFamily = Pretendard, fontSize = 12.sp, fontWeight = FontWeight.Medium)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SignUpFlowScreen(
//    modifier: Modifier = Modifier,
//    onBack: () -> Unit = {},
//    onRequestEmailOtp: suspend (email: String) -> Boolean = { true },
//    onVerifyEmailOtp: suspend (email: String, code: String) -> Boolean = { _, _ -> true },
//    onDone: (name: String, email: String, password: String) -> Unit = { _, _, _ -> }
//) {
//    val kb = LocalSoftwareKeyboardController.current
//    var step by rememberSaveable { mutableStateOf(Step.NAME) }
//    var name by rememberSaveable { mutableStateOf("") }
//    var email by rememberSaveable { mutableStateOf("") }
//    var otp by rememberSaveable { mutableStateOf("") }
//    var password by rememberSaveable { mutableStateOf("") }
//    var password2 by rememberSaveable { mutableStateOf("") }
//    var showOtpSheet by rememberSaveable { mutableStateOf(false) }
//    var pwVisible by rememberSaveable { mutableStateOf(false) }
//    var pw2Visible by rememberSaveable { mutableStateOf(false) }
//
//    val emailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
//    val hasUpper   = remember(password) { password.any { it.isUpperCase() } }
//    val hasLower   = remember(password) { password.any { it.isLowerCase() } }
//    val hasSpecial = remember(password) { password.any { !it.isLetterOrDigit() } }
//    val hasMinLen  = remember(password) { password.length >= 8 }
//
//// 최종 유효성
//    val pwValid = hasUpper && hasLower && hasSpecial && hasMinLen
//    //val pwValid = remember(password) { password.matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,20}$")) }
//    val pw2Valid = remember(password, password2) { password2.isNotBlank() && password2 == password }
//
//    LaunchedEffect(name) { if (name.isNotBlank() && step == Step.NAME) step = Step.EMAIL }
//    LaunchedEffect(pwValid) { if (pwValid && step == Step.PASSWORD) step = Step.PASSWORD_CONFIRM }
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = { Text("회원가입", style = TextStyle(fontFamily = Pretendard, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)) }
//            )
//        },
//        bottomBar = {
//            val enabled = pwValid && pw2Valid
//
//            AnimatedVisibility(visible = step == Step.PASSWORD_CONFIRM) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp)
//                        .padding(bottom = 64.dp),              // 👈 바닥에서 64 위
//                    contentAlignment = Alignment.Center        // 👈 가운데 정렬
//                ) {
//                    Button(
//                        onClick = { onDone(name.trim(), email.trim(), password) },
//                        enabled = enabled,
//                        modifier = Modifier
//                            .fillMaxWidth(0.33f)              // 👈 가로폭 1/3
//                            .height(44.dp),                   // 👈 높이 살짝 줄임 (원하면 40~48 조절)
//                        shape = RoundedCornerShape(22.dp),    // (옵션) pill 느낌; 기존 14dp 유지해도 OK
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = if (enabled) BrandBlue else DisabledGray,
//                            disabledContainerColor = DisabledGray
//                        )
//                    ) {
//                        Text(
//                            "가입 완료",
//                            style = TextStyle(
//                                fontFamily = Pretendard,
//                                fontSize = 16.sp,             // 👈 16
//                                fontWeight = FontWeight.SemiBold,
//                                color = Color.White
//                            )
//                        )
//                    }
//                }
//            }
//        },
//        modifier = modifier
//    ) { inner ->
//        Column(
//            modifier = Modifier
//                .padding(inner)
//                .padding(horizontal = 16.dp)
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.Top
//        ) { Spacer(Modifier.height(64.dp))
//
//
//            val emailStage = step.reached(Step.EMAIL) // 이름 입력완료 이후
//
//            if (!emailStage) {
//                // ① 최초: 이름 활성, 이메일은 아래 비활성
//                SectionInput(
//                    headerText = "이름",
//                    fieldLabel = "이름",
//                    placeholderText = "이름을 입력해주세요.",
//                    value = name,
//                    onValueChange = { name = it },
//                    enabled = step == Step.NAME,
//                    onImeDone = { kb?.hide(); if (name.isNotBlank()) step = Step.EMAIL }
//                )
//                Spacer(Modifier.height(12.dp))
//
//                SectionInput(
//                    headerText = "이메일 주소 입력",
//                    fieldLabel = "이메일 주소",
//                    placeholderText = "이메일 주소를 입력해주세요.",
//                    value = email,
//                    onValueChange = { },
//                    enabled = false
//                )
//            } else {
//                when (step) {
//
//                    Step.EMAIL, Step.EMAIL_OTP -> {
//                        // ② OTP 전: 이메일이 위(활성), 이름은 아래(비활성)
//                        SectionInput(
//                            headerText = "이메일 주소 입력",
//                            fieldLabel = "이메일 주소",
//                            placeholderText = "이메일 주소를 입력해주세요.",
//                            value = email,
//                            onValueChange = { email = it },
//                            enabled = step == Step.EMAIL,
//                            trailing = {
//                                if (step == Step.EMAIL) {
//                                    TextButton(
//                                        onClick = { showOtpSheet = true; step = Step.EMAIL_OTP },
//                                        enabled = emailValid,
//                                        colors = ButtonDefaults.textButtonColors(
//                                            contentColor = if (emailValid) BrandBlue else DisabledGray
//                                        )
//                                    ) { Text("인증번호", style = SmallLabel) }
//                                }
//                            }
//                        )
//                        Spacer(Modifier.height(12.dp))
//                        SectionInput(
//                            headerText = "이름",
//                            fieldLabel = "이름",
//                            placeholderText = "",
//                            value = name,
//                            onValueChange = { },
//                            enabled = false
//                        )
//                    }
//
//                    Step.PASSWORD -> {
//                        // ③ OTP 성공 직후: 비밀번호가 가장 위 (활성)
//                        PasswordField(
//                            label = "비밀번호 입력",
//                            value = password,
//                            onValueChange = { password = it },
//                            visible = pwVisible,
//                            onToggleVisible = { pwVisible = !pwVisible },
//                            enabled = true, // 현재 단계에서만 활성
//                            helper = null,
//                            isError = password.isNotBlank() && !pwValid,
//                            smallFieldLabel = "비밀번호 입력"
//                        )
//                        Spacer(Modifier.height(8.dp))
//                        PasswordChecklist(
//                            hasComplexity = hasUpper && hasLower && hasSpecial,
//                            hasMinLen = hasMinLen
//                        )
//                        Spacer(Modifier.height(12.dp))
//
//                        // 아래로 이메일/이름 비활성
//                        SectionInput(
//                            headerText = "이메일 주소 입력",
//                            fieldLabel = "이메일 주소",
//                            placeholderText = "",
//                            value = email,
//                            onValueChange = { },
//                            enabled = false
//                        )
//                        Spacer(Modifier.height(12.dp))
//                        SectionInput(
//                            headerText = "이름",
//                            fieldLabel = "이름",
//                            placeholderText = "",
//                            value = name,
//                            onValueChange = { },
//                            enabled = false
//                        )
//                    }
//
//                    Step.PASSWORD_CONFIRM, Step.NAME -> {
//                        // ④ 비밀번호 유효 → 확인 단계:
//                        //    ✅ 비밀번호 확인이 가장 위
//                        PasswordField(
//                            label = "비밀번호 확인",
//                            value = password2,
//                            onValueChange = { password2 = it },
//                            visible = pw2Visible,
//                            onToggleVisible = { pw2Visible = !pw2Visible },
//                            enabled = true,
//                            helper = if (password2.isBlank() || pw2Valid) null else "비밀번호가 일치하지 않아요",
//                            isError = password2.isNotBlank() && !pw2Valid,
//                            smallFieldLabel = "비밀번호 확인"
//                        )
//                        Spacer(Modifier.height(12.dp))
//
//                        // 그 다음, 비밀번호 입력칸은 비활성로 내려감(체크리스트는 감춤)
//                        PasswordField(
//                            label = "비밀번호 입력",
//                            value = password,
//                            onValueChange = { }, // 확인 단계에서는 수정 막기
//                            visible = pwVisible,
//                            onToggleVisible = { pwVisible = !pwVisible },
//                            enabled = false,     // 🔒 비활성
//                            helper = null,
//                            isError = false,
//                            smallFieldLabel = "비밀번호 입력"
//                        )
//                        Spacer(Modifier.height(12.dp))
//
//                        SectionInput(
//                            headerText = "이메일 주소 입력",
//                            fieldLabel = "이메일 주소",
//                            placeholderText = "",
//                            value = email,
//                            onValueChange = { },
//                            enabled = false
//                        )
//                        Spacer(Modifier.height(12.dp))
//                        SectionInput(
//                            headerText = "이름",
//                            fieldLabel = "이름",
//                            placeholderText = "",
//                            value = name,
//                            onValueChange = { },
//                            enabled = false
//                        )
//                    }
//                }
//            }
//
//        }
//    }
//
//    if (showOtpSheet) {
//        androidx.compose.ui.window.Dialog(
//            onDismissRequest = { showOtpSheet = false },
//            properties = androidx.compose.ui.window.DialogProperties(
//                dismissOnBackPress = true,
//                dismissOnClickOutside = true,
//                usePlatformDefaultWidth = false // 폭 제어
//            )
//        ) {
//            // 가운데 카드 (네 모서리 전부 둥글게)
//            Surface(
//                shape = RoundedCornerShape(18.dp), // 👈 네 모서리 모두 18dp 둥글게
//                tonalElevation = 2.dp,
//                shadowElevation = 2.dp,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 24.dp) // 화면 좌우 여백
//                    .imePadding()
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(16.dp)
//                        .verticalScroll(rememberScrollState()),
//                ) {
//                    // 타이틀 + 재요청
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            "코드번호 입력",
//                            style = TextStyle(
//                                fontFamily = Pretendard,
//                                fontSize = 20.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = BrandBlue
//                            )
//                        )
//                        Spacer(Modifier.weight(1f))
//                        TextButton(
//                            onClick = {
//                                // 재요청 콜백 연결 가능
//                            }
//                        ) {
//                            Text(
//                                "재요청",
//                                style = TextStyle(
//                                    fontFamily = Pretendard,
//                                    fontSize = 14.sp,
//                                    fontWeight = FontWeight.Medium,
//                                    color = BrandBlue
//                                )
//                            )
//                        }
//                    }
//
//                    // 이메일 표시
//                    if (email.isNotBlank()) {
//                        Spacer(Modifier.height(2.dp))
//                        Text(
//                            email,
//                            style = TextStyle(
//                                fontFamily = Pretendard,
//                                fontSize = 14.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color(0xFFB8BAC3)
//                            )
//                        )
//                    }
//
//                    Spacer(Modifier.height(12.dp))
//
//                    // OTP 입력 필드
//                    OutlinedTextField(
//                        value = otp,
//                        onValueChange = { otp = it.filter(Char::isDigit).take(6) },
//                        singleLine = true,
//                        placeholder = { Text("6자리", style = placeholder()) },
//                        modifier = Modifier.fillMaxWidth(),
//                        textStyle = TextStyle(
//                            fontFamily = Pretendard,
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = fieldText().color
//                        ),
//                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//                        keyboardActions = KeyboardActions(onDone = { kb?.hide() }),
//                        colors = OutlinedTextFieldDefaults.colors(
//                            focusedBorderColor = BrandBlue,
//                            unfocusedBorderColor = DisabledGray
//                        )
//                    )
//
//                    Spacer(Modifier.height(12.dp))
//
//                    // 인증 완료 버튼
//                    Button(
//                        onClick = {
//                            if (otp.length == 6) {
//                                step = Step.PASSWORD
//                                showOtpSheet = false
//                                otp = ""
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(EmailButtonHeight),
//                        shape = RoundedCornerShape(EmailButtonCorner),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = BrandBlue,
//                            contentColor = Color.White
//                        ),
//                        elevation = ButtonDefaults.buttonElevation(0.dp)
//                    ) {
//                        Text("인증 완료", style = buttonText())
//                    }
//
//                    Spacer(Modifier.height(4.dp))
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun SectionInput(
//    headerText: String,                 // 섹션 제목 (파랑 18/SB)
//    fieldLabel: String,                 // 필드 내부의 작은 라벨(12/M)
//    placeholderText: String,            // 힌트(16/M)
//    value: String,
//    onValueChange: (String) -> Unit,
//    enabled: Boolean,
//    trailing: (@Composable () -> Unit)? = null,
//    onImeDone: () -> Unit = {}
//) {
//    val labelColor = if (enabled) BrandBlue else DisabledGray
//    val textColor  = if (enabled) Color(0xFF111111) else DisabledGray
//
//    // 섹션 헤더
//    Text(headerText, style = sectionHeader(if (enabled) BrandBlue else DisabledGray))
//    Spacer(Modifier.height(6.dp))
//
//    OutlinedTextField(
//        value = value,
//        onValueChange = onValueChange,
//        singleLine = true,
//        enabled = enabled,
//        modifier = Modifier.fillMaxWidth(),
//        // 작은 라벨(12/M)
//        label = { Text(fieldLabel, style = SmallLabel.copy(color = labelColor)) },
//        // 힌트(16/M)
//        placeholder = { Text(placeholderText, style = placeholder()) },
//        textStyle = fieldText().copy(color = textColor),
//        trailingIcon = trailing,
//        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//        keyboardActions = KeyboardActions(onDone = { onImeDone() }),
//        colors = OutlinedTextFieldDefaults.colors(
//            focusedBorderColor = if (enabled) BrandBlue else DisabledGray,
//            unfocusedBorderColor = DisabledGray,
//            errorBorderColor = ErrorRed,
//            focusedContainerColor = Color.Transparent,
//            unfocusedContainerColor = Color.Transparent,
//            disabledContainerColor = Color.Transparent,
//            disabledTextColor = DisabledGray,      // 입력값 회색
//            disabledLabelColor = DisabledGray,
//            disabledPlaceholderColor = DisabledGray
//        )
//    )
//}
//
//@Composable
//private fun LabeledField(
//    label: String,
//    placeholder: String,
//    value: String,
//    onValueChange: (String) -> Unit,
//    enabled: Boolean,
//    trailing: (@Composable () -> Unit)? = null,
//    onImeDone: () -> Unit = {}
//) {
//    val labelColor = if (enabled) BrandBlue else DisabledGray
//    val textColor  = if (enabled) Color(0xFF111111) else DisabledGray
//
//    Column(Modifier.fillMaxWidth()) {
//        OutlinedTextField(
//            value = value,
//            onValueChange = onValueChange,
//            singleLine = true,
//            enabled = enabled,
//            modifier = Modifier.fillMaxWidth(),
//            label = { Text(label, style = TextStyle(fontFamily = Pretendard, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = labelColor)) },
//            placeholder = { Text(placeholder, style = placeholder()) },
//            textStyle = fieldText().copy(color = textColor),
//            trailingIcon = trailing,
//            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//            keyboardActions = KeyboardActions(onDone = { onImeDone() }),
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = if (enabled) BrandBlue else DisabledGray,
//                unfocusedBorderColor = DisabledGray,
//                errorBorderColor = ErrorRed,
//                focusedContainerColor = Color.Transparent,
//                unfocusedContainerColor = Color.Transparent,
//                disabledContainerColor = Color.Transparent,
//                disabledTextColor = DisabledGray,
//                disabledLabelColor = DisabledGray,
//                disabledPlaceholderColor = DisabledGray
//            )
//        )
//    }
//}
//
//@Composable
//private fun PasswordChecklist(
//    hasComplexity: Boolean,
//    hasMinLen: Boolean
//) {
//    val row: @Composable (String, Boolean) -> Unit = { text, ok ->
//        Row(
//            Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // 아이콘 제거, 조건 충족 시에만 ✓ 텍스트
//            if (ok) {
//                Text(
//                    "✓",
//                    style = TextStyle(
//                        fontFamily = Pretendard,
//                        fontSize = 12.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = HintGray
//                    )
//                )
//                Spacer(Modifier.width(6.dp))
//            } else {
//                // 정렬 유지용 최소 간격
//                Spacer(Modifier.width(12.dp))
//            }
//            Text(
//                text,
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = if (ok) HintGray else ErrorRed
//                )
//            )
//        }
//    }
//
//    row("대, 소문자, 숫자, 특수문자 포함", hasComplexity)
//    Spacer(Modifier.height(4.dp))
//    row("최소 8자리 수", hasMinLen)
//}
//
//@Composable
//private fun PasswordField(
//    label: String,                  // 섹션 헤더(파랑 18/SB)
//    value: String,
//    onValueChange: (String) -> Unit,
//    visible: Boolean,
//    onToggleVisible: () -> Unit,
//    enabled: Boolean,
//    helper: String? = null,
//    isError: Boolean = false,
//    smallFieldLabel: String = "비밀번호 입력",   // 👈 필드 내부 작은 라벨(12/M)
//    showCheck: Boolean = false
//) {
//    var focused by remember { mutableStateOf(false) }
//    val active = focused || value.isNotBlank()
//
//    // 섹션 헤더: 18 / SB / 파랑
//    val headerStyle = TextStyle(
//        fontFamily = Pretendard,
//        fontSize = 18.sp,
//        fontWeight = FontWeight.SemiBold,
//        color = if (enabled) BrandBlue else DisabledGray
//    )
//
//    // 내부 작은 라벨(12 / M / 활성=파랑, 비활성=회색)
//    val smallLabelStyle = SmallLabel.copy(color = if (enabled) BrandBlue else DisabledGray)
//
//    Column(Modifier.fillMaxWidth()) {
//        Text(label, style = headerStyle)       // 헤더
//        Spacer(Modifier.height(6.dp))
//        OutlinedTextField(
//            value = value,
//            onValueChange = onValueChange,
//            singleLine = true,
//            enabled = enabled,
//            modifier = Modifier
//                .fillMaxWidth()
//                .onFocusChanged { focused = it.isFocused },
//            // 👇 필드 내부 작은 라벨 추가
//            label = { Text(smallFieldLabel, style = smallLabelStyle) },
//            // 힌트(16/M)
//            placeholder = { Text("비밀번호를 입력해주세요.", style = placeholder()) },
//            // 입력 글자 18sp
//            textStyle = fieldText().copy(fontSize = 18.sp, color = if (enabled) Color(0xFF111111) else DisabledGray),
//            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
//            trailingIcon = {
//                if (showCheck) {
//                    // ✅ 확인 완료면 체크 아이콘 노출 (클릭 불가)
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_password_check),
//                        contentDescription = "확인됨",
//                        tint = BrandBlue,
//                        modifier = Modifier.size(24.dp)
//                    )
//                } else {
//                    // 기본: 눈 아이콘 토글
//                    IconButton(onClick = onToggleVisible, enabled = enabled) {
//                        Icon(
//                            imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
//                            contentDescription = if (visible) "숨김" else "표시",
//                            tint = if (enabled) BrandBlue else DisabledGray
//                        )
//                    }
//                }
//            },
//            isError = isError,
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedBorderColor = if (enabled) BrandBlue else DisabledGray,
//                unfocusedBorderColor = DisabledGray,
//                errorBorderColor = ErrorRed,
//                focusedContainerColor = Color.Transparent,
//                unfocusedContainerColor = Color.Transparent,
//                disabledContainerColor = Color.Transparent,
//                disabledTextColor = DisabledGray,
//                disabledLabelColor = DisabledGray,
//                disabledPlaceholderColor = DisabledGray
//            )
//        )
//        if (!helper.isNullOrBlank()) {
//            Spacer(Modifier.height(6.dp))
//            Text(helper, style = SmallLabel.copy(color = if (isError) ErrorRed else HintGray))
//        }
//    }
//}
//
//
//
//private fun h1() = TextStyle(fontFamily = Pretendard, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
//private fun title() = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
//private fun label(color: Color = Color(0xFF6B6E7A)) = TextStyle(fontFamily = Pretendard, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
//private fun placeholder() = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF195FCF))
//private fun fieldText() = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111111))
//private fun buttonText() = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
//
//@Preview(showBackground = true, widthDp = 360)
//@Composable
//private fun PreviewInitial() {
//    SignUpFlowScreen()
//}
