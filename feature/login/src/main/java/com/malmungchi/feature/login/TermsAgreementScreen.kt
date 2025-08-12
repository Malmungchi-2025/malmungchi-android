package com.malmungchi.feature.login


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.foundation.layout.Box


@Composable
fun TermsAgreementScreen(
    modifier: Modifier = Modifier,
    logoResId: Int? = R.drawable.img_malmungchi_logo,
    onOpenAppTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenMarketing: () -> Unit,
    onAgreeContinue: () -> Unit
) {
    // ---- Colors ----
    val blue = Color(0xFF195FCF)
    val grayBorder = Color(0xFFE0E0E0)
    val checkboxUnchecked = Color(0xFFC9CAD4)

    // ---- States ----
    var allAgree by remember { mutableStateOf(false) }
    var appAgree by remember { mutableStateOf(false) }       // (필수)
    var privacyAgree by remember { mutableStateOf(false) }   // (필수)
    var marketingAgree by remember { mutableStateOf(false) } // (선택)

    // 전체동의 → 개별 동기화
    LaunchedEffect(allAgree) {
        if (allAgree) {
            appAgree = true
            privacyAgree = true
            marketingAgree = true
        }
    }
    // 개별이 바뀌면 전체동의 재계산
    LaunchedEffect(appAgree, privacyAgree, marketingAgree) {
        allAgree = appAgree && privacyAgree && marketingAgree
    }

    val requiredOk = appAgree && privacyAgree

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        // 상단 로고 & 타이틀
        // 상단 로고 & 타이틀
        Column(horizontalAlignment = Alignment.Start) {

            val isPreview = LocalInspectionMode.current
            val safeLogoRes = if (isPreview) null else logoResId

            if (safeLogoRes != null) {
                Image(
                    painter = painterResource(id = safeLogoRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Transparent, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color(0xFFE9ECF1), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "LOGO",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = Pretendard,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF8A8D94)
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp)) // ← width가 아니라 height

            Text(
                text = "말뭉치에\n오신 것을 환영합니다.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = Pretendard,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 36.sp
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "회원가입 전, 약관들을 확인해주세요.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp,
                    color = Color(0xFF989898)
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(148.dp))


        // 전체 동의 (세미볼드 18sp)
        TermsRow(
            title = "약관 전체동의",
            checked = allAgree,
            onCheckedChange = { checked ->
                allAgree = checked
                // 해제 시 개별 그대로 (원하면 모두 해제로 바꿀 수 있음)
            },
            blue = blue,
            unchecked = checkboxUnchecked,
            showChevron = false,
            onClick = {},
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = Pretendard, fontSize = 18.sp, fontWeight = FontWeight.SemiBold
            )
        )

        // 여기만 구분선
        Divider(color = grayBorder, thickness = 1.dp)

        // 개별 동의들 — 구분선 제거, 간격 확 줄임(4dp)
        Spacer(Modifier.height(4.dp))
        TermsRow(
            title = annotated("앱 서비스 이용약관 동의", true),
            checked = appAgree,
            onCheckedChange = { appAgree = it },
            blue = blue, unchecked = checkboxUnchecked, onClick = onOpenAppTerms
        )

        Spacer(Modifier.height(4.dp))
        TermsRow(
            title = annotated("개인정보 수집 및 이용 동의", true),
            checked = privacyAgree,
            onCheckedChange = { privacyAgree = it },
            blue = blue, unchecked = checkboxUnchecked, onClick = onOpenPrivacy
        )

        Spacer(Modifier.height(4.dp))
        TermsRow(
            title = annotated("마케팅 활용 동의", false),
            checked = marketingAgree,
            onCheckedChange = { marketingAgree = it },
            blue = blue, unchecked = checkboxUnchecked, onClick = onOpenMarketing
        )


//        // 전체 동의
//        TermsRow(
//            title = "약관 전체동의",
//            checked = allAgree,
//            onCheckedChange = { checked ->
//                allAgree = checked
//                if (!checked) {
//                    // 전체동의 해제 시 개별은 그대로 두지만,
//                    // 원하면 모두 해제하려면 아래 주석 해제
//                    // appAgree = false; privacyAgree = false; marketingAgree = false
//                }
//            },
//            blue = blue,
//            unchecked = checkboxUnchecked,
//            showChevron = false,
//            onClick = {}
//        )
//
//        Divider(color = grayBorder, thickness = 1.dp)
//
//        // 개별 동의들
//        TermsRow(
//            title = annotated("앱 서비스 이용약관 동의", required = true, blue = blue),
//            checked = appAgree,
//            onCheckedChange = { appAgree = it },
//            blue = blue,
//            unchecked = checkboxUnchecked,
//            onClick = onOpenAppTerms
//        )
//        Divider(color = grayBorder, thickness = 1.dp)
//
//        TermsRow(
//            title = annotated("개인정보 수집 및 이용 동의", required = true, blue = blue),
//            checked = privacyAgree,
//            onCheckedChange = { privacyAgree = it },
//            blue = blue,
//            unchecked = checkboxUnchecked,
//            onClick = onOpenPrivacy
//        )
//        Divider(color = grayBorder, thickness = 1.dp)
//
//        TermsRow(
//            title = annotated("마케팅 활용 동의", required = false, blue = blue),
//            checked = marketingAgree,
//            onCheckedChange = { marketingAgree = it },
//            blue = blue,
//            unchecked = checkboxUnchecked,
//            onClick = onOpenMarketing
//        )

        // ⬇️ 여기서 24dp 간격 주고 버튼 배치
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onAgreeContinue,
            enabled = requiredOk,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (requiredOk) Color(0xFF195FCF) else Color(0xFFE0E0E0),
                disabledContainerColor = Color(0xFFE0E0E0),
                contentColor = Color.White,
                disabledContentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            // 내부 여백 기본이 24dp라 오른쪽이 더 넓어 보일 수 있어. 딱 16dp로 고정!
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            Text(
                "시작하기",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

    }
}

