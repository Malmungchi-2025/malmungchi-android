package com.malmungchi.feature.mypage.badge

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.mypage.MyPageViewModel
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

private val badgeResMap: Map<String, Int> = mapOf(
    // --- 출석 관련 ---
    "img_badge_1week_attendance" to MyPageR.drawable.img_badge_1week_attendance,
    "img_badge_1month_attendance" to MyPageR.drawable.img_badge_1month_attendance,
    "img_badge_100days_attendance" to MyPageR.drawable.img_badge_100days_attendance,

    // --- 학습 관련 ---
    "img_badge_first_lesson" to MyPageR.drawable.img_badge_first_lesson,
    "img_badge_five_lessons" to MyPageR.drawable.img_badge_five_lessons,
    "img_badge_first_quizmunch" to MyPageR.drawable.img_badge_first_quizmunch,
    "img_badge_five_quizzes" to MyPageR.drawable.img_badge_five_quizzes,
    "img_badge_first_ai_chat" to MyPageR.drawable.img_badge_first_ai_chat,
    "img_badge_five_ai_chats" to MyPageR.drawable.img_badge_five_ai_chats,

    // --- 랭크/보너스 ---
    "img_badge_first_rank" to MyPageR.drawable.img_badge_first_rank,
    "img_badge_rank_1week" to MyPageR.drawable.img_badge_rank_1week,
    "img_badge_rank_1month" to MyPageR.drawable.img_badge_rank_1month,
    "img_badge_bonus_month" to MyPageR.drawable.img_badge_bonus_month,

    // --- 기타 ---
    "img_badge_rank_100days" to MyPageR.drawable.img_badge_rank_100days,
    "img_badge_bonus" to MyPageR.drawable.img_badge_bonus,
    "img_badge_early_morning" to MyPageR.drawable.img_badge_early_morning,
    "img_badge_five_logins_day" to MyPageR.drawable.img_badge_five_logins_day,

    // === 잠금 이미지 ===
    "img_badge_1week_attendance_lock" to MyPageR.drawable.img_badge_1week_attendance_lock,
    "img_badge_1month_attendance_lock" to MyPageR.drawable.img_badge_1month_attendance_lock,
    "img_badge_100days_attendance_lock" to MyPageR.drawable.img_badge_100days_attendance_lock,
    "img_badge_first_lesson_lock" to MyPageR.drawable.img_badge_first_lesson_lock,
    "img_badge_five_lessons_lock" to MyPageR.drawable.img_badge_five_lessons_lock,
    "img_badge_first_quizmunch_lock" to MyPageR.drawable.img_badge_first_quizmunch_lock,
    "img_badge_five_quizzes_lock" to MyPageR.drawable.img_badge_five_quizzes_lock,
    "img_badge_first_ai_chat_lock" to MyPageR.drawable.img_badge_first_ai_chat_lock,
    "img_badge_five_ai_chats_lock" to MyPageR.drawable.img_badge_five_ai_chats_lock,
    "img_badge_first_rank_lock" to MyPageR.drawable.img_badge_first_rank_lock,
    "img_badge_rank_1week_lock" to MyPageR.drawable.img_badge_rank_1week_lock,
    "img_badge_rank_1month_lock" to MyPageR.drawable.img_badge_rank_1month_lock,
    "img_badge_bonus_month_lock" to MyPageR.drawable.img_badge_bonus_month_lock,
    "img_badge_rank_100days_lock" to MyPageR.drawable.img_badge_rank_100days_lock,
    "img_badge_bonus_lock" to MyPageR.drawable.img_badge_bonus_lock,
    "img_badge_early_morning_lock" to MyPageR.drawable.img_badge_early_morning_lock,
    "img_badge_five_logins_day_lock" to MyPageR.drawable.img_badge_five_logins_day_lock
)

// ===== Entry Point =====
@Composable
fun BadgeCollectionRoute(
    onBack: () -> Unit,
    viewModel: BadgeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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

        else -> Unit
    }
}

