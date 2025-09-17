package com.malmungchi.feature.ai

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

// ----------------------
// 임시 모델 (그대로 유지)
// ----------------------
enum class Role { User, Bot }
data class ChatMessage(val role: Role, val text: String)

// ----------------------------------------------------
// 말풍선 (그대로 유지)
//  - Bot: 왼쪽 위만 직각
//  - User: 오른쪽 위만 직각
//  - TIP은 Bot 말풍선 내부 616161
// ----------------------------------------------------
@Composable
private fun ChatBubbleRectTopLeftSharp(
    text: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color(0xFFF7F7F7),
    corner: Dp = 16.dp,
    showTipInside: Boolean = false,
    userShape: Boolean = false
) {
    val lines = remember(text) { text.lines() }
    val tips = remember(text) { lines.filter { it.trim().startsWith("TIP:", ignoreCase = true) } }
    val body = remember(text) { lines.firstOrNull { !it.trim().startsWith("TIP:", ignoreCase = true) } ?: text }

    val shape =
        if (userShape) {
            RoundedCornerShape(topStart = corner, topEnd = 0.dp, bottomEnd = corner, bottomStart = corner)
        } else {
            RoundedCornerShape(topStart = 0.dp, topEnd = corner, bottomEnd = corner, bottomStart = corner)
        }

    Box(
        modifier = modifier
            .background(bgColor, shape)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column {
            Text(body, fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Color(0xFF222222))
            if (showTipInside && tips.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                tips.forEach { tip ->
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

// ----------------------
// 실제 ChatScreen (ViewModel 연동)
// ----------------------
@Composable
fun ChatScreen(
    vm: ChatViewModel = viewModel(),  // ✅ ViewModel 주입
    onBack: () -> Unit = {},
    onExit: () -> Unit = {},          // "대화 종료하기" 누르면 AiScreen으로 네비게이트
    onContinue: () -> Unit = {}       // "대화 이어가기" 계속 음성 대화
) {
    val state = vm.ui.value

    // 최신 대화가 "위"에서 보이도록
    val listState = rememberLazyListState()
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(0)
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
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
                ) { Icon(painter = painterResource(id = R.drawable.ic_back), contentDescription = "Back") }

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
                Text("2025.04.05", fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
            }

            Spacer(Modifier.height(24.dp))

            // ✅ 최신이 위에 보이게: reverseLayout = true (데이터 순서는 그대로)
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(bottom = 0.dp)
            ) {
                itemsIndexed(state.messages) { index, msg ->
                    // reverseLayout이므로 "화면상" 바로 위 아이템은 index+1
                    val nextRole = state.messages.getOrNull(index + 1)?.role
                    val topPad = if (nextRole != null && nextRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
                    Spacer(Modifier.height(topPad))

                    if (msg.role == Role.Bot) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            Image(
                                painter = painterResource(id = R.drawable.img_chatbot_malchi),
                                contentDescription = "Bot",
                                modifier = Modifier.size(48.dp).offset(y = (-2).dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            ChatBubbleRectTopLeftSharp(
                                text = msg.text,
                                modifier = Modifier.weight(1f).padding(end = 24.dp),
                                bgColor = Color(0xFFF7F7F7),
                                showTipInside = true
                            )
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            ChatBubbleRectTopLeftSharp(
                                text = msg.text,
                                modifier = Modifier.widthIn(max = 280.dp),
                                bgColor = Color.White,
                                userShape = true
                            )
                        }
                    }
                }
            }

            // XP와 마지막 말풍선 간격 4dp
            Spacer(Modifier.height(4.dp))

            if (state.botReplyCount >= 3) {
                // ─────── XP를 지급합니다 ───────
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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

                // 버튼 두 개 (텍스트만 감싸도록)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFC9CAD4), RoundedCornerShape(20.dp))
                            .clickable { onExit() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("대화 종료하기", fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.White) }

                    Spacer(Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF195FCF), RoundedCornerShape(20.dp))
                            .clickable { onContinue() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("대화 이어가기", fontFamily = Pretendard, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Color.White) }
                }
            }
        }

        // 하단 마이크: 녹음/업로드 중이면 ING 아이콘
        val isBusy = state.isRecording || state.isLoading
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp)) {
            Icon(
                painter = painterResource(id = if (isBusy) R.drawable.ic_chat_mike_ing else R.drawable.ic_chat_mike),
                contentDescription = "Mic",
                modifier = Modifier
                    .size(56.dp)
                    .clickable {
                        when {
                            !state.isRecording && !state.isLoading -> vm.startRecording() // 시작
                            state.isRecording -> vm.stopAndSend()  // 종료 + 서버 업로드
                        }
                    }
            )
        }
    }
}

