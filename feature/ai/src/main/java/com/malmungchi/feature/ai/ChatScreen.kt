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
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_chatbot_malchi),
                contentDescription = "Bot",
                modifier = Modifier
                    .size(40.dp) // ✅ 48 → 40으로 줄여서 말풍선 높이와 시각적으로 맞춤
                    .align(Alignment.Top) // ✅ Row 내에서 세로 정렬 맞추기
            )
            Spacer(Modifier.width(4.dp)) // ✅ 여백 살짝 줄이기 (6 → 4)
            ChatBubbleRectTopLeftSharp(
                text = msg.text,
                modifier = Modifier
                    .weight(1f, false) // ✅ 너무 넓게 퍼지지 않게 제한
                    .widthIn(max = bubbleWidthMax)
                    .wrapContentHeight(),
                bgColor = Color(0xFFF7F7F7),
                showTipInside = true,
                borderColor = if (msg.style == MBubbleStyle.BotFeedback)
                    Color(0xFF195FCF)
                else null
            )
        }
    } else {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ChatBubbleRectTopLeftSharp(
                text = msg.text,
                modifier = Modifier
                    .widthIn(max = bubbleWidthMax)
                    .wrapContentHeight(),
                bgColor = Color.White,
                userShape = true,
                borderColor = if (msg.style == MBubbleStyle.UserRetryNeeded)
                    Color(0xFFFF0D0D)
                else null
            )
        }
    }
}
//@Composable
//private fun MessageRow(msg: MChatMessage) {
//    val bubbleWidthMax = 280.dp
//    if (msg.role == MRole.Bot) {
//        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
//            Image(
//                painter = painterResource(id = R.drawable.img_chatbot_malchi),
//                contentDescription = "Bot",
//                modifier = Modifier.size(48.dp).offset(y = (-2).dp)
//            )
//            Spacer(Modifier.width(6.dp))
//            ChatBubbleRectTopLeftSharp(
//                text = msg.text,
//                modifier = Modifier.widthIn(max = bubbleWidthMax).wrapContentHeight(),
//                bgColor = Color(0xFFF7F7F7),
//                showTipInside = true,
//                borderColor = if (msg.style == MBubbleStyle.BotFeedback) Color(0xFF195FCF) else null
//            )
//        }
//    } else {
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
//            ChatBubbleRectTopLeftSharp(
//                text = msg.text,
//                modifier = Modifier.widthIn(max = bubbleWidthMax).wrapContentHeight(),
//                bgColor = Color.White,
//                userShape = true,
//                borderColor = if (msg.style == MBubbleStyle.UserRetryNeeded) Color(0xFFFF0D0D) else null
//            )
//        }
//    }
//}

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

    // ✅ 다이얼로그 표시 여부
    var showExitConfirm by remember { mutableStateOf(false) }

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

    // ✅ Bot 메시지 실제 카운트(안전장치)
    val botCount = remember(msgs, state.botReplyCount) {
        maxOf(state.botReplyCount, msgs.count { it.role == MRole.Bot })
    }

    // ✅ 시스템 뒤로가기: 3개 미만이면 다이얼로그, 아니면 바로 뒤로
    BackHandler {
        if (botCount < 3) {
            showExitConfirm = true
        } else {
            onBack()
        }
    }

    // ✅ 새 메시지 오면 "최근의 첫 아이템"이 리스트 맨 위(날짜 바로 밑)에 오도록 고정
    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) {
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
                    // ✅ 뒤로가기 버튼도 동일 분기
                    onClick = {
                        if (botCount < 3) showExitConfirm = true else onBack()
                    },
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

            // 오늘 날짜 계산 (YYYY.MM.DD 포맷)
            val todayLabel = remember {
                val now = java.time.LocalDate.now()
                "%04d.%02d.%02d".format(now.year, now.monthValue, now.dayOfMonth)
            }

            // 날짜 배지
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .background(Color.White, shape = RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = todayLabel,
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

        // ✅ 다이얼로그 렌더 (루트 Box의 마지막 자식으로)
        if (showExitConfirm) {
            EndChatConfirmDialog(
                onYes = {
                    showExitConfirm = false
                    onBack()             // 네 → AiScreen으로 이동
                },
                onNo = {
                    showExitConfirm = false  // 아니요 → 현재 화면 유지
                }
            )
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


