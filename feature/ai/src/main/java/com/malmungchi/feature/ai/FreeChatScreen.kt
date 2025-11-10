package com.malmungchi.feature.ai

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.ai.R
import com.malmungchi.feature.ai.model.BubbleStyle as MBubbleStyle

// ▼▼▼ 공통 모델 alias ▼▼▼
import com.malmungchi.feature.ai.model.ChatUiState as MChatUiState
import com.malmungchi.feature.ai.model.ChatMessage as MChatMessage
import com.malmungchi.feature.ai.model.Role as MRole
// ▲▲▲

// ────────────────────────────────────────────────────
// 말풍선 컴포넌트 (원형 유지)
// ────────────────────────────────────────────────────
@Composable
private fun ChatBubbleRectTopLeftSharp(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color(0xFFF7F7F7),
    corner: Dp = 16.dp,
    showTipInside: Boolean = false,
    userShape: Boolean = false,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp
) {
    val lines = remember(text) { text.lines() }
    val tipLines = remember(text) { lines.filter { it.trim().startsWith("TIP:", ignoreCase = true) } }
    val body = remember(text) {
        lines.filter { !it.trim().startsWith("TIP:", ignoreCase = true) }
            .joinToString("\n")
            .ifBlank { text }
    }

    val shape =
        if (userShape) RoundedCornerShape(topStart = corner, topEnd = 0.dp, bottomEnd = corner, bottomStart = corner)
        else RoundedCornerShape(topStart = 0.dp, topEnd = corner, bottomEnd = corner, bottomStart = corner)

    Box(
        modifier = modifier
            .background(bgColor, shape)
            .then(if (borderColor != null) Modifier.border(borderWidth, borderColor, shape) else Modifier)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                body,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color(0xFF222222)
            )
            if (showTipInside && tipLines.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                tipLines.forEach { tip ->
                    Text(
                        text = tip.trim(),
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF616161)
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────
// 공용 Row 렌더러
// ────────────────────────────────────────────────────
@Composable
private fun MessageRow(msg: MChatMessage) {
    val bubbleWidthMax = 280.dp
    if (msg.role == MRole.Bot) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(id = R.drawable.img_chatbot_malchi),
                contentDescription = "Bot",
                modifier = Modifier.size(48.dp).offset(y = (-2).dp)
            )
            Spacer(Modifier.width(6.dp))
            ChatBubbleRectTopLeftSharp(
                text = msg.text,
                modifier = Modifier.widthIn(max = bubbleWidthMax).wrapContentHeight(),
                bgColor = Color(0xFFF7F7F7),
                showTipInside = true,
                borderColor = if (msg.style == MBubbleStyle.BotFeedback) Color(0xFF195FCF) else null
            )
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            ChatBubbleRectTopLeftSharp(
                text = msg.text,
                modifier = Modifier.widthIn(max = bubbleWidthMax).wrapContentHeight(),
                bgColor = Color.White,
                userShape = true,
                borderColor = if (msg.style == MBubbleStyle.UserRetryNeeded) Color(0xFFFF0D0D) else null
            )
        }
    }
}

