package com.malmungchi.feature.login

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp


@Composable
fun AppTermsScreen(
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    TermsScaffold(
        title = "상세 약관",
        onBack = onBack,
        onComplete = onDone,
        requireScrollToEnd = true
    ) { _: ScrollState ->

        val TITLE_BODY_GAP = 6.dp    // 필요하면 12~16.dp로 조절

        Title18Semi("앱 서비스 이용약관")
        Spacer(Modifier.height(32.dp))

        Section16Medium("제 1조 (목적)")
        Spacer(Modifier.height(TITLE_BODY_GAP)) // ← 조 제목과 본문 사이 간격
        Body16Medium("본 약관은 말뭉치가 제공하는 어휘문해력 증진 서비스의 이용과 관련하여 회사(이하 Team. 말뭉치)와 이용자(이하 “회원”) 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.")

        Spacer(Modifier.height(16.dp))

        Section16Medium("제 2조 (약관의 효력 및 변경)")
        Spacer(Modifier.height(TITLE_BODY_GAP)) // ← 조 제목과 본문 사이 간격
        NumberedList16Medium(
            listOf(
                "본 약관은 서비스를 이용하고자 하는 모든 회원에게 적용됩니다.",
                "회사는 필요한 경우 관련 법령을 위배하지 않는 범위에서 본 약관을 변경할 수 있으며, 변경된 약관은 적용 7일 전(중대한 사항은 30일 전)부터 회원에게 공지됩니다.",
                "회원이 변경된 약관에 동의하지 않을 경우 서비스 이용을 중단할 수 있으며, 지속적인 이용 시 변경된 약관에 동의한 것으로 간주됩니다."
            )
        )

        Spacer(Modifier.height(16.dp))

        Section16Medium("제 3조 (서비스 이용 및 제한)")
        Spacer(Modifier.height(TITLE_BODY_GAP)) // ← 조 제목과 본문 사이 간격
        NumberedList16Medium(
            listOf(
                "회사는 회원에게 사용자 수준 맞춤형 학습 서비스를 포함한 다양한 콘텐츠 및 기능을 제공합니다.",
                "회사는 서비스의 운영상 필요에 따라 서비스의 일부 또는 전부를 변경, 중단할 수 있습니다.",
                "회원은 서비스 이용 시 관련 법령 및 본 약관을 준수해야하며, 타인의 권리를 침해하거나 공공질서를 해치는 행위를 해서는 안 됩니다."
            )
        )

        Spacer(Modifier.height(16.dp))

        Section16Medium("제 4조 (회원가입 및 계정관리)")
        Spacer(Modifier.height(TITLE_BODY_GAP)) // ← 조 제목과 본문 사이 간격
        NumberedList16Medium(
            listOf(
                "회원가입은 이메일 로그인을 통해 진행되며, 회원은 정확한 정보를 제공해야 합니다.",
                "회원 계정의 관리 책임은 회원 본인에게 있으며, 제3자에게 계정을 양도 또는 공유할 수 없습니다.",
                "회사는 회원이 허위 정보를 제공하거나, 타인의 정보를 도용하는 경우 계정을 제한하거나 삭제할 수 있습니다."
            )
        )

        Spacer(Modifier.height(16.dp))

        Section16Medium("제 5조 (개인정보 보호 및 이용)")
        Spacer(Modifier.height(TITLE_BODY_GAP)) // ← 조 제목과 본문 사이 간격
        NumberedList16Medium(
            listOf(
                "회사는 관련 법령에 따라 회원의 개인정보를 보호하며, 개인정보의 수집 및 이용에 대한 사항은 개인정보 처리방침에 따릅니다.",
                "회사는 회원의 동의없이 개인정보를 제3자에게 제공하지 않으며, 서비스 운영을 위해 필요한 경우에만 최소한의 정보를 이용합니다."
            )
        )

        Spacer(Modifier.height(16.dp))

        Section16Medium("제 6조 (이용제한 및 계약 해지)")
        Spacer(Modifier.height(TITLE_BODY_GAP)) // ← 조 제목과 본문 사이 간격
        NumberedList16Medium(
            listOf(
                "회원이 본 약관을 위반하거나 서비스 운영에 지장을 초래하는 경우, 회사는 사전 통지없이 회원의 서비스 이용을 제한하거나 계약을 해지할 수 있습니다.",
                "회원은 언제든지 서비스 이용을 중단하고 계정을 삭제할 수 있습니다."
            )
        )

        Spacer(Modifier.height(16.dp))

        Section16Medium("제 7조 (면책조항)")
        Spacer(Modifier.height(TITLE_BODY_GAP)) // ← 조 제목과 본문 사이 간격
        NumberedList16Medium(
            listOf(
                "회사는 천재지변, 기술적 장애 등 불가항력적인 사유로 서비스 제공이 불가능한 경우 이에 대한 책임을 지지 않습니다.",
                "회원이 본인의 부주의로 인해 발생한 손해에 대해 회사는 책임을 지지 않습니다."
            )
        )

        Spacer(Modifier.height(16.dp))

        Section16Medium("제 8조 (준거법 및 관할 법원)")
        Spacer(Modifier.height(TITLE_BODY_GAP)) // ← 조 제목과 본문 사이 간격
        Body16Medium("본 약관과 관련된 분쟁은 대한민국 법을 준거법으로 하며, 관할 법원은 회사의 본사 소재지를 관할하는 법원으로 합니다.")

        Spacer(Modifier.height(24.dp))
        Body16Medium("[부칙] 본 약관은 2025년 12월 01일부터 시행됩니다.")
        Spacer(Modifier.height(48.dp))
    }
}

