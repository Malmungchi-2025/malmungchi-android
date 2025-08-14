package com.malmungchi.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp

// 공통 버튼 규격 (다른 화면과 동일)
private val CommonButtonHeight = 52.dp
private val CommonButtonCorner = 14.dp
private val HorizontalPadding16 = 16.dp

// 색상
private val BrandBlue = Color(0xFF195FCF)
private val DisabledGray = Color(0xFFC9CAD4)

/**
 * 상세 약관 공통 스캐폴드
 * - 상단 앱바
 * - 본문(가운데 정렬)
 * - 체크박스 없음 (버튼으로 대체)
 * - 하단 버튼: 좌우 16, 높이 52, 라운드 14
 *
 * @param requireScrollToEnd true면 스크롤 끝에 도달해야 버튼 활성화
 */

@Composable
fun TermsScaffold(
    title: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    buttonText: String = "약관에 동의합니다.",
    requireScrollToEnd: Boolean = true,
   // extraTopPadding: Dp = 0.dp,
    content: @Composable ColumnScope.(ScrollState) -> Unit
) {
    val scrollState = rememberScrollState()
    val atBottom by remember { derivedStateOf { scrollState.value >= scrollState.maxValue } }
    val buttonEnabled = if (requireScrollToEnd) atBottom else true

    Column(Modifier.fillMaxSize()) {
        // 상태바 높이만큼 안전하게 띄우기 (선택)
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        // 원하는 추가 여백 16dp
        Spacer(Modifier.height(12.dp))
        // 상단바
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                // ← 아이콘 이미지 교체
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null
                )
            }
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))

        // 본문
        Column(
            Modifier
                .weight(1f)
                .padding(
                    start = 16.dp, end = 16.dp,
                    top = 52.dp,           // ← 앱바 아래 40dp
                    bottom = 24.dp
                )
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            content(scrollState)
        }

        // 하단 버튼 (동일)
        Divider(thickness = 1.dp, color = Color(0xFFE0E0E0))
        Button(
            onClick = onComplete,
            enabled = buttonEnabled,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (buttonEnabled) Color(0xFF195FCF) else Color(0xFFC9CAD4),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFC9CAD4),
                disabledContentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                buttonText,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

/* ===================== 텍스트 헬퍼: 피그마 스펙 ======================= */
private val SECTION_GAP = 32.dp        // 각 조 사이 간격
private val BODY_LINE_HEIGHT = 25.6.sp // 16sp × 1.6 = 25.6sp
private val TITLE_BODY_GAP = 8.dp    // 필요하면 12~16.dp로 조절



@Composable
private fun Title18Semi(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
            fontFamily = Pretendard,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            lineHeight = 26.sp
        )
    )
}

@Composable
private fun Section16Medium(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            lineHeight = BODY_LINE_HEIGHT
        )
    )
}

@Composable
private fun Body16Medium(text: String) {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = TextStyle(
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            lineHeight = BODY_LINE_HEIGHT
        )
    )
}

@Composable
private fun NumberedList16Medium(items: List<String>, gap: Dp = 8.dp) {
    items.forEachIndexed { i, line ->
        Body16Medium("${i + 1}. $line")
        if (i != items.lastIndex) Spacer(Modifier.height(gap))
    }
}
/* ---------- 미리보기 ---------- */

@Preview(
    name = "TermsScaffold - Disabled (짧은 본문)",
    showBackground = true,
    backgroundColor = 0xFFF7F8FA,
    widthDp = 360, heightDp = 800
)
@Composable
private fun PreviewTermsScaffold_Disabled() {
    MaterialTheme {
        TermsScaffold(
            title = "상세 약관",
            onBack = {},
            onComplete = {},
            requireScrollToEnd = true
        ) { _ ->
            Text(
                "앱 서비스 이용약관",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "프리뷰용 더미 텍스트",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Preview(
    name = "TermsScaffold - Enabled (스크롤 끝 요구 X)",
    showBackground = true,
    backgroundColor = 0xFFF7F8FA,
    widthDp = 360, heightDp = 800
)
@Composable
private fun PreviewTermsScaffold_Enabled() {
    MaterialTheme {
        TermsScaffold(
            title = "상세 약관",
            onBack = {},
            onComplete = {},
            requireScrollToEnd = false
        ) { _ ->
            Text(
                "앱 서비스 이용약관",
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(Modifier.height(12.dp))
            repeat(20) {
                Text(
                    "충분히 긴 본문 $it",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(48.dp))
        }
    }
}