// ────────────────────────────────────────────────────
// 자유 대화 화면 (ChatScreen과 동일 UX 적용)
// ────────────────────────────────────────────────────
@Composable
fun FreeChatScreen(
    vm: ChatViewModel = viewModel(),
    onBack: () -> Unit = {},     // ← AiScreen으로 이동
    onExit: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val state = vm.ui.value

    // 모드 세팅 + hello (1회만)
    LaunchedEffect(Unit) {
        vm.setModeDaily()
        vm.loadHello()
    }

    // 시스템 Back → 무조건 상위로
    BackHandler { onBack() }

    val listState = rememberLazyListState()

    // 메시지 분해: 과거/최근(1쌍=2개)
    val msgs = state.messages
    val recentCount = if (msgs.size >= 2) 2 else msgs.size
    val olderCount = (msgs.size - recentCount).coerceAtLeast(0)
    val older = remember(msgs) { msgs.take(olderCount) }
    val recent = remember(msgs) { msgs.takeLast(recentCount) }

    // 새 메시지 오면 최신 시작 위치(olderCount)로 고정 스크롤
    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) listState.scrollToItem(index = olderCount, scrollOffset = 0)
    }

    val normalItemSpacing = 8.dp
    val crossRoleExtraSpacing = 16.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3E9F3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
        ) {
            // 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { onBack() },   // ← 무조건 AiScreen 이동
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back"
                    )
                }
                Text(
                    text = "자유 대화",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF222222),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(12.dp))

            // 날짜 배지
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color.White, shape = RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "2025.04.05",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(12.dp))

            // 리스트: older → recent → XP (ChatScreen과 동일)
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                // A) 과거 (스크롤 올려야 보임)
                itemsIndexed(older) { index, msg ->
                    val nextRole = older.getOrNull(index + 1)?.role
                    val topPad =
                        if (nextRole != null && nextRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
                    if (index > 0) Spacer(Modifier.height(topPad))
                    MessageRow(msg)
                }

                // B) 최신 1쌍 (날짜 바로 밑에 보임)
                itemsIndexed(recent) { index, msg ->
                    val prevRole = if (index == 0) older.lastOrNull()?.role else recent.getOrNull(index - 1)?.role
                    val topPad =
                        if (prevRole != null && prevRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
                    if (older.isNotEmpty() || index > 0) Spacer(Modifier.height(topPad))
                    MessageRow(msg)
                }

                // C) XP 블럭 (마지막 말풍선 바로 뒤)
                if (state.botReplyCount >= 3) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(Modifier.weight(1f), color = Color(0xFFCDD2DB), thickness = 1.dp)
                            Text(
                                text = "XP를 지급합니다",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF616161)
                            )
                            Divider(Modifier.weight(1f), color = Color(0xFFCDD2DB), thickness = 1.dp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFC9CAD4), RoundedCornerShape(20.dp))
                                    .clickable { onExit() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "대화 종료하기",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF195FCF), RoundedCornerShape(20.dp))
                                    .clickable { onContinue() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "대화 이어가기",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 하단 마이크 (VM 상태에 맞춰 표시)
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)) {
            MicButton(vm)
        }
    }
}

/* ===========================
 * 프리뷰
 * =========================== */
@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
fun FreeChatScreenPreview_After3Replies() {
    val previewState = MChatUiState(
        messages = listOf(
            MChatMessage(MRole.Bot, "편하게 아무 이야기나 시작해 보세요.\nTIP: 오늘 있었던 일 한 가지."),
            MChatMessage(MRole.User, "오늘 헬스장에서 상체 운동했어!"),
            MChatMessage(MRole.Bot, "오 멋져요! 어떤 운동을 몇 세트 하셨나요?\nTIP: 숫자를 붙이면 좋아요."),
            MChatMessage(MRole.User, "벤치프레스 30kg 8회 4세트, 랫풀다운 35kg 10회 4세트!"),
            MChatMessage(MRole.Bot, "와, 체계적으로 하셨네요. 다음 목표를 하나 정해볼까요?\nTIP: 기간 + 수치.")
        ),
        isRecording = false,
        isLoading = false,
        botReplyCount = 3
    )
    FreeChatScreenPreviewHost(state = previewState)
}

