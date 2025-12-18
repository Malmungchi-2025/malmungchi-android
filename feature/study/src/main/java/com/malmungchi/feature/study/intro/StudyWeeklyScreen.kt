package com.malmungchi.feature.study.intro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.Pretendard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.SideEffect
import java.net.SocketTimeoutException
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.malmungchi.feature.study.StudyReadingViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate

/* ─────────────────────────────── 상수 ─────────────────────────────── */
private val BrandBlue = Color(0xFF195FCF)
private val GrayBg = Color(0xFFF2F4F7)
private val CardBg = Color(0xFFF5F5F5)

/* ─────────────────────────────── 로고 ─────────────────────────────── */
@Composable
private fun AppLogo(
    modifier: Modifier = Modifier,
    defaultHeight: Dp = 36.dp
) {
    val isPreview = LocalInspectionMode.current
    val painter = if (!isPreview)
        runCatching { painterResource(R.drawable.img_malmungchi_word) }.getOrNull()
    else null

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = "말뭉치 로고",
            modifier = modifier.height(defaultHeight) // 필요 시 외부 Modifier로 덮어쓰기 가능
        )
    } else {
        Text(
            text = "말뭉치",
            fontSize = 20.sp,
            fontFamily = Pretendard,
            fontWeight = FontWeight.SemiBold,
            color = BrandBlue,
            modifier = modifier
        )
    }
}

/**
 * 주간 캘린더 + 지난 본문 카드
 *
 * - initialDateLabel: "YYYY-MM-DD" (보통 오늘)
 * - onDateChange(dateLabel): 날짜가 바뀔 때마다 프론트에서 서버 호출
 * - bodyText: 현재 선택 날짜의 본문(없으면 null/빈 문자열)
 * - hasStudy(dateLabel): 해당 날짜에 학습 기록이 있으면 true (기본 false)
 */
@Composable
fun StudyWeeklyScreen(
    vm: StudyReadingViewModel, // ✅ 추가
    initialDateLabel: String,
    onDateChange: suspend (String) -> Unit,
    bodyText: String?,
    onBackClick: () -> Unit = {},
    onGoStudyClick: () -> Unit = {},
    onOpenPastStudy: (String) -> Unit = {}
   // getProgressLevel: (String) -> Int
) {
    val systemUi = rememberSystemUiController()
    SideEffect {
        systemUi.setStatusBarColor(color = Color.White, darkIcons = true)
        systemUi.setNavigationBarColor(color = Color.White, darkIcons = true)
    }

    //val progressMap by vm.progressMap.collectAsState() // ✅


    var selected by remember { mutableStateOf(initialDateLabel) }
    val latestOnDateChange by rememberUpdatedState(onDateChange)
    val isPreview = LocalInspectionMode.current

    //var isLoading by remember { mutableStateOf(false) }
    val isQuoteReady by vm.isQuoteReady.collectAsState()

    // 오늘 날짜 ("YYYY-MM-DD")
    val today = remember { toDateLabel(Calendar.getInstance()) }
    val isPast = selected < today // YYYY-MM-DD 포맷은 문자열 비교로 과거 판별 가능

    // 프리뷰에선 네트워크/콜백 실행 X
    if (!isPreview) {
        LaunchedEffect(selected) {
            latestOnDateChange(selected)
        }
//        LaunchedEffect(selected) {
//            isLoading = true       //  글감 불러오기 시작
//            try {
//                latestOnDateChange(selected)
//            } finally {
//                isLoading = false  //  글감 불러오기 끝
//            }
//        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp)
    ) {
        // 상단 로고(센터)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AppLogo(modifier = Modifier.height(18.dp))
        }

        Spacer(Modifier.height(24.dp))

        // ✅ ViewModel로부터 progressMap을 관찰
        val progressMap by vm.progressMap.collectAsState()

        WeeklyCalendarBar(
            selectedDateLabel = selected,
            onPrevWeek = { selected = shiftDays(selected, -7) },
            onNextWeek = { selected = shiftDays(selected, +7) },
            onSelectDate = { picked -> selected = picked },
            progressMap = progressMap // ✅ 직접 전달
        )
        Spacer(Modifier.height(24.dp))

        // 본문 카드 탭 → 과거면 지난학습으로 이동
        OverviewCard(
            dateLabelForDisplay = selected.replace("-", "."),
            bodyText = bodyText,
            isLoading = !isQuoteReady,
            onGoStudyClick = onGoStudyClick,
            onBodyClick = {
                if (isPast) onOpenPastStudy(selected)
            },
            showResetToToday = selected != today,     // ✅ 오늘이 아니면 버튼 표시
            onResetToToday = {                        // ✅ 클릭 시 오늘로
                selected = today
            }
        )
    }
}

