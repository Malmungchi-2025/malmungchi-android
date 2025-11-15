package com.malmungchi.feature.quiz


// ===== Imports (필수) =====
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.quiz.R



// Pretendard: 프로젝트 공통 FontFamily로 교체해서 사용하세요.


// ===== 치수 상수 =====
private val GAP = 28.dp
private val CARD_HEIGHT_SMALL = 154.dp   // 심화 / 기초 / 활용 / 고급
private val CARD_HEIGHT_BIG = 336.dp     // 취업 준비

@Composable
fun QuizScreen(
    onClickJobPrep: () -> Unit = {},
    onClickBasic: () -> Unit = {},
    onClickPractice: () -> Unit = {},
    onClickDeep: () -> Unit = {},
    onClickAdvanced: () -> Unit = {}
) {
    val shape = RoundedCornerShape(20.dp)
    val cardBg = Color(0xFFF7F7F7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        // 제목: Pretendard 24 / SemiBold
        Text(
            text = "퀴즈뭉치",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 28.dp),
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF111111)
            )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(GAP)
        ) {
            // 좌/우 전체 높이 계산 → 짧은 쪽에만 보정 Spacer 추가
            val leftTotal  = CARD_HEIGHT_BIG + GAP + CARD_HEIGHT_SMALL               // 328 + 12 + 154 = 494
            val rightTotal = CARD_HEIGHT_SMALL * 3 + GAP * 2                         // 154*3 + 12*2 = 486
            val rightNeeds = if (leftTotal > rightTotal) leftTotal - rightTotal else 0.dp
            val leftNeeds  = if (rightTotal > leftTotal) rightTotal - leftTotal else 0.dp

            // ===== 왼쪽: 큰 카드 + 심화 (PNG 배지 오버레이) =====
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    BigIllustrationCard(
                        title = "취업\n준비",
                        imageRes = R.drawable.img_quiz_mungchi,
                        background = cardBg,
                        shape = shape,
                        onClick = onClickJobPrep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CARD_HEIGHT_BIG)
                    )

                    Spacer(Modifier.height(GAP))

                    QuizInfoCard(
                        title = "심화",
                        subtitle = "복잡한 문장 이해\n문어체 & 학술적 표현",
                        background = cardBg,
                        shape = shape,
                        onClick = onClickDeep,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CARD_HEIGHT_SMALL)
                            //.offset(y = (-8).dp)
                    )

                    // 왼쪽이 더 짧다면 여기서만 보정
                    if (leftNeeds > 0.dp) Spacer(Modifier.height(leftNeeds))
                }

                // PNG 배지 오버레이 (레이아웃 공간 차지 X)
                val inPreview = LocalInspectionMode.current

            }

            // ===== 오른쪽: 기초 / 활용 / 고급 =====
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(GAP)
            ) {
                QuizInfoCard(
                    title = "기초",
                    subtitle = "단순한 문장 이해\n기본 어휘",
                    background = cardBg,
                    shape = shape,
                    onClick = onClickBasic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CARD_HEIGHT_SMALL)
                )

                QuizInfoCard(
                    title = "활용",
                    subtitle = "다양한 문형 활용 가능\n실용 어휘",
                    background = cardBg,
                    shape = shape,
                    onClick = onClickPractice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CARD_HEIGHT_SMALL)
                )

                QuizInfoCard(
                    title = "고급",
                    subtitle = "심층적 사고와 비판적 이해\n전문 용어 & 고급 어휘",
                    background = cardBg,
                    shape = shape,
                    onClick = onClickAdvanced,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CARD_HEIGHT_SMALL)
                )

                // 오른쪽이 더 짧다면 여기서만 보정 (현재 케이스: 8.dp)
                if (rightNeeds > 0.dp) Spacer(Modifier.height(rightNeeds))
            }
        }
    }
}

/* ---------- 카드 컴포넌트 ---------- */

@Composable
private fun QuizInfoCard(
    title: String,
    subtitle: String,
    background: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(4.dp, shape, clip = false)
            .clip(shape)
            .background(background)
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 타이틀: Pretendard 20 / SemiBold
        Text(
            text = title,
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111)
            )
        )
        // 서브: 12 / Medium / 150% / #989898
        Text(
            text = subtitle,
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp, // 150%
                color = Color(0xFF989898)
            )
        )
    }
}

@Composable
private fun BigIllustrationCard(
    title: String,
    imageRes: Int,
    background: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(background)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111111),
                lineHeight = 32.sp
            ),
            modifier = Modifier.align(Alignment.TopStart)
        )
        // 이미지 (Preview에서도 항상 표시)
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .height(246.89.dp),
            contentScale = ContentScale.Fit
        )

        //val inPreview = LocalInspectionMode.current

    }
}

/* ---------- 프리뷰 ---------- */
@Preview(showBackground = true)
@Composable
private fun PreviewQuizScreen() {
    MaterialTheme {
        Surface { QuizScreen() }
    }
}