@Composable
private fun FreeChatScreenPreviewHost(
    state: MChatUiState,
    onExit: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    val msgs = state.messages
    val recentCount = if (msgs.size >= 2) 2 else msgs.size
    val olderCount = (msgs.size - recentCount).coerceAtLeast(0)
    val older = remember(msgs) { msgs.take(olderCount) }
    val recent = remember(msgs) { msgs.takeLast(recentCount) }

    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) listState.scrollToItem(index = olderCount, scrollOffset = 0)
    }

    val normalItemSpacing = 8.dp
    val crossRoleExtraSpacing = 16.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3E9F3))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, start = 20.dp, end = 12.dp, bottom = 4.dp)
        ) {
            // 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { /* no-op */ },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.Unspecified
                    )
                }
                Text(
                    text = "자유 대화",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    color = Color(0xFF222222),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(2.dp))

            // 날짜 배지
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color.White, shape = RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "2025.04.05",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(bottom = 72.dp)
            ) {
                // older
                itemsIndexed(older) { index, msg ->
                    val nextRole = older.getOrNull(index + 1)?.role
                    val topPad =
                        if (nextRole != null && nextRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
                    if (index > 0) Spacer(Modifier.height(topPad))
                    MessageRow(msg)
                }

                // recent
                itemsIndexed(recent) { index, msg ->
                    val prevRole = if (index == 0) older.lastOrNull()?.role else recent.getOrNull(index - 1)?.role
                    val topPad =
                        if (prevRole != null && prevRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
                    if (older.isNotEmpty() || index > 0) Spacer(Modifier.height(topPad))
                    MessageRow(msg)
                }

                // XP
                if (state.botReplyCount >= 3) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(Modifier.weight(1f), color = Color(0xFFCDD2DB), thickness = 1.dp)
                            Text(
                                text = "XP를 지급합니다",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF616161)
                            )
                            Divider(Modifier.weight(1f), color = Color(0xFFCDD2DB), thickness = 1.dp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFC9CAD4), RoundedCornerShape(20.dp))
                                    .clickable { onExit() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "대화 종료하기",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF195FCF), RoundedCornerShape(20.dp))
                                    .clickable { onContinue() }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "대화 이어가기",
                                    fontFamily = Pretendard,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 프리뷰용 마이크
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)) {
            Image(
                painter = painterResource(id = R.drawable.ic_chat_mike),
                contentDescription = "Mic",
                modifier = Modifier.size(56.dp)
            )
        }
    }
}