@Composable
private fun TermsRow(
    title: Any,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    blue: Color,
    unchecked: Color,
    showChevron: Boolean = true,
    onClick: () -> Unit,
    // ← 개별 행의 텍스트 스타일을 바꿀 수 있게 추가
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.Medium
    )
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)              // ⬅️ 56→44로 더 촘촘하게
            .clickable { onClick() }
            .padding(horizontal = 0.dp, vertical = 0.dp), // ⬅️ 가로 패딩 0, 세로 2
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = blue, uncheckedColor = unchecked, checkmarkColor = Color.White
            )
        )
        Spacer(Modifier.width(6.dp))            // ⬅️ 4→6 (체크박스와 텍스트 최소 간격)
        when (title) {
            is String -> Text(title, style = textStyle, modifier = Modifier.weight(1f))
            else -> Text(title as androidx.compose.ui.text.AnnotatedString,
                style = textStyle, modifier = Modifier.weight(1f))
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF8A8D94),
                modifier = Modifier
                    .size(20.dp)                // ⬅️ 살짝 줄여 시각적 여백 감소
                    .padding(end = 0.dp)        // ⬅️ 우측 16은 부모 Column의 패딩이 보장
            )
        }
    }
}

private fun annotated(base: String, required: Boolean) = buildAnnotatedString {
    append(base); append(" ")
    val tagColor = if (required) Color(0xFF195FCF) else Color(0xFF000000)
    withStyle(SpanStyle(color = tagColor, fontFamily = Pretendard, fontWeight = FontWeight.Medium)) {
        append(if (required) "(필수)" else "(선택)")
    }
}


@Preview(
    name = "Terms - 기본",
    showBackground = true,
    backgroundColor = 0xFFF7F8FA,
    widthDp = 360,
    heightDp = 800,          // ← 높이 명시
    showSystemUi = true
)
@Composable
private fun PreviewTermsAgreementScreen() {
    MaterialTheme {
        Surface {
            Box(Modifier.fillMaxSize()) {   // ← 부모에 사이즈 부여
                TermsAgreementScreen(
                    logoResId = null,
                    onOpenAppTerms = {},
                    onOpenPrivacy = {},
                    onOpenMarketing = {},
                    onAgreeContinue = {}
                )
            }
        }
    }
}