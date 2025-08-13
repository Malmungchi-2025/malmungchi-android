package com.malmungchi.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalInspectionMode


@Composable
fun PrivacyTermsScreen(
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    // 간격/라인하이트 고정값 (AppTerms와 동일 철학)
    val TITLE_BODY_GAP = 8.dp          // 제목 ↔ 본문(또는 문단) 기본 간격
    val SECTION_GAP = 24.dp            // 항목(1,2,3…) 사이 간격
    val BODY_LINE_HEIGHT = 25.6.sp     // 16sp × 1.6

    TermsScaffold(
        title = "상세 약관",           // 앱바는 모든 약관 화면 공통: "상세 약관"
        onBack = onBack,
        onComplete = onDone,
        requireScrollToEnd = true
    ) { _: ScrollState ->

        // 화면 제목(카드 안의 타이틀): 18/SemiBold, 좌측
        Text(
            "개인정보 수집 및 이용 동의서",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Start,
                lineHeight = 26.sp
            )
        )

        Spacer(Modifier.height(32.dp)) // 상단 제목 ↔ 1번 항목 간격 큼

        // 1. 개인정보의 수집 및 이용 목적
        Text(
            "1. 개인정보의 수집 및 이용 목적",
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
            "말뭉치는 회원에게 원활한 서비스 제공을 위해 아래와 같은 목적의 개인정보를 수집 및 이용합니다.",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(8.dp))
        // • 서브 불릿
        listOf(
            "서비스 회원가입 및 로그인",
            "본인확인 및 중복가입 방지",
            "저장한 사용자 데이터의 개인화 제공",
            "서비스 이용 기록 분석 및 맞춤형 콘텐츠 제공",
            "고객 문의 응대 및 공지사항 전달"
        ).forEach {
            Row(Modifier.fillMaxWidth()) {
                Text("•  ",
                    style = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, lineHeight = BODY_LINE_HEIGHT))
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

        // 2. 수집하는 개인정보 항목 (+ 이미지)
        Text(
            "2. 수집하는 개인정보 항목",
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
            "회사는 회원가입 및 서비스 이용과정에서 아래와 같은 개인정보를 수집할 수 있습니다.",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(10.dp))

        // 표 이미지는 피그마 리소스 사용: img_personal
//        Image(
//            painter = painterResource(id = R.drawable.img_personal),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .clip(RoundedCornerShape(8.dp))
//        )
        // 프리뷰(Inspection)에서는 리소스가 없을 수 있으므로 안전 처리
        val inPreview = LocalInspectionMode.current

        // remember/runCatching/try-catch 없이, 단순 분기만
        val imgPainter: Painter? =
            if (inPreview) null
            else painterResource(id = R.drawable.img_personal)

        if (imgPainter != null) {
            Image(
                painter = imgPainter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            // 프리뷰/런타임에서 리소스 없을 때 안전한 플레이스홀더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F3F5))
            ) {
                Text(
                    "이미지 리소스 없음 (img_personal)",
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
            "※ 선택 항목은 입력하지 않아도 서비스 이용에 제한이 없습니다.",
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

        // 3. 개인정보의 보유 및 이용 기간
        Text(
            "3. 개인정보의 보유 및 이용 기간",
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
            "회사는 개인정보의 수집 및 이용 목적이 달성될 때까지 회원의 개인정보를 보유하며, 관련 법령에 따라 일정 기간 동안 보관될 수 있습니다.",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(8.dp))
        listOf(
            "회원 탈퇴 시 즉시 삭제",
            "관련 법령에 따른 보유 기간(전자상거래법, 통신비밀보호법 등)"
        ).forEach {
            Row(Modifier.fillMaxWidth()) {
                Text("•  ",
                    style = TextStyle(fontFamily = Pretendard, fontSize = 16.sp, lineHeight = BODY_LINE_HEIGHT))
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

        // 4. 개인정보의 제3자 제공
        Text(
            "4. 개인정보의 제3자 제공",
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
            "회사는 회원의 동의 없이 개인정보를 제3자에게 제공하지 않습니다. 단, 법령에 의거하여 요구되는 경우에는 예외로 합니다.",
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

        // 5. 개인정보 처리 위탁
        Text(
            "5. 개인정보 처리 위탁",
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
            "회사는 서비스 운영을 위해 필요한 경우 개인정보 처리를 위탁할 수 있으며, 위탁 시 회원에게 사전에 고지하고 동의를 받습니다.",
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

        // 6. 개인정보 보호를 위한 권리
        Text(
            "6. 개인정보 보호를 위한 권리",
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
            "회원은 언제든지 개인정보 열람, 수정, 삭제 요청을 할 수 있으며, 이에 대한 문의는 고객센터를 통해 가능합니다.",
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

        // 7. 동의 거부 권리 및 불이익 안내
        Text(
            "7. 동의 거부 권리 및 불이익 안내",
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
            "회원은 개인정보 수집 및 이용에 대한 동의를 거부할 수 있으며, 다만 동의하지 않을 경우 서비스 이용에 제한이 있을 수 있습니다.",
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

        // 동의 문구 + 부칙
        Text(
            "본 동의서에 동의함으로써, 말뭉치의 개인정보 처리 방침에 따라 개인정보를 제공하는 것에 동의하게 됩니다.",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Start,
                lineHeight = BODY_LINE_HEIGHT
            )
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "[부칙] 본 동의서는 2025년 12월 01일부터 시행됩니다.",
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

@Preview(showBackground = true)
@Composable
fun PreviewPrivacyTermsScreen() {
    PrivacyTermsScreen(
        onBack = {},
        onDone = {}
    )
}