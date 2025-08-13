package com.malmungchi.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

@Composable
fun MarketingTermsScreen(
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    // 간격/라인하이트 고정값 (PrivacyTermsScreen과 동일)
    val TITLE_BODY_GAP = 8.dp          // 제목 ↔ 본문(또는 문단) 기본 간격
    val SECTION_GAP = 24.dp            // 항목(1,2,3…) 사이 간격
    val BODY_LINE_HEIGHT = 25.6.sp     // 16sp × 1.6

    TermsScaffold(
        title = "상세 약관",           // 앱바는 공통: "상세 약관"
        onBack = onBack,
        onComplete = onDone,
        requireScrollToEnd = true
    ) { _: ScrollState ->

        // 화면 제목: 18/SemiBold, 좌측 정렬
        Text(
            "마케팅 수신 동의서",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
                lineHeight = 26.sp
            )
        )

        Spacer(Modifier.height(32.dp))

        // 안내 문장
        Text(
            "말뭉치는 이용자에게 맞춤혜택 및 유용한 정보를 전달하기 위해 아래와 같이 마케팅 목적의 개인정보 수집·이용 및 광고성 정보 수신에 대한 동의를 요청드립니다.",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )

        Spacer(Modifier.height(SECTION_GAP))

        // 1. 수집 및 이용 목적
        Text(
            "1. 수집 및 이용 목적",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(TITLE_BODY_GAP))
        listOf(
            "이벤트, 혜택, 프로모션 등 마케팅 정보 제공",
            "앱 기능 안내 및 맞춤형 콘텐츠 추천",
            "설문조사 및 사용자 참여 프로그램 안내"
        ).forEach {
            Row(Modifier.fillMaxWidth()) {
                Text("•  ",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        lineHeight = BODY_LINE_HEIGHT
                    )
                )
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                        lineHeight = BODY_LINE_HEIGHT
                    )
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(SECTION_GAP))

        // 2. 수집 항목 (+ 이미지)
        Text(
            "2. 수집 항목",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(TITLE_BODY_GAP))
        // 프리뷰(Inspection)에서는 리소스 접근 금지
        val inPreview = LocalInspectionMode.current
        val marketingPainter: Painter? =
            if (inPreview) null else painterResource(id = R.drawable.img_marketing)

        if (marketingPainter != null) {
            Image(
                painter = marketingPainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            // 프리뷰/리소스 없을 때 플레이스홀더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F3F5))
            ) {
                Text(
                    "이미지 리소스 없음 (img_marketing)",
                    modifier = Modifier.padding(12.dp),
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF61636B),
                        textAlign = TextAlign.Start,
                        lineHeight = 16.sp
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "※ 본 항목은 마케팅 정보 제공을 위해 별도로 수집되며, 동의하지 않아도 서비스 이용에는 제한이 없습니다.",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )

        Spacer(Modifier.height(SECTION_GAP))

        // 3. 보유 및 이용 기간
        Text(
            "3. 보유 및 이용 기간",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(TITLE_BODY_GAP))
        listOf(
            "동의 철회 또는 회원 탈퇴 시까지",
            "단, 관련 법령에 따른 보존 의무가 있는 경우 해당 기간까지 보관"
        ).forEach {
            Row(Modifier.fillMaxWidth()) {
                Text("•  ",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        lineHeight = BODY_LINE_HEIGHT
                    )
                )
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                        lineHeight = BODY_LINE_HEIGHT
                    )
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(SECTION_GAP))

        // 4. 전송 방법
        Text(
            "4. 전송 방법",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(TITLE_BODY_GAP))
        Text(
            "이메일, 앱 푸시 알림, 인앱 메시지 등",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )

        Spacer(Modifier.height(SECTION_GAP))

        // 5. 동의 거부 권리 및 불이익
        Text(
            "5. 동의 거부 권리 및 불이익",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(TITLE_BODY_GAP))
        Text(
            "본 동의는 선택 사항이며, 동의하지 않더라도 서비스 이용에 제한은 없습니다.",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )

        Spacer(Modifier.height(48.dp))
    }
}

/* ---------- 프리뷰 ---------- */
@Preview(showBackground = true)
@Composable
fun PreviewMarketingTermsScreen() {
    MaterialTheme {
        MarketingTermsScreen(
            onBack = {},
            onDone = {}
        )
    }
}