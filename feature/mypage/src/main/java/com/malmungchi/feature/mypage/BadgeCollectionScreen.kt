package com.malmungchi.feature.mypage.badge

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
private val Gray_262626 = Color(0xFF262626)
private val Gray_989898 = Color(0xFF989898)
private val V_SECTION = 48.dp

// ===== Model =====
data class BadgeUi(
    val key: String,
    val title: String,
    val unlocked: Boolean
)

// ===== Drawable Map =====
private val badgeResMap: Map<String, Int> = mapOf(
    "img_badge_1week_attendance" to MyPageR.drawable.img_badge_1week_attendance,
    "img_badge_1month_attendance" to MyPageR.drawable.img_badge_1month_attendance,
    "img_badge_100days_attendance" to MyPageR.drawable.img_badge_100days_attendance,
    "img_badge_first_lesson" to MyPageR.drawable.img_badge_first_lesson,
    "img_badge_five_lessons" to MyPageR.drawable.img_badge_five_lessons,
    "img_badge_first_quizmunch" to MyPageR.drawable.img_badge_first_quizmunch,
    "img_badge_five_quizzes" to MyPageR.drawable.img_badge_five_quizzes,
    "img_badge_first_ai_chat" to MyPageR.drawable.img_badge_first_ai_chat,
    "img_badge_five_ai_chats" to MyPageR.drawable.img_badge_five_ai_chats,
    "img_badge_first_rank" to MyPageR.drawable.img_badge_first_rank,
    "img_badge_rank_1month" to MyPageR.drawable.img_badge_rank_1month,
    "img_badge_rank_100days" to MyPageR.drawable.img_badge_rank_100days,
    "img_badge_bonus" to MyPageR.drawable.img_badge_bonus,
    "img_badge_early_morning" to MyPageR.drawable.img_badge_early_morning,
    "img_badge_five_logins_day" to MyPageR.drawable.img_badge_five_logins_day
)

// ===== Entry Point =====

@Composable
fun BadgeCollectionRoute(
    onBack: () -> Unit,
    viewModel: BadgeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 🚀 진입 시 API 호출
    LaunchedEffect(Unit) {
        viewModel.loadBadges()
    }

    when (val state = uiState) {
        is BadgeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is BadgeUiState.Success -> {
            BadgeCollectionScreen(
                onBack = onBack,
                badges = state.badges
            )
        }

        is BadgeUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "배지 로드 실패: ${state.message}",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                )
            }
        }
        else -> Unit // ✅ 추가 (모든 경우 처리했다고 인식)
    }
}
//@Composable
//fun BadgeCollectionRoute(onBack: () -> Unit) {
//    val allBadges = remember {
//        listOf(
//            BadgeUi("img_badge_1week_attendance", "일주일 출석", true),
//            BadgeUi("img_badge_1month_attendance", "한 달 출석", false),
//            BadgeUi("img_badge_100days_attendance", "100일 출석", false),
//            BadgeUi("img_badge_first_lesson", "오늘의 학습\n첫 학습 완료", true),
//            BadgeUi("img_badge_five_lessons", "오늘의 학습\n5회 학습 완료", false),
//            BadgeUi("img_badge_first_quizmunch", "퀴즈뭉치\n첫 학습 완료", true),
//            BadgeUi("img_badge_five_quizzes", "퀴즈뭉치\n5회 학습 완료", false),
//            BadgeUi("img_badge_first_ai_chat", "AI 대화\n첫 학습 완료", true),
//            BadgeUi("img_badge_five_ai_chats", "AI 대화\n5회 학습 완료", false),
//            BadgeUi("img_badge_first_rank", "처음 1등 달성", true),
//            BadgeUi("img_badge_bonus", "보너스 배지", true),
//            BadgeUi("img_badge_early_morning", "새벽 학습", true),
//            BadgeUi("img_badge_five_logins_day", "하루 5회 학습", false)
//        )
//    }
//
//    BadgeCollectionScreen(onBack = onBack, badges = allBadges)
//}

// ===== Screen =====
@Composable
private fun BadgeCollectionScreen(
    onBack: () -> Unit,
    badges: List<BadgeUi>
) {
    var selectedBadge by remember {
        mutableStateOf<BadgeUi?>(badges.firstOrNull { it.key == "img_badge_1week_attendance" })
    }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(48.dp))

        // === Top Bar ===
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
                Icon(
                    painter = painterResource(id = MyPageR.drawable.ic_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }
        }

        // === 나의 대표 배지 ===
        Spacer(Modifier.height(24.dp))
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

        // 💬 설명 문구 (대표 배지 위)
        Text(
            text = "모은 배지 중 가장 보람찬 배지를 골라\n대표 배지로 설정해주세요!",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Gray_989898,
                lineHeight = 14.sp * 1.6f
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(20.dp))

        // === 대표 배지 카드 ===
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp) // ✅ 살짝 줄여서 가운데로 보이게
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (selectedBadge != null) {
                    val resId =
                        badgeResMap[selectedBadge!!.key] ?: MyPageR.drawable.ic_lock_on
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = selectedBadge!!.title,
                        modifier = Modifier.size(120.dp) // ✅ 1.2x 배 크기
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = selectedBadge!!.title,
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.Black
                        ),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "아직 대표 배지를 선택하지 않았어요!",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Gray_989898
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // === 전체 배지 리스트 ===
        Spacer(Modifier.height(V_SECTION))
        BadgeGrid(
            badges = badges,
            onSelect = { selectedBadge = it }
        )

        Spacer(Modifier.height(60.dp))
    }
}

// ===== Grid =====
@Composable
private fun BadgeGrid(
    badges: List<BadgeUi>,
    onSelect: (BadgeUi) -> Unit
) {
    val rows = badges.chunked(3)
    Column(modifier = Modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, row ->
            if (index != 0) Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { badge ->
                    BadgeCell(badge = badge, onSelect = onSelect)
                }
                repeat(3 - row.size) { Spacer(Modifier.width(88.dp)) }
            }
        }
    }
}

// ===== Cell =====
@Composable
private fun BadgeCell(
    badge: BadgeUi,
    onSelect: (BadgeUi) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .widthIn(min = 88.dp)
            .clickable(enabled = badge.unlocked) { onSelect(badge) }
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (!badge.unlocked) {
                Icon(
                    painter = painterResource(id = MyPageR.drawable.ic_lock_on),
                    contentDescription = "잠금 배지",
                    modifier = Modifier.size(32.dp),
                    tint = Color.Unspecified
                )
            } else {
                val resId = badgeResMap[badge.key] ?: MyPageR.drawable.ic_lock_on
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = badge.title,
                    modifier = Modifier.size(88.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = badge.title,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Gray_262626,
                lineHeight = 14.sp * 1.4f
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
