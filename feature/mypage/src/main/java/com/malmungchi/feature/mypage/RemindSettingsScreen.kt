package com.malmungchi.feature.mypage


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import kotlinx.coroutines.launch
import com.malmungchi.feature.mypage.R as MyPageR

// ===== Colors (피그마 스펙) =====
private val Blue_195FCF = Color(0xFF195FCF)
private val Gray_C9CAD4 = Color(0xFFC9CAD4)
private val Gray_E0E0E0 = Color(0xFFE0E0E0)
private val Gray_Disabled = Color(0xFFE0E0E0)
private val TextDefault = Color(0xFF262626)
private val Bg_EFF4FB = Color(0xFFEFF4FB)
private val ScreenPadding = 20.dp

// ✅ Ampm는 한 번만 선언 (public 또는 internal)
enum class Ampm(val label: String) { AM("오전"), PM("오후") }

data class RemindTime(
    val ampm: Ampm,
    val hour: String,
    val minute: String
)

@Composable
fun RemindSettingsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: (List<RemindTime>) -> Unit = {}
) {
    var firstOn by remember { mutableStateOf(true) }
    var firstAmpm by remember { mutableStateOf(Ampm.PM) }
    var firstHour by remember { mutableStateOf("02") }
    var firstMinute by remember { mutableStateOf("10") }

    var secondOn by remember { mutableStateOf(false) }
    var secondAmpm by remember { mutableStateOf(Ampm.PM) }
    var secondHour by remember { mutableStateOf("08") }
    var secondMinute by remember { mutableStateOf("30") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Bg_EFF4FB)     // 전체 배경
    ) {
        // ⬇️ (A) 헤더 - 전폭 흰색
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Spacer(Modifier.height(12.dp))
            // 내부 텍스트/아이콘만 좌우 패딩
            Box(Modifier.padding(horizontal = ScreenPadding)) {
                TopBar(title = "리마인드 알림 설정", onBack = onBack)
            }
            Spacer(Modifier.height(12.dp))
        }

        // ⬇️ (B) 안내 밴드 - 전폭 EFF4FB
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

        // 본문(카드들)부터 좌우 패딩
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
                    val list = buildList {
                        if (firstOn) add(RemindTime(firstAmpm, firstHour, firstMinute))
                        if (secondOn) add(RemindTime(secondAmpm, secondHour, secondMinute))
                    }
                    onSave(list)
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)   // 👈 StudyComplete와 동일
                    .height(48.dp)
            ) {
                Text(
                    text = "저장하기",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun formattedTitle(ampm: Ampm, h: String, m: String): String {
    val hourInt = h.toIntOrNull() ?: 0
    val hourForTitle = hourInt.toString()
    return "${ampm.label} ${hourForTitle}시 ${m}분"
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
                    text = title,
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

            // ⬇️ 피커 래퍼: 흰색 + 라운드 + 옅은 보더
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
    val itemHeight = 34.dp   // 피그마 느낌 높이
    val ampmItems = listOf(Ampm.AM.label, Ampm.PM.label)
    val ampmIndex = if (ampm == Ampm.AM) 0 else 1
    val hourIndex = hours.indexOf(hour).coerceAtLeast(0)
    val minuteIndex = minutes.indexOf(minute).coerceAtLeast(0)

    // 카드 내부 래퍼(흰색)는 기존 RemindCard에서 감싸고 있으므로 여기선 내용만
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // ▼▼ “한 줄” 하이라이트: 텍스트보다 먼저(뒤) 그려짐
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(Bg_EFF4FB)
        )

        // ▼ 실제 3개 컬럼 (배경/하이라이트 없음)
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

            Spacer(Modifier.width(8.dp))  // 간격 좁게

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
private fun SegButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val bg = when {
        !enabled -> Color(0xFFF5F5F5)
        selected -> Bg_EFF4FB
        else -> Color.White
    }
    val color = when {
        !enabled -> Gray_Disabled
        selected -> TextDefault
        else -> Gray_Disabled
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = color
            )
        )
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

    Box(
        modifier = modifier
            .height(totalHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Transparent)   // ← 배경 제거 (공통 하이라이트만 사용)
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
                            scope.launch { state.animateScrollToItem(index) }
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = value,
                        // 항상 또렷—선택 시 굵게만
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontSize = 20.sp,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium,
                            color = TextDefault
                        ),
                        // 비활성 카드일 때만 투명도
                        modifier = if (enabled) Modifier else Modifier.alpha(0.45f)
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedIndex) {
        if (state.firstVisibleItemIndex != selectedIndex) {
            state.scrollToItem(selectedIndex)
        }
    }
}

@Composable
private fun WheelColumn(
    items: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabled: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (enabled) Bg_EFF4FB else Color(0xFFF5F5F5))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items.forEach { value ->
                    val isSel = value == selected
                    val color = when {
                        !enabled -> Gray_Disabled
                        isSel -> TextDefault
                        else -> Gray_Disabled
                    }
                    Text(
                        text = value,
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 20.sp,
                            color = color
                        ),
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable(enabled = enabled) { onSelect(value) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
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
                tint = MaterialTheme.colorScheme.onBackground
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