/* ==== 텍스트 헬퍼 ==== */

// 큰 제목: Pretendard 18, SemiBold, 좌측 정렬
@Composable
private fun Title18Semi(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        style = TextStyle(
            fontFamily = Pretendard,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
            lineHeight = 26.sp
        )
    )
}

// 섹션 제목: Pretendard 16, Medium, 좌측 정렬
@Composable
private fun Section16Medium(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        style = TextStyle(
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp
        )
    )
}

// 본문: Pretendard 16, Medium, 좌측 정렬
@Composable
private fun Body16Medium(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp),
        style = TextStyle(
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp
        )
    )
}

// 숫자 목록: 1., 2. 그대로 표시
@Composable
private fun NumberedList16Medium(items: List<String>, gap: Dp = 8.dp) {
    items.forEachIndexed { idx, line ->
        Body16Medium("${idx + 1}. $line")
        if (idx != items.lastIndex) Spacer(Modifier.height(gap))
    }
}

/* ==== 프리뷰 ==== */

@Preview(
    name = "AppTermsScreen",
    showBackground = true,
    backgroundColor = 0xFFF7F8FA,
    widthDp = 360, heightDp = 800
)
@Composable
private fun Preview_AppTermsScreen() {
    MaterialTheme {
        AppTermsScreen(onBack = {}, onDone = {})
    }
}

