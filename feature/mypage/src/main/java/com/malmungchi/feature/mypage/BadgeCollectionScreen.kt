package com.malmungchi.feature.mypage.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.mypage.R as MyPageR

// ===== Tokens =====
private val Blue_195FCF = Color(0xFF195FCF)
private val Gray_989898 = Color(0xFF989898)
private val Gray_262626 = Color(0xFF262626)
private val Bg_EFF4FB = Color(0xFFEFF4FB)

private val H_PADDING = 20.dp
private val V_SECTION = 48.dp
private val CardCorner = 16.dp

// ===== Public Entry =====
@Composable
fun BadgeCollectionRoute(
    onBack: () -> Unit,                    // ← ic_back 누르면 MyPageScreen으로
) {
    // 더미 데이터 (서버 연동 시 대체)
    val attendanceBadges = remember {
        listOf(
            BadgeUi("학습 1000일 달성", unlocked = true),
            BadgeUi("일주일 출석", unlocked = false),
            BadgeUi("일주일 출석", unlocked = false)
        )
    }
    val studyBadges = remember {
        listOf(
            BadgeUi("학습 1000일 달성", unlocked = false),
            BadgeUi("일주일 출석", unlocked = false),
            BadgeUi("일주일 출석", unlocked = false)
        )
    }

    BadgeCollectionScreen(
        onBack = onBack,
        representativeTitle = "학습 1000일 달성",
        attendanceBadges = attendanceBadges,
        studyBadges = studyBadges
    )
}

// ===== Screen =====
@Composable
private fun BadgeCollectionScreen(
    onBack: () -> Unit,
    representativeTitle: String,
    attendanceBadges: List<BadgeUi>,
    studyBadges: List<BadgeUi>
) {
    val isPreview = LocalInspectionMode.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = H_PADDING)
    ) {

        Spacer(Modifier.height(12.dp))
        // TopBar
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "배지 수집함",
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color.Black
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                if (isPreview) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기"
                    )
                } else {
                    Icon(
                        painter = painterResource(id = MyPageR.drawable.ic_back),
                        contentDescription = "뒤로가기",
                        tint = Color.Unspecified  // 원본 이미지 색 사용
                    )
                }
            }
        }

        // ===== 나의 대표 배지 =====
        Spacer(Modifier.height(V_SECTION))
        Text(
            text = "나의 대표 배지",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Color.Black
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(CardCorner),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "모은 배지 중 가장 보람찬 배지를 골라\n대표 배지로 설정해주세요!",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Gray_989898,
                        lineHeight = 14.sp * 1.6f  // 줄간격 160%
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // 동그라미 (배지 이미지 자리)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Bg_EFF4FB),
                    contentAlignment = Alignment.Center
                ) {
                    // 배지 이미지가 생기면 여기서 교체
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = representativeTitle, // "학습 1000일 달성"
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // ===== 출석 배지 =====
        Spacer(Modifier.height(V_SECTION))
        SectionTitle("출석 배지")

        Spacer(Modifier.height(16.dp))
        BadgeRow(badges = attendanceBadges)

        // ===== 학습 배지 =====
        Spacer(Modifier.height(V_SECTION))
        SectionTitle("학습 배지")

        Spacer(Modifier.height(16.dp))
        BadgeRow(badges = studyBadges)

        Spacer(Modifier.height(24.dp))
    }
}

// ===== UI Pieces =====
@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color.Black
        )
    )
}

data class BadgeUi(
    val title: String,
    val unlocked: Boolean
)

@Composable
private fun BadgeRow(
    badges: List<BadgeUi>
) {
    // 한 줄에 3개씩
    val rows = badges.chunked(3)
    Column(modifier = Modifier.fillMaxWidth()) {
        rows.forEachIndexed { rowIdx, row ->
            if (rowIdx != 0) Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { badge ->
                    BadgeCell(badge)
                }
                // 3개 미만이면 더미 공간으로 정렬 유지
                repeat(3 - row.size) {
                    Spacer(Modifier.width(88.dp))
                }
            }
        }
    }
}

@Composable
private fun BadgeCell(badge: BadgeUi) {
    val isPreview = LocalInspectionMode.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 88.dp)
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Bg_EFF4FB),
            contentAlignment = Alignment.Center
        ) {
            if (isPreview) {
                // 프리뷰에서는 그냥 잠금 아이콘만 보여주기
                val vector: ImageVector = Icons.Filled.Lock
                Icon(
                    imageVector = vector,
                    contentDescription = "프리뷰 잠금",
                    modifier = Modifier.size(28.dp)
                )
            } else {
                // 런타임: 모듈 리소스 사용
                Image(
                    painter = painterResource(
                        id = if (badge.unlocked)
                            MyPageR.drawable.ic_lock_off
                        else
                            MyPageR.drawable.ic_lock_on
                    ),
                    contentDescription = if (badge.unlocked) "잠금 해제" else "잠금",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = badge.title, // 예: "학습 1000일 달성", "일주일 출석"
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 14.sp * 1.4f,   // 줄간격 140%
                color = Gray_262626
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ===== Preview =====
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = true)
@Composable
private fun BadgeCollectionPreview() {
    BadgeCollectionRoute(onBack = {})
}
