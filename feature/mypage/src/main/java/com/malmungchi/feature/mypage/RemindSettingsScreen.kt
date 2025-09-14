package com.malmungchi.feature.mypage

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.malmungchi.feature.mypage.R as MyPageR
import com.malmungchi.feature.mypage.remind.Ampm
import com.malmungchi.feature.mypage.remind.RemindTime
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.snapshotFlow


// ===== Colors =====
private val Blue_195FCF = Color(0xFF195FCF)
private val Gray_C9CAD4 = Color(0xFFC9CAD4)
private val Gray_E0E0E0 = Color(0xFFE0E0E0)
private val Gray_Disabled = Color(0xFFE0E0E0)
private val TextDefault = Color(0xFF262626)
private val Bg_EFF4FB = Color(0xFFEFF4FB)
private val ScreenPadding = 20.dp

// 최대 2회 안내 텍스트도 유지
//enum class Ampm(val label: String) { AM("오전"), PM("오후") }

//data class RemindTime(
//    val ampm: Ampm,
//    val hour: String,   // "01".."12"
//    val minute: String  // "00","10",...,"50"
//)

@Composable
fun RemindSettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (List<RemindTime>) -> Unit = {}
) {
    // 기본 값
    var firstOn by remember { mutableStateOf(true) }
    var firstAmpm by remember { mutableStateOf(Ampm.PM) }
    var firstHour by remember { mutableStateOf("02") }
    var firstMinute by remember { mutableStateOf("10") }

    var secondOn by remember { mutableStateOf(false) }
    var secondAmpm by remember { mutableStateOf(Ampm.PM) }
    var secondHour by remember { mutableStateOf("08") }
    var secondMinute by remember { mutableStateOf("30") }

    // 저장 검증 메시지 (간단 스낵바)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { scaffoldPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Bg_EFF4FB)
                .padding(scaffoldPadding)
        ) {
            // (A) 헤더 - 전폭 흰색
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // 상단 상태바 높이 등은 시스템 인셋으로 처리 가능하지만
                // 요구사항: 아이콘이 "위에서 48dp 간격" -> TopBar 내부 padding으로 해결
                TopBar(title = "리마인드 알림 설정", onBack = onBack)
                Divider(color = Color.Transparent, thickness = 12.dp)
            }

            // (B) 안내 밴드
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Bg_EFF4FB)
                    .padding(horizontal = ScreenPadding, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "하루에 두 번 메시지를 받을 수 있어요",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDefault
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 본문
            Column(Modifier.padding(horizontal = ScreenPadding)) {
                Spacer(Modifier.height(8.dp))

                RemindCard(
                    title = formattedTitle(firstAmpm, firstHour, firstMinute),
                    isOn = firstOn,
                    onToggle = { firstOn = it },
                    ampm = firstAmpm,
                    hour = firstHour,
                    minute = firstMinute,
                    onSelectAmpm = { firstAmpm = it },
                    onSelectHour = { firstHour = it },
                    onSelectMinute = { firstMinute = it }
                )

                Spacer(Modifier.height(16.dp))

                RemindCard(
                    title = formattedTitle(secondAmpm, secondHour, secondMinute),
                    isOn = secondOn,
                    onToggle = { secondOn = it },
                    ampm = secondAmpm,
                    hour = secondHour,
                    minute = secondMinute,
                    onSelectAmpm = { secondAmpm = it },
                    onSelectHour = { secondHour = it },
                    onSelectMinute = { secondMinute = it }
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        // 켜진 항목만 수집
                        val list = buildList {
                            if (firstOn) add(RemindTime(firstAmpm, firstHour, firstMinute))
                            if (secondOn) add(RemindTime(secondAmpm, secondHour, secondMinute))
                        }

                        // 검증: 최대 2개(현재 UI는 2개지만 방어), 중복 시간 금지, 최소 1개 권장
                        val distinct = list.distinctBy { it.ampm to (it.hour to it.minute) }
                        when {
                            list.isEmpty() -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("알림 시간을 하나 이상 켜주세요.")
                                }
                            }

                            distinct.size != list.size -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("동일한 시간대가 중복되었습니다. 시간을 변경해 주세요.")
                                }
                            }

                            list.size > 2 -> {
                                scope.launch {
                                    snackbarHostState.showSnackbar("하루에 최대 두 번까지 설정할 수 있어요.")
                                }
                            }

                            else -> {
                                // ✅ 저장 + 예약(기존 알람 취소 후 재등록 포함)
                                com.malmungchi.feature.mypage.remind.ReminderScheduler
                                    .saveAndSchedule(context, distinct)
                                Log.d("REMIND", "save: $distinct")
                                // ✅ 저장 성공 안내 (이 화면의 SnackbarHost에 표시)
                                scope.launch { snackbarHostState.showSnackbar("리마인드 시간이 저장됐어요.") }
                                onSave(distinct) // ✅ 프론트엔드에서 저장/스케줄링 연결
                            }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue_195FCF),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)                 // 너비 절반
                        .align(Alignment.CenterHorizontally) // 수평 가운데 정렬
                        .padding(bottom = 48.dp)            // 아래 간격 48
                        .height(48.dp)
                ) {
                    Text(
                        text = "저장하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private fun formattedTitle(ampm: Ampm, h: String, m: String): String {
    // 시간은 자연스럽게(1자리/2자리 상관 없이) 표기, 분은 두 자리 유지
    val hourInt = h.toIntOrNull() ?: 0
    val minuteLabel = m.padStart(2, '0')
    return "${ampm.label} ${hourInt}시 ${minuteLabel}분"
}

private val hours = (1..12).map { it.toString().padStart(2, '0') }
private val minutes = listOf("00", "10", "20", "30", "40", "50")

@Composable
private fun RemindCard(
    title: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    ampm: Ampm,
    hour: String,
    minute: String,
    onSelectAmpm: (Ampm) -> Unit,
    onSelectHour: (String) -> Unit,
    onSelectMinute: (String) -> Unit
) {
    val titleColor = if (isOn) Blue_195FCF else Gray_Disabled
    val onTrack = Blue_195FCF
    val offTrack = Gray_E0E0E0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title, // ✅ 휠에서 값 바뀌면 상위 상태가 변하고 여기 제목도 즉시 갱신
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = titleColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                )
                Switch(
                    checked = isOn,
                    onCheckedChange = onToggle,
                    modifier = Modifier.scale(0.8f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = onTrack,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = offTrack,
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                color = Gray_E0E0E0,
                thickness = 1.dp
            )

            Spacer(Modifier.height(12.dp))

            // 피커 래퍼
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(12.dp)
            ) {
                PickerRow(
                    enabled = isOn,
                    ampm = ampm,
                    hour = hour,
                    minute = minute,
                    onSelectAmpm = onSelectAmpm,
                    onSelectHour = onSelectHour,
                    onSelectMinute = onSelectMinute
                )
            }
        }
    }
}

