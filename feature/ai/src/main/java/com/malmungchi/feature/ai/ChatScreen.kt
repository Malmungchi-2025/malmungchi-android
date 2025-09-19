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
//// ▼▼▼ 공통 모델을 alias 임포트로 고정 사용 (로컬 선언 금지!) ▼▼▼
//import com.malmungchi.feature.ai.model.ChatUiState as MChatUiState
//import com.malmungchi.feature.ai.model.ChatMessage as MChatMessage
//import com.malmungchi.feature.ai.model.Role as MRole
//// ▲▲▲
//
//// ----------------------------------------------------
//// 말풍선 (그대로 유지)
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
//    // ★ TIP 이 아닌 라인들을 모두 보이도록 결합
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
//// 실제 ChatScreen (ViewModel 연동)
//// ----------------------
//@Composable
//fun ChatScreen(
//    vm: ChatViewModel = viewModel(),
//    onBack: () -> Unit = {},
//    onExit: () -> Unit = {},          // "대화 종료하기" → 상위에서 네비게이트
//    onContinue: () -> Unit = {}       // "대화 이어가기" → 계속 음성 대화
//) {
//    val state = vm.ui.value
//
//    // ★ 종료 확인 다이얼로그 상태
//    var showExitConfirm by remember { mutableStateOf(false) }
//
//    // ★ 공통 종료 시도 핸들러
//    fun handleExitAttempt() {
//        if (state.botReplyCount < 3) showExitConfirm = true else onExit()
//    }
//
//    // ★ 시스템 Back 버튼 가로채기
//    BackHandler { handleExitAttempt() }
//
//
//    LaunchedEffect(Unit) {
//        vm.setModeJob()   // ✅ 명시
//        vm.loadHello()
//    }
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
//            // 헤더
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                IconButton(
//                    onClick = { handleExitAttempt() },
//                    //onClick = onBack,
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
//                    text = "취준생 맞춤 상황",
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
//                                borderColor = if (msg.style == MBubbleStyle.BotFeedback) Color(0xFF195FCF) else null // ★ 파란 테두리
//                            )
//                        }
//                    } else {
//                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
//                            ChatBubbleRectTopLeftSharp(
//                                text = msg.text,
//                                modifier = Modifier.widthIn(max = 280.dp).wrapContentHeight(),
//                                bgColor = Color.White,
//                                userShape = true,
//                                borderColor = if (msg.style == MBubbleStyle.UserRetryNeeded) Color(0xFFFF0D0D) else null // ★ 빨간 테두리
//                            )
//                        }
//                    }
//                }
//            }
//
//
//            // XP와 마지막 말풍선 간격 4dp
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
//        // 하단 마이크: 녹음/업로드 중이면 ING 아이콘
//        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)) {
//            MicButton(vm)   // MicButton은 같은 패키지에 존재해야 함
//        }
//
//        if (showExitConfirm) {
//            EndChatConfirmDialog(
//                onYes = {
//                    showExitConfirm = false
//                    onExit()        // 네 → AiScreen 이동
//                },
//                onNo = {
//                    showExitConfirm = false // 아니요 → 현재 화면 유지
//                }
//            )
//        }
//    }
//}
//
///* ===========================
// * 프리뷰
// * =========================== */
//@Preview(showBackground = true, widthDp = 360, heightDp = 760)
//@Composable
//fun ChatScreenPreview_After3Replies() {
//    val previewState = MChatUiState(
//        messages = listOf(
//            MChatMessage(MRole.Bot, "[면접 상황]\n: 본인의 장단점이 무엇인가요?"),
//            MChatMessage(MRole.User, "저의 장점은 실행력이 빠르고, 단점은 가끔 과속합니다."),
//            MChatMessage(MRole.Bot, "좋아요. 과속을 조절했던 경험을 STAR로 말해볼까요?\nTIP: 결과(RESULT)를 명확히."),
//            MChatMessage(MRole.User, "OK, S/T/A/R로 정리해서 다시 말해볼게요."),
//            MChatMessage(MRole.Bot, "좋습니다. 핵심 수치와 배운 점을 1문장으로 덧붙여주세요.\nTIP: 숫자 1개 이상.")
//        ),
//        isRecording = false,
//        isLoading = false,
//        botReplyCount = 3
//    )
//    ChatScreenPreviewHost(state = previewState)
//}
//
//@Composable
//private fun ChatScreenPreviewHost(
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
//            // 헤더
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
//                    text = "취준생 맞춤 상황",
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
//
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
//
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

// 공통 모델 alias
import com.malmungchi.feature.ai.model.ChatUiState as MChatUiState
import com.malmungchi.feature.ai.model.ChatMessage as MChatMessage
import com.malmungchi.feature.ai.model.Role as MRole

// ────────────────────────────────────────────────────
// 말풍선
// Bot: 왼쪽 위만 직각 / User: 오른쪽 위만 직각
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
// Row 렌더러 (공용)
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
// 실제 ChatScreen
// ────────────────────────────────────────────────────
@Composable
fun ChatScreen(
    vm: ChatViewModel = viewModel(),
    onBack: () -> Unit = {},
    onExit: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val state = vm.ui.value

    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        vm.setModeJob()
        vm.loadHello()
    }

    val listState = rememberLazyListState()

    // 정방향(기본) 렌더링: older → recent → XP
    val msgs = state.messages
    val recentCount = if (msgs.size >= 2) 2 else msgs.size
    val olderCount = (msgs.size - recentCount).coerceAtLeast(0)
    val older = remember(msgs) { msgs.take(olderCount) }
    val recent = remember(msgs) { msgs.takeLast(recentCount) } // 최신 1쌍

    // ✅ 새 메시지 오면 "최근의 첫 아이템"이 리스트 맨 위(날짜 바로 밑)에 오도록 고정
    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) {
            // olderCount 위치로 스크롤 → 최근의 첫 아이템이 뷰포트 상단에 붙음
            listState.scrollToItem(index = olderCount, scrollOffset = 0)
        }
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
                    onClick = onBack,
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
                    text = "취준생 맞춤 상황",
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

            // 메시지 리스트 (정방향)
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(bottom = 72.dp) // 마이크/하단 여유
            ) {
                // (A) 과거 메시지들 — 기본 화면에선 스크롤 올려야 보임
                itemsIndexed(older) { index, msg ->
                    val nextRole = older.getOrNull(index + 1)?.role
                    val topPad =
                        if (nextRole != null && nextRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
                    if (index > 0) Spacer(Modifier.height(topPad))
                    MessageRow(msg)
                }

                // (B) 최신 1쌍 — 날짜 바로 밑(리스트 상단)에 고정해 보이게 스크롤 위치 조정
                itemsIndexed(recent) { index, msg ->
                    val prevRole = if (index == 0) older.lastOrNull()?.role else recent.getOrNull(index - 1)?.role
                    val topPad =
                        if (prevRole != null && prevRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
                    if (older.isNotEmpty() || index > 0) Spacer(Modifier.height(topPad))
                    MessageRow(msg)
                }

                // (C) XP 블럭 — 최신 바로 뒤에 붙음 (떨어지지 않음)
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

        // 하단 마이크
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
fun ChatScreenPreview_After3Replies() {
    val previewState = MChatUiState(
        messages = listOf(
            MChatMessage(MRole.Bot, "[면접 상황]\n: 본인의 장단점이 무엇인가요?"),
            MChatMessage(MRole.User, "저의 장점은 실행력이 빠르고, 단점은 가끔 과속합니다."),
            MChatMessage(MRole.Bot, "다시 한 번 해볼까요?\n피드백: 답변이 모호하고 구체성이 부족했습니다.\nTIP: 예를 들어, '안녕하세요, 저는 [이름]입니다. 면접 일정에 대해 문의드리고 싶습니다.'라고 시작해보세요."),
            MChatMessage(MRole.User, "알았어, 다시 해볼게."),
            MChatMessage(MRole.Bot, "면접 일정 변경 요청은 예의 바르고 구체적으로! \nTIP: '안녕하세요. [이름]입니다. 일정 변경이 가능할까요? 사정이 있어 날짜를 조정하고 싶습니다.'")
        ),
        isRecording = false,
        isLoading = false,
        botReplyCount = 3
    )
    ChatScreenPreviewHost(state = previewState)
}

@Composable
private fun ChatScreenPreviewHost(
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

    // 프리뷰에서도 최신이 날짜 바로 밑에 오도록 고정
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
                    text = "취준생 맞춤 상황",
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