/* ===========================
 * 프리뷰 (기존과 동일)
 * =========================== */
@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
fun ChatScreenPreview_After3Replies() {
    ChatScreen(
        onBack = {},
        onExit = {},
        onContinue = {}
    )
}


//package com.malmungchi.feature.ai
//
//import android.widget.Toast
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
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
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.malmungchi.core.designsystem.Pretendard
//import com.malmungchi.feature.ai.R
//
//// ----------------------
//// 임시 모델
//// ----------------------
//enum class Role { User, Bot }
//
//data class ChatMessage(
//    val role: Role,
//    val text: String
//)
//
//// ----------------------------------------------------
//// 말풍선
////  - 기본: 왼쪽 위 직각(봇용)
////  - 사용자 말풍선은 topRight만 직각으로 옵션 처리
//// ----------------------------------------------------
//@Composable
//private fun ChatBubbleRectTopLeftSharp(
//    text: String,
//    modifier: Modifier = Modifier,
//    bgColor: Color = Color(0xFFF7F7F7),
//    corner: Dp = 16.dp,
//    showTipInside: Boolean = false,
//    userShape: Boolean = false // ✅ 사용자 말풍선이면 true
//) {
//    // 본문/Tip 분리
//    val lines = remember(text) { text.lines() }
//    val tips = remember(text) { lines.filter { it.trim().startsWith("TIP:", ignoreCase = true) } }
//    val body = remember(text) {
//        lines.firstOrNull { !it.trim().startsWith("TIP:", ignoreCase = true) } ?: text
//    }
//
//    val shape =
//        if (userShape) {
//            // 사용자: 오른쪽 위만 직각
//            RoundedCornerShape(
//                topStart = corner,
//                topEnd = 0.dp,
//                bottomEnd = corner,
//                bottomStart = corner
//            )
//        } else {
//            // 봇: 왼쪽 위만 직각
//            RoundedCornerShape(
//                topStart = 0.dp,
//                topEnd = corner,
//                bottomEnd = corner,
//                bottomStart = corner
//            )
//        }
//
//    Box(
//        modifier = modifier
//            .background(bgColor, shape)
//            .padding(horizontal = 12.dp, vertical = 12.dp)
//    ) {
//        Column {
//            Text(
//                text = body,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                fontSize = 16.sp,
//                color = Color(0xFF222222)
//            )
//            if (showTipInside && tips.isNotEmpty()) {
//                Spacer(Modifier.height(6.dp))
//                tips.forEach { tip ->
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
//// 실제 ChatScreen
//// ----------------------
//@Composable
//fun ChatScreen(
//    onBack: () -> Unit = {},
//    onExit: () -> Unit = {},
//    onContinue: () -> Unit = {},
//    onMicToggle: (Boolean) -> Unit = {},
//    initialMessages: List<ChatMessage> = listOf(
//        ChatMessage(Role.Bot, "[면접 상황]\n: 본인의 장단점이 무엇인가요?")
//    )
//) {
//    val ctx = LocalContext.current
//
//    var isRecording by remember { mutableStateOf(false) }
//    var messages by remember { mutableStateOf(initialMessages) }
//
//    val botReplyCount by remember(messages) {
//        derivedStateOf { messages.count { it.role == Role.Bot } }
//    }
//
//    // 최신 대화를 항상 "위"에 보이게 렌더링
//    val renderItems by remember(messages) { mutableStateOf(messages.asReversed()) }
//
//    val listState = rememberLazyListState()
//    // 새 메시지 추가될 때 최상단(0번)으로 스크롤해서 최신이 보이게
//    LaunchedEffect(messages.size) {
//        if (messages.isNotEmpty()) listState.animateScrollToItem(0)
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
//                .padding(horizontal = 20.dp, vertical = 16.dp)
//        ) {
//            // 헤더
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                IconButton(
//                    onClick = onBack,
//                    modifier = Modifier
//                        .align(Alignment.CenterStart)
//                        .size(48.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_back),
//                        contentDescription = "Back"
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
//                    text = "2025.04.05",
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 14.sp,
//                    color = Color.Black
//                )
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            // 최신이 위에 보이게: messages.asReversed() 사용
//            LazyColumn(
//                modifier = Modifier.weight(1f),
//                state = listState,
//                reverseLayout = true,   // ✅ 리스트를 뒤집어서 최신 대화가 위로
//                contentPadding = PaddingValues(bottom = 0.dp) // 하단 패딩 없음
//            ) {
//                itemsIndexed(renderItems) { index, msg ->
//                    val prevRole = renderItems.getOrNull(index - 1)?.role
//                    val topPad =
//                        if (prevRole != null && prevRole != msg.role) crossRoleExtraSpacing else normalItemSpacing
//                    Spacer(Modifier.height(topPad))
//
//                    if (msg.role == Role.Bot) {
//                        Row(
//                            Modifier.fillMaxWidth(),
//                            verticalAlignment = Alignment.Top
//                        ) {
//                            Image(
//                                painter = painterResource(id = R.drawable.img_chatbot_malchi),
//                                contentDescription = "Bot",
//                                modifier = Modifier
//                                    .size(48.dp)
//                                    .offset(y = (-2).dp) // ✅ 살짝 위로 보정
//                            )
//                            Spacer(Modifier.width(6.dp)) // 아바타-말풍선 간격 좁게
//
//                            ChatBubbleRectTopLeftSharp(
//                                text = msg.text,
//                                modifier = Modifier
//                                    .weight(1f)
//                                    .padding(end = 24.dp),
//                                bgColor = Color(0xFFF7F7F7),
//                                showTipInside = true
//                            )
//                        }
//                    } else {
//                        Row(
//                            Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.End
//                        ) {
//                            ChatBubbleRectTopLeftSharp(
//                                text = msg.text,
//                                modifier = Modifier.widthIn(max = 280.dp),
//                                bgColor = Color.White,
//                                showTipInside = false,
//                                userShape = true // ✅ 사용자: topRight만 직각
//                            )
//                        }
//                    }
//                }
//            }
//
//            // ✅ XP와 마지막 말풍선 간격 4dp 고정
//            Spacer(Modifier.height(4.dp))
//
//            if (botReplyCount >= 3) {
//                // ─────── XP를 지급합니다 ───────
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 8.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Divider(
//                        modifier = Modifier.weight(1f),
//                        color = Color(0xFFCDD2DB),
//                        thickness = 1.dp
//                    )
//                    Text(
//                        text = "XP를 지급합니다",
//                        modifier = Modifier.padding(horizontal = 8.dp),
//                        fontFamily = Pretendard,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 12.sp,
//                        color = Color(0xFF616161)
//                    )
//                    Divider(
//                        modifier = Modifier.weight(1f),
//                        color = Color(0xFFCDD2DB),
//                        thickness = 1.dp
//                    )
//                }
//
//                Spacer(Modifier.height(8.dp))
//
//                // 작은 버튼 2개
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .background(Color(0xFFC9CAD4), RoundedCornerShape(20.dp))
//                            .clickable { onExit() }
//                            .padding(horizontal = 12.dp, vertical = 8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "대화 종료하기",
//                            fontFamily = Pretendard,
//                            fontWeight = FontWeight.Medium,
//                            fontSize = 12.sp,
//                            color = Color.White
//                        )
//                    }
//                    Spacer(Modifier.width(12.dp))
//                    Box(
//                        modifier = Modifier
//                            .background(Color(0xFF195FCF), RoundedCornerShape(20.dp))
//                            .clickable { onContinue() }
//                            .padding(horizontal = 12.dp, vertical = 8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "대화 이어가기",
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
//        // 하단 마이크 (녹음 토글)
//        Box(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 48.dp)
//        ) {
//            Icon(
//                painter = painterResource(
//                    id = if (isRecording) R.drawable.ic_chat_mike_ing else R.drawable.ic_chat_mike
//                ),
//                contentDescription = "Mic",
//                modifier = Modifier
//                    .size(56.dp)
//                    .clickable {
//                        isRecording = !isRecording
//                        onMicToggle(isRecording)
//
//                        // 데모: 녹음 종료 시 임시 메시지 추가
//                        if (!isRecording) {
//                            messages = messages + ChatMessage(
//                                Role.User,
//                                "저의 장점은 솔직함이고 단점도 솔직함입니다."
//                            )
//                            messages = messages + ChatMessage(
//                                Role.Bot,
//                                "그렇게 생각하시는 이유는 무엇인가요?\nTIP: 접속사의 사용이 부자연스러워요."
//                            )
//                            Toast.makeText(ctx, "가상의 서버 응답 추가(데모)", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//            )
//        }
//    }
//}
//
///* ===========================
// * 프리뷰 (봇 3회 응답 이후 상태)
// * =========================== */
//@Preview(showBackground = true, widthDp = 360, heightDp = 760)
//@Composable
//fun ChatScreenPreview_After3Replies() {
//    ChatScreen(
//        onBack = {},
//        onExit = {},
//        onContinue = {},
//        onMicToggle = {},
//        initialMessages = listOf(
//            ChatMessage(Role.Bot, "[면접 상황]\n: 본인의 장단점이 무엇인가요?"),
//            ChatMessage(Role.User, "저의 장점은 솔직함이고 단점은 솔직함입니다."),
//            ChatMessage(Role.Bot, "다시 한 번 해볼까요?\nTIP: 접속사의 사용이 부자연스러워요."),
//            ChatMessage(Role.User, "저의 장점은 솔직함이고 단점 또한 솔직함입니다."),
//            ChatMessage(Role.Bot, "왜 그렇게 생각하나요?\nTIP: 예시를 하나 더 들어볼까요?"),
//            ChatMessage(Role.User, "팀과의 커뮤니케이션이 좋아요."),
//            ChatMessage(Role.Bot, "좋아요! 그 경험을 말해주세요.\nTIP: STAR 구조를 시도해 보세요.")
//        )
//    )
//}




//package com.malmungchi.feature.ai
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Path
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.malmungchi.core.designsystem.Pretendard
//import com.malmungchi.feature.ai.R
//
//// ----------------------
//// 말풍선(왼쪽 위만 직각, 나머지 라운드)
//// ----------------------
//@Composable
//private fun ChatBubbleRectTopLeftSharp(
//    text: String,
//    modifier: Modifier = Modifier,
//    bgColor: Color = Color(0xFFF7F7F7),
//    corner: Dp = 16.dp
//) {
//    Box(modifier = modifier) {
//        Canvas(modifier = Modifier.matchParentSize()) {
//            val c = corner.toPx()
//
//            // 몸통: 왼쪽 위만 0, 나머지 라운드
//            // Compose Canvas엔 개별 코너 반경 지원이 없어 Path로 직접 그림
//            val w = size.width
//            val h = size.height
//
//            val path = Path().apply {
//                moveTo(0f, 0f)              // 좌상단(직각)
//                lineTo(w - c, 0f)
//                quadraticBezierTo(w, 0f, w, c) // 우상단 라운드
//                lineTo(w, h - c)
//                quadraticBezierTo(w, h, w - c, h) // 우하단 라운드
//                lineTo(c, h)
//                quadraticBezierTo(0f, h, 0f, h - c) // 좌하단 라운드
//                lineTo(0f, 0f)             // 좌상단으로
//                close()
//            }
//            drawPath(path = path, color = bgColor)
//        }
//
//        // 내용 패딩
//        Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
//            Text(
//                text = text,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,   // Pretendard 16 Medium
//                fontSize = 16.sp,
//                color = Color(0xFF222222)
//            )
//        }
//    }
//}
//
//@Composable
//fun ChatScreen(
//    onBack: () -> Unit = {},
//    onMicClick: () -> Unit = {}
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFE3E9F3)) // 전체 배경: E3E9F3
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 20.dp, vertical = 16.dp)
//        ) {
//            // 헤더: Back(48) + 제목(프리텐다드 20 세미볼드)
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(48.dp), // Back 버튼 높이와 동일하게 맞춤
//                contentAlignment = Alignment.Center
//            ) {
//                // Back 버튼 (왼쪽)
//                IconButton(
//                    onClick = onBack,
//                    modifier = Modifier
//                        .align(Alignment.CenterStart) // 왼쪽 정렬
//                        .size(48.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_back),
//                        contentDescription = "Back"
//                    )
//                }
//
//                // 가운데 제목
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
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // 날짜: 흰색 배경의 둥근 배지
//            Box(
//                modifier = Modifier
//                    .align(Alignment.CenterHorizontally)
//                    .background(color = Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(999.dp))
//                    .padding(horizontal = 12.dp, vertical = 6.dp)
//            ) {
//                Text(
//                    text = "2025.04.05",
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 14.sp,
//                    color = Color.Black
//                )
//            }
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // 챗봇 말풍선
//            Row(
//                verticalAlignment = Alignment.Top,
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.img_chatbot_malchi),
//                    contentDescription = "Chatbot",
//                    modifier = Modifier.size(48.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//
//                // 말풍선: 나머지 공간을 전부 차지하도록 weight(1f)
//                ChatBubbleRectTopLeftSharp(
//                    text = "[면접 상황]\n: 본인의 장단점이 무엇인가요?",
//                    modifier = Modifier
//                        .weight(1f) // <-- 이미지 빼고 남은 영역 꽉 차게
//                        .wrapContentHeight()
//                )
//            }
//        }
//
//        // 마이크 버튼: 하단에서 48
//        Box(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 48.dp)
//        ) {
//            Icon(
//                painter = painterResource(id = R.drawable.ic_chat_mike),
//                contentDescription = "Mic",
//                modifier = Modifier
//                    .size(56.dp)
//                    .clickable { onMicClick() }
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true, widthDp = 360, heightDp = 760)
//@Composable
//fun ChatScreenPreview() {
//    ChatScreen()
//}