@Composable
private fun PickerRow(
    enabled: Boolean,
    ampm: Ampm,
    hour: String,
    minute: String,
    onSelectAmpm: (Ampm) -> Unit,
    onSelectHour: (String) -> Unit,
    onSelectMinute: (String) -> Unit
) {
    val itemHeight = 34.dp
    val ampmItems = listOf(Ampm.AM.label, Ampm.PM.label)
    val ampmIndex = if (ampm == Ampm.AM) 0 else 1
    val hourIndex = hours.indexOf(hour).coerceAtLeast(0)
    val minuteIndex = minutes.indexOf(minute).coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // 중앙 하이라이트 줄
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(Bg_EFF4FB)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelPicker(
                items = ampmItems,
                selectedIndex = ampmIndex,
                onSelectedIndex = { idx -> onSelectAmpm(if (idx == 0) Ampm.AM else Ampm.PM) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                itemHeight = itemHeight
            )

            Spacer(Modifier.width(8.dp))

            WheelPicker(
                items = hours,
                selectedIndex = hourIndex,
                onSelectedIndex = { idx -> onSelectHour(hours[idx]) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                itemHeight = itemHeight
            )

            Spacer(Modifier.width(8.dp))

            WheelPicker(
                items = minutes,
                selectedIndex = minuteIndex,
                onSelectedIndex = { idx -> onSelectMinute(minutes[idx]) },
                enabled = enabled,
                modifier = Modifier.weight(1f),
                itemHeight = itemHeight
            )
        }
    }
}

@Composable
private fun WheelPicker(
    items: List<String>,
    selectedIndex: Int,
    onSelectedIndex: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    visibleCount: Int = 3,
    itemHeight: Dp = 36.dp,
) {
    val totalHeight = itemHeight * visibleCount
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }

    Box(
        modifier = modifier
            .height(totalHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)
    ) {
        LazyColumn(
            state = state,
            userScrollEnabled = enabled,
            contentPadding = PaddingValues(vertical = (totalHeight - itemHeight) / 2),
        ) {
            itemsIndexed(items) { index, value ->
                val isSel = index == selectedIndex
                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .clickable(enabled = enabled) {
                            onSelectedIndex(index)
                            // 👇 클릭했을 때만 스크롤 애니메이션
                            scope.launch { state.animateScrollToItem(index) }
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value,
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 20.sp,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium,
                            color = TextDefault
                        ),
                        modifier = if (enabled) Modifier else Modifier.alpha(0.45f)
                    )
                }
            }
        }
    }

    // 선택값이 외부에서 바뀌면 해당 위치로만 1회 스크롤(동기화)
    LaunchedEffect(selectedIndex) {
        if (state.firstVisibleItemIndex != selectedIndex) {
            state.scrollToItem(selectedIndex)
        }
    }

    // 🔧 스냅 로직: 멈춘 순간에만 실행, "필요할 때만" 애니메이션
    LaunchedEffect(state) {
        snapshotFlow { state.isScrollInProgress }.collect { scrolling ->
            if (!scrolling) {
                // 현재 중앙에 가장 가까운 인덱스 계산
                val raw = state.firstVisibleItemIndex.toFloat() +
                        (state.firstVisibleItemScrollOffset / itemHeightPx)
                val target = raw.roundToInt().coerceIn(0, items.lastIndex)

                val indexChanged = target != selectedIndex
                val notCentered = state.firstVisibleItemIndex != target ||
                        state.firstVisibleItemScrollOffset != 0

                if (indexChanged) {
                    onSelectedIndex(target)
                }
                if (notCentered) {
                    // 정말 필요할 때만 스냅
                    state.animateScrollToItem(target)
                }
            }
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    // 요구사항: 아이콘이 상단에서 48dp 간격
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ScreenPadding, end = ScreenPadding, top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        )
        IconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBack
        ) {
            Icon(
                painter = painterResource(id = MyPageR.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Black
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEFF4FB)
@Composable
private fun RemindSettingsPreview() {
    MaterialTheme {
        RemindSettingsScreen()
    }
}