//카드 우측용 “오늘로” 칩 컴포저블 추가
@Composable
private fun ResetToTodayChip(
    onClick: () -> Unit,
    height: Dp = 28.dp,
    modifier: Modifier = Modifier
) {
    val isPreview = LocalInspectionMode.current
    val painter = if (!isPreview)
        runCatching { painterResource(R.drawable.img_reset) }.getOrNull()
    else null

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = "오늘 날짜로 이동",
            modifier = modifier
                .height(height)
                .clip(RoundedCornerShape(999.dp))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onClick() }
        )
    } else {
        // 대체(프리뷰)
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color(0xFFFFE066), // 필요시 브랜드 노랑으로 교체
            shadowElevation = 0.dp
        ) {
            Text(
                text = "오늘 날짜로",
                fontSize = 12.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                modifier = Modifier
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClick() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}


/* ──────────────────────────────── 주간 캘린더 (완성: 하단 정렬) ─────────────────────────────── */
@Composable
private fun WeeklyCalendarBar(
    selectedDateLabel: String,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectDate: (String) -> Unit,
    progressMap: Map<String, Int> // ✅ Map 전달
){
    val week = remember(selectedDateLabel) { buildWeekFrom(selectedDateLabel) }
    val today = remember { toDateLabel(Calendar.getInstance()) }
    val arrowSlotWidth = 28.dp

    // ───── 요일 라벨 행 ─────
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(arrowSlotWidth))
        val todayIndex = remember(week, today) { week.indexOfFirst { it == today }.coerceAtLeast(0) }

        listOf("월", "화", "수", "목", "금", "토", "일").forEachIndexed { i, label ->
            val isToday = i == todayIndex
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isToday)
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(BrandBlue)
                        )
                    else Spacer(Modifier.height(6.dp))
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        color = if (isToday) BrandBlue else Color.Black
                    )
                }
            }
        }
        Box(Modifier.width(arrowSlotWidth))
    }

    Spacer(Modifier.height(0.dp))

    // ───── 날짜 박스 행 ─────
    Row(
        Modifier
            .fillMaxWidth()
            .height(72.dp), // 전체 높이 고정
        verticalAlignment = Alignment.Bottom
    ) {

        // < 버튼 (왼쪽 이동)
        Box(
            modifier = Modifier
                .width(arrowSlotWidth)
                .offset(y = (-20).dp)
                .clickable { onPrevWeek() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_weeklty_left_button),
                contentDescription = "이전 주",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
        }
        // 요일별 칸
        week.forEach { day ->
            val progress = progressMap[day] ?: 0
            //val progress = getProgressLevel(day)
            val resId = when (progress) {
                1 -> R.drawable.img_box_check1
                2 -> R.drawable.img_box_check2
                3 -> R.drawable.img_box_check3
                4 -> R.drawable.img_box_check
                else -> R.drawable.img_box_uncheck
            }

            val isUncheck = resId == R.drawable.img_box_uncheck  // ✅ 이 줄 추가!

            // ✅ 각 단계별 이미지 높이만 다르게 (밑변 고정)
            val imgHeightDp = when (progress) {
                1 -> 24.dp   // 1단계 (짧음)
                2 -> 36.dp   // 2단계
                3 -> 48.dp   // 3단계
                4 -> 56.dp   // 완료
                else -> 18.dp // 미학습 (아주 낮음)
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 2.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onSelectDate(day) }, // 🔹 Box 전체가 클릭 가능!
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "단계 $progress",
                    modifier = Modifier
                        .width(if (isUncheck) 52.dp else 40.dp)
                        .height(if (isUncheck) imgHeightDp + 32.dp else imgHeightDp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }

        // > 버튼
        // > 버튼 (오른쪽 이동)
        Box(
            modifier = Modifier
                .width(arrowSlotWidth)
                .offset(y = (-20).dp)
                .clickable { onNextWeek() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_weeklty_right_button),
                contentDescription = "다음 주",
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}


/* ─────────────────────────────── 지난 본문 카드 ─────────────────────────────── */
@Composable
private fun OverviewCard(
    dateLabelForDisplay: String,   // "YYYY.MM.DD"
    bodyText: String?,
    isLoading: Boolean,
    onGoStudyClick: () -> Unit,
    onBodyClick: () -> Unit,
    showResetToToday: Boolean,
    onResetToToday: () -> Unit
) {
    val today = remember { toDateLabel(Calendar.getInstance()) }
    val selectedDate = dateLabelForDisplay.replace(".", "-")
    // bodyText가 null이 아니고 비어있지 않을 때 true
    val hasBody = bodyText?.isNotBlank() == true

    // 조건: 오늘 날짜 AND 글감 존재 -> 오늘의 학습 글감이 다 눌러온 뒤에만 학습하러가기 버튼 활성화되게 수정함.
    val btnEnabled = (selectedDate == today) && hasBody && !isLoading

    // ✅ 카드 + 칩 전체를 감싸는 Box
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter // 칩이 중앙 하단으로 오게
    ) {
        // ── 카드 본체 ──
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBg,
            shadowElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(460.dp) // 카드 자체 높이 고정
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 상단 날짜 + 본문
                Column {
                    Text(
                        text = dateLabelForDisplay,
                        fontSize = 14.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161),
                        modifier = Modifier.offset(x = 4.dp,y = 12.dp)
                    )

                    Spacer(Modifier.height(24.dp))

                    // 로딩 중에는 “글감을 불러오는 중입니다.” 표시
                    val displayText = when {
                        isLoading || bodyText == null -> "글감이 생성 중입니다 :)"
                        bodyText.isNotBlank() -> bodyText
                        else -> "학습한 글감이 없습니다."
                    }

                    Text(
                        //text = bodyText?.takeIf { it.isNotBlank() } ?: "학습한 글감이 없습니다.",
                        text = displayText,
                        fontSize = 16.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333),
                        lineHeight = 25.6.sp,
                        maxLines = 12,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                enabled = !isLoading,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onBodyClick() }
                            .padding(4.dp)
                    )
                }

                // 하단: 학습하러 가기 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val buttonColor =
                        if (btnEnabled) BrandBlue else Color(0xFFBDBDBD)

                    Text(
                        text = "학습하러 가기 →",
                        fontSize = 16.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        color = buttonColor,
                        modifier = Modifier
                            .offset(x = (-10).dp, y = (-16).dp)  // ← 왼쪽 25dp, 위로 25dp 이동!!
                            .clickable(
                                enabled = btnEnabled,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { if (btnEnabled) onGoStudyClick() }
                    )
                }
            }
        }

        // ── 카드 외부 하단 중앙 “오늘 날짜로” 칩 ──
        if (showResetToToday) {
            ResetToTodayChip(
                onClick = onResetToToday,
                height = 40.dp,
                modifier = Modifier.offset(y = 68.dp) // ✅ 이렇게 Modifier로 전달!
            )
        }
    }
}