//@Composable
//fun PrivacyTermsScreen(
//    onBack: () -> Unit,
//    onDone: () -> Unit
//) {
//    TermsScaffold(
//        title = "개인정보 수집 및 이용",
//        onBack = onBack,
//        onComplete = onDone,
//        requireScrollToEnd = true
//    ) { _: ScrollState ->
//        // 🔹 상세 약관: 가운데 정렬 + Pretendard 20 SemiBold
//        // 본문 시작
//        Text(
//            text = "앱 서비스 이용약관",
//            style = MaterialTheme.typography.titleSmall.copy(
//                fontFamily = Pretendard,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        SectionTitle("제 1조 (목적)")
//        Paragraph(
//            "본 약관은 말뭉치가 제공하는 어휘문해력 증진 서비스의 이용과 관련하여 회사(이하 Team. 말뭉치)와 이용자(이하 “회원”) 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다."
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        SectionTitle("제 2조 (약관의 효력 및 변경)")
//        Numbered(
//            listOf(
//                "본 약관은 서비스를 이용하고자 하는 모든 회원에게 적용됩니다.",
//                "회사는 필요한 경우 관련 법령을 위배하지 않는 범위에서 본 약관을 변경할 수 있으며, 변경된 약관은 적용 7일 전(중대한 사항은 30일 전)부터 회원에게 공지됩니다.",
//                "회원이 변경된 약관에 동의하지 않을 경우 서비스 이용을 중단할 수 있으며, 지속적인 이용 시 변경된 약관에 동의한 것으로 간주됩니다."
//            )
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        SectionTitle("제 3조 (서비스 이용 및 제한)")
//        Numbered(
//            listOf(
//                "회사는 회원에게 사용자 수준 맞춤형 학습 서비스를 포함한 다양한 콘텐츠 및 기능을 제공합니다.",
//                "회사는 서비스의 운영상 필요에 따라 서비스의 일부 또는 전부를 변경, 중단할 수 있습니다.",
//                "회원은 서비스 이용 시 관련 법령 및 본 약관을 준수해야하며, 타인의 권리를 침해하거나 공공질서를 해치는 행위를 해서는 안 됩니다."
//            )
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        SectionTitle("제 4조 (회원가입 및 계정관리)")
//        Numbered(
//            listOf(
//                "회원가입은 이메일 로그인을 통해 진행되며, 회원은 정확한 정보를 제공해야 합니다.",
//                "회원 계정의 관리 책임은 회원 본인에게 있으며, 제3자에게 계정을 양도 또는 공유할 수 없습니다.",
//                "회사는 회원이 허위 정보를 제공하거나, 타인의 정보를 도용하는 경우 계정을 제한하거나 삭제할 수 있습니다."
//            )
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        SectionTitle("제 5조 (개인정보 보호 및 이용)")
//        Numbered(
//            listOf(
//                "회사는 관련 법령에 따라 회원의 개인정보를 보호하며, 개인정보의 수집 및 이용에 대한 사항은 개인정보 처리방침에 따릅니다.",
//                "회사는 회원의 동의없이 개인정보를 제3자에게 제공하지 않으며, 서비스 운영을 위해 필요한 경우에만 최소한의 정보를 이용합니다."
//            )
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        SectionTitle("제 6조 (이용제한 및 계약 해지)")
//        Numbered(
//            listOf(
//                "회원이 본 약관을 위반하거나 서비스 운영에 지장을 초래하는 경우, 회사는 사전 통지없이 회원의 서비스 이용을 제한하거나 계약을 해지할 수 있습니다.",
//                "회원은 언제든지 서비스 이용을 중단하고 계정을 삭제할 수 있습니다."
//            )
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        SectionTitle("제 7조 (면책조항)")
//        Numbered(
//            listOf(
//                "회사는 천재지변, 기술적 장애 등 불가항력적인 사유로 서비스 제공이 불가능한 경우 이에 대한 책임을 지지 않습니다.",
//                "회원이 본인의 부주의로 인해 발생한 손해에 대해 회사는 책임을 지지 않습니다."
//            )
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        SectionTitle("제 8조 (준거법 및 관할 법원)")
//        Paragraph(
//            "본 약관과 관련된 분쟁은 대한민국 법을 준거법으로 하며, 관할 법원은 회사의 본사 소재지를 관할하는 법원으로 합니다."
//        )
//
//        Spacer(Modifier.height(24.dp))
//        Paragraph("[부칙] 본 약관은 2025년 12월 01일부터 시행됩니다.")
//        Spacer(Modifier.height(48.dp)) // 하단 버튼과 간섭 방지용 여유
//    }
//}
//
///* ==== 작은 UI 헬퍼 ==== */
//
//@Composable
//private fun SectionTitle(text: String) {
//    Text(
//        text = text,
//        style = MaterialTheme.typography.bodyMedium.copy(
//            fontFamily = Pretendard,
//            fontSize = 16.sp,
//            fontWeight = FontWeight.Medium
//        )
//    )
//}
//
//@Composable
//private fun Paragraph(text: String) {
//    Text(
//        text = text,
//        style = MaterialTheme.typography.bodyMedium.copy(
//            fontFamily = Pretendard,
//            fontSize = 16.sp,
//            fontWeight = FontWeight.Medium,
//            lineHeight = 22.sp
//        )
//    )
//}
//
//@Composable
//private fun Numbered(items: List<String>, gap: Int = 6) {
//    Column {
//        items.forEachIndexed { idx, s ->
//            Row(Modifier.fillMaxWidth()) {
//                Text(
//                    "${idx + 1}. ",
//                    style = MaterialTheme.typography.bodyMedium.copy(
//                        fontFamily = Pretendard,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                )
//                Text(
//                    s,
//                    modifier = Modifier.weight(1f),
//                    style = MaterialTheme.typography.bodyMedium.copy(
//                        fontFamily = Pretendard,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium,
//                        lineHeight = 22.sp
//                    )
//                )
//            }
//            if (idx != items.lastIndex) Spacer(Modifier.height(gap.dp))
//        }
//    }
//}
//
//@Preview(
//    name = "AppTermsScreen",
//    showBackground = true,
//    backgroundColor = 0xFFF7F8FA,
//    widthDp = 360, heightDp = 800, showSystemUi = false
//)
//@Composable
//private fun PreviewAppTermsScreen() {
//    var agreed by remember { mutableStateOf(false) }
//
//    MaterialTheme {
//        AppTermsScreen(
//            agreed = agreed,
//            onAgreeChange = { agreed = it },
//            onBack = { },
//            onDone = { }
//        )
//    }
//}