//package com.malmungchi.feature.ai
//
//import androidx.activity.compose.BackHandler
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Divider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.malmungchi.core.designsystem.Pretendard
//import com.malmungchi.feature.ai.R
//import com.malmungchi.feature.ai.model.BubbleStyle as MBubbleStyle
//
//// ▼▼▼ 공통 모델 alias (원본과 동일) ▼▼▼
//import com.malmungchi.feature.ai.model.ChatUiState as MChatUiState
//import com.malmungchi.feature.ai.model.ChatMessage as MChatMessage
//import com.malmungchi.feature.ai.model.Role as MRole
//// ▲▲▲
//
//// ----------------------------------------------------
//// (원본과 동일) 말풍선
////  - Bot: 왼쪽 위만 직각
////  - User: 오른쪽 위만 직각
////  - TIP은 Bot 말풍선 내부 616161
//// ----------------------------------------------------
//@Composable
//private fun ChatBubbleRectTopLeftSharp(
//    text: String,
//    modifier: Modifier = Modifier,
//    bgColor: Color = Color(0xFFF7F7F7),
//    corner: Dp = 16.dp,
//    showTipInside: Boolean = false,
//    userShape: Boolean = false,
//    borderColor: Color? = null,
//    borderWidth: Dp = 1.dp
//) {
//    val lines = remember(text) { text.lines() }
//    val tipLines = remember(text) { lines.filter { it.trim().startsWith("TIP:", ignoreCase = true) } }
//    val body = remember(text) {
//        lines.filter { !it.trim().startsWith("TIP:", ignoreCase = true) }
//            .joinToString("\n")
//            .ifBlank { text }
//    }
//
//    val shape =
//        if (userShape) RoundedCornerShape(topStart = corner, topEnd = 0.dp, bottomEnd = corner, bottomStart = corner)
//        else RoundedCornerShape(topStart = 0.dp, topEnd = corner, bottomEnd = corner, bottomStart = corner)
//
//    Box(
//        modifier = modifier
//            .background(bgColor, shape)
//            .then(if (borderColor != null) Modifier.border(borderWidth, borderColor, shape) else Modifier)
//            .padding(horizontal = 12.dp, vertical = 12.dp)
//    ) {
//        Column {
//            Text(
//                body,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                fontSize = 16.sp,
//                color = Color(0xFF222222)
//            )
//            if (showTipInside && tipLines.isNotEmpty()) {
//                Spacer(Modifier.height(6.dp))
//                tipLines.forEach { tip ->
//                    Text(
//                        text = tip.trim(),
//                        fontFamily = Pretendard,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 13.sp,
//                        color = Color(0xFF616161)
//                    )
//                }
//            }
//        }
//    }
//}
//
//// ----------------------
//// 새로운 자유 대화 화면 (제목만 변경)
//// ----------------------
//@Composable
//fun FreeChatScreen(
//    vm: ChatViewModel = viewModel(),
//    onBack: () -> Unit = {},
//    onExit: () -> Unit = {},        // "대화 종료하기" → 상위에서 네비게이트
//    onContinue: () -> Unit = {}     // "대화 이어가기" → 계속 음성 대화
//) {
//    val state = vm.ui.value
//
//
//    // ✅ 진입 시 DAILY 모드로 전환 후 hello
//    LaunchedEffect(Unit) {
//        vm.setModeDaily()
//        vm.loadHello()
//    }
//
//    // 종료 확인 다이얼로그
//    var showExitConfirm by remember { mutableStateOf(false) }
//    fun handleExitAttempt() {
//        if (state.botReplyCount < 3) showExitConfirm = true else onExit()
//    }
//
//    BackHandler { handleExitAttempt() }
//
//    LaunchedEffect(Unit) { vm.loadHello() }
//
//    val listState = rememberLazyListState()
//    LaunchedEffect(state.messages.size) {
//        if (state.messages.isNotEmpty()) listState.animateScrollToItem(0)
//    }
//
//    val normalItemSpacing = 8.dp
//    val crossRoleExtraSpacing = 16.dp
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFE3E9F3))
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 16.dp)
//        ) {
//            // 헤더 (제목만 "자유 대화")
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                IconButton(
//                    onClick = { handleExitAttempt() },
//                    modifier = Modifier
//                        .align(Alignment.CenterStart)
//                        .size(48.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_back),
//                        contentDescription = "Back"
//                    )
//                }
//
//                Text(
//                    text = "자유 대화",
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 20.sp,
//                    color = Color(0xFF222222),
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//
//            Spacer(Modifier.height(12.dp))
//
//            // 날짜 배지 (임시 고정)
//            Box(
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .background(Color.White, shape = RoundedCornerShape(999.dp))
//                    .padding(horizontal = 12.dp, vertical = 6.dp)
//            ) {
//                Text(
//                    "2025.04.05",
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 14.sp,
//                    color = Color.Black
//                )
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            LazyColumn(
//                modifier = Modifier.weight(1f),
//                state = listState,
//                reverseLayout = false,
//                contentPadding = PaddingValues(bottom = 0.dp)
//            ) {
//                itemsIndexed(state.messages) { index, msg ->
//                    val nextRole = state.messages.getOrNull(index + 1)?.role
//                    val topPad =
//                        if (nextRole != null && nextRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
//                    Spacer(Modifier.height(topPad))
//
//                    if (msg.role == MRole.Bot) {
//                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
//                            Image(
//                                painter = painterResource(id = R.drawable.img_chatbot_malchi),
//                                contentDescription = "Bot",
//                                modifier = Modifier.size(48.dp).offset(y = (-2).dp)
//                            )
//                            Spacer(Modifier.width(6.dp))
//                            ChatBubbleRectTopLeftSharp(
//                                text = msg.text,
//                                modifier = Modifier.widthIn(max = 280.dp).wrapContentHeight(),
//                                bgColor = Color(0xFFF7F7F7),
//                                showTipInside = true,
//                                borderColor = if (msg.style == MBubbleStyle.BotFeedback) Color(0xFF195FCF) else null
//                            )
//                        }
//                    } else {
//                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
//                            ChatBubbleRectTopLeftSharp(
//                                text = msg.text,
//                                modifier = Modifier.widthIn(max = 280.dp).wrapContentHeight(),
//                                bgColor = Color.White,
//                                userShape = true,
//                                borderColor = if (msg.style == MBubbleStyle.UserRetryNeeded) Color(0xFFFF0D0D) else null
//                            )
//                        }
//                    }
//                }
//            }
//
//            // XP/버튼 구역 (원본과 동일)
//            Spacer(Modifier.height(4.dp))
//
//            if (state.botReplyCount >= 3) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Divider(Modifier.weight(1f), color = Color(0xFFCDD2DB), thickness = 1.dp)
//                    Text(
//                        text = "XP를 지급합니다",
//                        modifier = Modifier.padding(horizontal = 8.dp),
//                        fontFamily = Pretendard,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 12.sp,
//                        color = Color(0xFF616161)
//                    )
//                    Divider(Modifier.weight(1f), color = Color(0xFFCDD2DB), thickness = 1.dp)
//                }
//
//                Spacer(Modifier.height(8.dp))
//
//                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
//                    Box(
//                        modifier = Modifier
//                            .background(Color(0xFFC9CAD4), RoundedCornerShape(20.dp))
//                            .clickable { onExit() }
//                            .padding(horizontal = 12.dp, vertical = 8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "대화 종료하기",
//                            fontFamily = Pretendard,
//                            fontWeight = FontWeight.Medium,
//                            fontSize = 12.sp,
//                            color = Color.White
//                        )
//                    }
//
//                    Spacer(Modifier.width(12.dp))
//
//                    Box(
//                        modifier = Modifier
//                            .background(Color(0xFF195FCF), RoundedCornerShape(20.dp))
//                            .clickable { onContinue() }
//                            .padding(horizontal = 12.dp, vertical = 8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            "대화 이어가기",
//                            fontFamily = Pretendard,
//                            fontWeight = FontWeight.Medium,
//                            fontSize = 12.sp,
//                            color = Color.White
//                        )
//                    }
//                }
//            }
//        }
//
//        // 하단 마이크 (녹음/업로드 상태는 VM에서 동일하게)
//        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)) {
//            MicButton(vm)
//        }
//
//        if (showExitConfirm) {
//            EndChatConfirmDialog(
//                onYes = {
//                    showExitConfirm = false
//                    onExit()
//                },
//                onNo = { showExitConfirm = false }
//            )
//        }
//    }
//}
//
///* ===========================
// * 프리뷰 (3회 응답 뒤 상태)
// * =========================== */
//@Preview(showBackground = true, widthDp = 360, heightDp = 760)
//@Composable
//fun FreeChatScreenPreview_After3Replies() {
//    val previewState = MChatUiState(
//        messages = listOf(
//            MChatMessage(MRole.Bot, "편하게 아무 이야기나 시작해 보세요.\nTIP: 오늘 있었던 일 한 가지."),
//            MChatMessage(MRole.User, "오늘 헬스장에서 상체 운동했어!"),
//            MChatMessage(MRole.Bot, "오 멋져요! 어떤 운동을 몇 세트 하셨나요?\nTIP: 숫자를 붙이면 좋아요."),
//            MChatMessage(MRole.User, "벤치프레스 30kg 8회 4세트, 랫풀다운 35kg 10회 4세트!"),
//            MChatMessage(MRole.Bot, "와, 체계적으로 하셨네요. 다음 목표를 하나 정해볼까요?\nTIP: 기간 + 수치.")
//        ),
//        isRecording = false,
//        isLoading = false,
//        botReplyCount = 3
//    )
//    FreeChatScreenPreviewHost(state = previewState)
//}
//
//@Composable
//private fun FreeChatScreenPreviewHost(
//    state: MChatUiState,
//    onExit: () -> Unit = {},
//    onContinue: () -> Unit = {}
//) {
//    val listState = rememberLazyListState()
//    LaunchedEffect(state.messages.size) {
//        if (state.messages.isNotEmpty()) listState.animateScrollToItem(0)
//    }
//
//    val normalItemSpacing = 8.dp
//    val crossRoleExtraSpacing = 16.dp
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFE3E9F3))
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(top = 48.dp, start = 20.dp, end = 12.dp, bottom = 4.dp)
//        ) {
//            // 헤더 (제목: 자유 대화)
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                IconButton(
//                    onClick = { /* no-op */ },
//                    modifier = Modifier
//                        .align(Alignment.CenterStart)
//                        .size(48.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_back),
//                        contentDescription = "Back",
//                        tint = Color.Unspecified
//                    )
//                }
//                Text(
//                    text = "자유 대화",
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 20.sp,
//                    color = Color(0xFF222222),
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }
//
//            Spacer(Modifier.height(2.dp))
//
//            // 날짜 배지
//            Box(
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .background(Color.White, shape = RoundedCornerShape(999.dp))
//                    .padding(horizontal = 12.dp, vertical = 6.dp)
//            ) {
//                Text(
//                    "2025.04.05",
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 14.sp,
//                    color = Color.Black
//                )
//            }
//
//            Spacer(Modifier.height(12.dp))
//
//            LazyColumn(
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth(),
//                state = listState,
//                reverseLayout = false,
//                contentPadding = PaddingValues(top = 0.dp, bottom = 72.dp)
//            ) {
//                itemsIndexed(state.messages) { index, msg ->
//                    val nextRole = state.messages.getOrNull(index + 1)?.role
//                    val topPad =
//                        if (nextRole != null && nextRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
//                    Spacer(Modifier.height(topPad))
//
//                    if (msg.role == MRole.Bot) {
//                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
//                            Image(
//                                painter = painterResource(id = R.drawable.img_chatbot_malchi),
//                                contentDescription = "Bot",
//                                modifier = Modifier
//                                    .size(48.dp)
//                                    .offset(y = (-2).dp)
//                            )
//                            Spacer(Modifier.width(6.dp))
//                            ChatBubbleRectTopLeftSharp(
//                                text = msg.text,
//                                modifier = Modifier
//                                    .widthIn(max = 280.dp)
//                                    .wrapContentHeight(),
//                                bgColor = Color(0xFFF7F7F7),
//                                showTipInside = true
//                            )
//                        }
//                    } else {
//                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
//                            ChatBubbleRectTopLeftSharp(
//                                text = msg.text,
//                                modifier = Modifier.widthIn(max = 280.dp),
//                                bgColor = Color.White,
//                                userShape = true
//                            )
//                        }
//                    }
//                }
//
//                if (state.botReplyCount >= 3) {
//                    item {
//                        Spacer(Modifier.height(12.dp))
//
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Divider(
//                                Modifier.weight(1f),
//                                color = Color(0xFFCDD2DB),
//                                thickness = 1.dp
//                            )
//                            Text(
//                                text = "XP를 지급합니다",
//                                modifier = Modifier.padding(horizontal = 8.dp),
//                                fontFamily = Pretendard,
//                                fontWeight = FontWeight.Medium,
//                                fontSize = 12.sp,
//                                color = Color(0xFF616161)
//                            )
//                            Divider(
//                                Modifier.weight(1f),
//                                color = Color(0xFFCDD2DB),
//                                thickness = 1.dp
//                            )
//                        }
//
//                        Spacer(Modifier.height(8.dp))
//
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(bottom = 8.dp),
//                            horizontalArrangement = Arrangement.Center
//                        ) {
//                            Box(
//                                modifier = Modifier
//                                    .background(Color(0xFFC9CAD4), RoundedCornerShape(20.dp))
//                                    .clickable { onExit() }
//                                    .padding(horizontal = 12.dp, vertical = 8.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    "대화 종료하기",
//                                    fontFamily = Pretendard,
//                                    fontWeight = FontWeight.Medium,
//                                    fontSize = 12.sp,
//                                    color = Color.White
//                                )
//                            }
//
//                            Spacer(Modifier.width(12.dp))
//
//                            Box(
//                                modifier = Modifier
//                                    .background(Color(0xFF195FCF), RoundedCornerShape(20.dp))
//                                    .clickable { onContinue() }
//                                    .padding(horizontal = 12.dp, vertical = 8.dp),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Text(
//                                    "대화 이어가기",
//                                    fontFamily = Pretendard,
//                                    fontWeight = FontWeight.Medium,
//                                    fontSize = 12.sp,
//                                    color = Color.White
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        // 프리뷰용 마이크
//        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_chat_mike),
//                contentDescription = "Mic",
//                modifier = Modifier.size(56.dp)
//            )
//        }
//    }
//}