/* ──────────────────────────────── 날짜 유틸 ──────────────────────────────── */
// "YYYY-MM-DD" → 해당 날짜가 포함된 주의 월~일까지 7일
private fun buildWeekFrom(dateLabel: String): List<String> {
    val cal = parseCal(dateLabel)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 일(1)~토(7)
    val offsetToMonday = when (dayOfWeek) {
        Calendar.SUNDAY -> -6
        else -> Calendar.MONDAY - dayOfWeek
    }
    cal.add(Calendar.DAY_OF_MONTH, offsetToMonday)
    return (0 until 7).map {
        val label = toDateLabel(cal)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        label
    }
}

// 날짜 이동
private fun shiftDays(dateLabel: String, deltaDays: Int): String {
    val cal = parseCal(dateLabel)
    cal.add(Calendar.DAY_OF_MONTH, deltaDays)
    return toDateLabel(cal)
}

// Parser
private fun parseCal(dateLabel: String): Calendar {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    return Calendar.getInstance().apply {
        time = sdf.parse(dateLabel) ?: Date()
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
}

// Calendar → "YYYY-MM-DD"
private fun toDateLabel(cal: Calendar): String {
    val y = cal.get(Calendar.YEAR)
    val m = cal.get(Calendar.MONTH) + 1
    val d = cal.get(Calendar.DAY_OF_MONTH)
    return "%04d-%02d-%02d".format(y, m, d)
}



@Preview(showBackground = true, widthDp = 390, name = "단계별 박스 프리뷰")
@Composable
private fun Preview_StudyWeeklyScreen_ProgressLevels() {
    val today = toDateLabel(Calendar.getInstance())
    val week = buildWeekFrom(today)

    val progressMap = mapOf(
        week[0] to 0, // 월: 미학습
        week[1] to 1, // 화: 1단계
        week[2] to 2, // 수: 2단계
        week[3] to 3, // 목: 3단계
        week[4] to 4, // 금: 전체 완료
        week[5] to 0,
        week[6] to 0
    )


}

@Preview(showBackground = true, widthDp = 390, name = "주간 학습 화면 프리뷰 (독립 버전)")
@Composable
private fun Preview_StudyWeeklyScreen() {
    val today = toDateLabel(Calendar.getInstance())
    val week = buildWeekFrom(today)

    val progressMap = mapOf(
        week[0] to 0,
        week[1] to 1,
        week[2] to 2,
        week[3] to 3,
        week[4] to 4,
        week[5] to 0,
        week[6] to 0
    )

    // ✅ VM 의존 없는 화면
    StudyWeeklyScreenPreviewOnly(
        initialDateLabel = today,
        progressMap = progressMap,
        bodyText = "" // ← 비어 있어도 버튼 아래 고정
    )
}

@Composable
private fun StudyWeeklyScreenPreviewOnly(
    initialDateLabel: String,
    progressMap: Map<String, Int>,
    bodyText: String?
) {
    var selected by remember { mutableStateOf(initialDateLabel) }
    val today = remember { toDateLabel(Calendar.getInstance()) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        AppLogo(Modifier.height(18.dp))
        Spacer(Modifier.height(24.dp))

        WeeklyCalendarBar(
            selectedDateLabel = selected,
            onPrevWeek = { selected = shiftDays(selected, -7) },
            onNextWeek = { selected = shiftDays(selected, +7) },
            onSelectDate = { selected = it },
            progressMap = progressMap
        )

        Spacer(Modifier.height(24.dp))

        OverviewCard(
            dateLabelForDisplay = selected.replace("-", "."),
            bodyText = bodyText,
            isLoading = false,
            onGoStudyClick = {},
            onBodyClick = {},
            showResetToToday = selected != today,
            onResetToToday = { selected = today }
        )
    }
}

@Preview(showBackground = true, widthDp = 390, name = "오늘로 버튼 하단 중앙 위치 프리뷰")
@Composable
private fun Preview_OverviewCard_TodayButtonBottomCenter() {
    OverviewCard(
        dateLabelForDisplay = "2025.11.11",
        bodyText = "",
        isLoading = false,
        onGoStudyClick = {},
        onBodyClick = {},
        showResetToToday = true, // ✅ 프리뷰에서 표시되게
        onResetToToday = {}
    )
}