// ===== Screen =====
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BadgeCollectionScreen(
    onBack: () -> Unit,
    badges: List<BadgeUi>,
    viewModel: MyPageViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    var selectedBadge by remember { mutableStateOf<BadgeUi?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    //대표 배지 상태
    var representativeBadge by remember { mutableStateOf<BadgeUi?>(null) }

    // ✅ 추가: 앱 시작 시 로컬에서 저장된 대표 배지 불러오기
    LaunchedEffect(Unit) {
        viewModel.loadRepresentativeBadge { savedKey ->
            if (savedKey != null) {
                val badge = badges.find { it.key == savedKey }
                if (badge != null) representativeBadge = badge
            }
        }
    }
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(20.dp))

        // === 대표 배지 카드 ===
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (representativeBadge != null) { // ✅ 대표 배지만 표시
                    val resId = badgeResMap[representativeBadge!!.key] ?: MyPageR.drawable.ic_lock_on
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = representativeBadge!!.title, // ✅ 수정됨
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop   // ✅ 박스를 꽉 채움
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = representativeBadge!!.title,
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

        Spacer(Modifier.height(V_SECTION))

        // === 전체 배지 리스트 ===
        BadgeGrid(
            badges = badges,
            onSelect = { badge ->
                if (badge.unlocked) {
                    selectedBadge = badge      // ✅ 선택만 함
                    showSheet = true            // ✅ 시트만 열림
                }
            }
        )

        Spacer(Modifier.height(60.dp))
    }

    // ✅ 시트에서 대표 배지 설정할 때만 실제 저장
    if (showSheet && selectedBadge != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            BadgeBottomSheetContent(
                badge = selectedBadge!!,
                onDismiss = { showSheet = false },
                onSetRepresentative = {
                    // 여기서만 저장
                    viewModel.setRepresentativeBadge(selectedBadge!!.key)
                    representativeBadge = selectedBadge!!
                    showSheet = false
                }
            )
        }
    }
}

@Composable
private fun BadgeBottomSheetContent(
    badge: BadgeUi,
    onDismiss: () -> Unit,
    onSetRepresentative: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(Modifier.height(0.dp))

            // ✅ 배지 이미지
            val resId = badgeResMap[badge.key] ?: MyPageR.drawable.ic_lock_on
            Image(
                painter = painterResource(id = resId),
                contentDescription = badge.title,
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ✅ 배지 이름
            Text(
                text = badge.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            // ✅ 설명 문구
            Text(
                text = "이 배지는 당신의 노력의 증표입니다!\n대표 배지로 설정해보세요.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                color = Color(0xFF666666),
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )

            // ✅ 버튼 위 흰색 여백 (WordCollect와 동일)
            Spacer(Modifier.height(48.dp))

            // ✅ 버튼 영역 (WordCollect 동일)
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top // ✅ 이미지 높이 맞춤
            ) {
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFF195FCF)), // 테두리 색상 추가
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(
                        "확인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        color = Color(0xFF195FCF)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = { onSetRepresentative() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                ) {
                    Text(
                        "대표 배지 설정",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeGrid(
    badges: List<BadgeUi>,
    onSelect: (BadgeUi) -> Unit
) {
    val rows = badges.chunked(3)
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp), // ✅ 좌우 20 유지
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { badge ->
                    BadgeCell(badge = badge, onSelect = onSelect)
                }
                repeat(3 - row.size) { // ✅ 마지막 줄 비워진 칸 공간 유지
                    Spacer(modifier = Modifier.size(96.dp))
                }
            }
        }
    }
}

@Composable
private fun BadgeCell(
    badge: BadgeUi,
    onSelect: (BadgeUi) -> Unit
) {
    val badgeSize = 96.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(badgeSize)
            .clickable(enabled = badge.unlocked) { onSelect(badge) }
    ) {
        Box(
            modifier = Modifier
                .size(badgeSize)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            val imageKey = if (badge.unlocked) badge.key else "${badge.key}_lock"
            val resId = badgeResMap[imageKey] ?: MyPageR.drawable.ic_lock_on

            // ✅ 잠금 상태일 때만 살짝 축소해서 균형 맞춤
            val imageModifier = if (badge.unlocked) {
                Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            } else {
                Modifier
                    .fillMaxSize(0.856f) // 🔹 lock 이미지 크기 보정 (0.85~0.9 사이 조정 가능)
                    .aspectRatio(1f)
            }

            Image(
                painter = painterResource(id = resId),
                contentDescription = badge.title,
                modifier = imageModifier,
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = badge.title,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Gray_262626,
                lineHeight = 13.sp * 1.4f
            ),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(96.dp)
                .height(36.dp) // ✅ 항상 동일 높이 유지
        )
    }
}

// ===== Preview =====
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = true)
@Composable
private fun BadgeCollectionPreview() {
    val sampleBadges = listOf(
        BadgeUi("img_badge_1week_attendance", "일주일 출석", true),
        BadgeUi("img_badge_1month_attendance", "한 달 출석", false),
        BadgeUi("img_badge_first_ai_chat", "AI 대화 첫 학습 완료", true)
    )
    BadgeCollectionScreen(onBack = {}, badges = sampleBadges)
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    showSystemUi = true,
    name = "배지 바텀 시트 디자인 미리보기"
)
@Composable
private fun BadgeBottomSheetContentPreview() {
    val sampleBadge = BadgeUi(
        key = "img_badge_first_ai_chat",
        title = "AI 대화 첫 학습 완료",
        unlocked = true
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            // 이 부분이 실제 ModalBottomSheet 안에 들어갈 내용과 동일
            BadgeBottomSheetContent(
                badge = sampleBadge,
                onDismiss = {},
                onSetRepresentative = {}
            )
        }
    }
}

