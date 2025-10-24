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
    onDateChange: (String) -> Unit,
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

    // 오늘 날짜 ("YYYY-MM-DD")
    val today = remember { toDateLabel(Calendar.getInstance()) }
    val isPast = selected < today // YYYY-MM-DD 포맷은 문자열 비교로 과거 판별 가능

    // 프리뷰에선 네트워크/콜백 실행 X
    if (!isPreview) {
        LaunchedEffect(selected) {
            latestOnDateChange(selected)
        }
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
    height: Dp = 28.dp
) {
    val isPreview = LocalInspectionMode.current
    val painter = if (!isPreview)
        runCatching { painterResource(R.drawable.img_reset) }.getOrNull()
    else null

    if (painter != null) {
        Image(
            painter = painter,
            contentDescription = "오늘 날짜로 이동",
            modifier = Modifier
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

///* ──────────────────────────────── 주간 캘린더 (숫자 칩) ──────────────────────────────── */
//@Composable
//private fun WeeklyCalendarBar(
//    selectedDateLabel: String, // "YYYY-MM-DD"
//    onPrevWeek: () -> Unit,
//    onNextWeek: () -> Unit,
//    onSelectDate: (String) -> Unit,
//    hasStudy: (String) -> Boolean
//) {
//    val week = remember(selectedDateLabel) { buildWeekFrom(selectedDateLabel) } // 월~일
//    val today = remember { toDateLabel(Calendar.getInstance()) }
//    val arrowSlotWidth = 28.dp // 좌우 화살표 폭(윗줄/아랫줄 정렬 맞춤)
//
//    // 1) 요일 라벨 행 (오늘 요일만 파란 텍스트 + 위쪽 파란 점)
//    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//        Box(Modifier.width(arrowSlotWidth)) { /* left spacer for alignment */ }
//
//        val todayDowIndex = remember(week, today) {
//            week.indexOfFirst { it == today }.coerceAtLeast(0)
//        }
//
//        listOf("월", "화", "수", "목", "금", "토", "일").forEachIndexed { i, label ->
//            val isTodayDow = i == todayDowIndex
//            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    // 위쪽 파란 점 (오늘 요일 표시)
//                    if (isTodayDow) {
//                        Box(
//                            Modifier
//                                .size(6.dp)
//                                .clip(RoundedCornerShape(50))
//                                .background(BrandBlue)
//                        )
//                    } else {
//                        Spacer(Modifier.height(6.dp))
//                    }
//                    Spacer(Modifier.height(2.dp))
//                    Text(
//                        text = label,
//                        fontSize = 14.sp,
//                        fontFamily = Pretendard,
//                        fontWeight = FontWeight.Medium,
//                        color = if (isTodayDow) BrandBlue else Color.Black
//                    )
//                }
//            }
//        }
//
//        Box(Modifier.width(arrowSlotWidth)) { /* right spacer for alignment */ }
//    }
//
//    Spacer(Modifier.height(8.dp))
//
//    // 2) 날짜 칩 행 (숫자 칩)
//    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//        // < 지난주
//        Box(
//            modifier = Modifier
//                .width(arrowSlotWidth)
//                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPrevWeek() },
//            contentAlignment = Alignment.Center
//        ) {
//            Text("<", fontSize = 18.sp, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, color = BrandBlue)
//        }
//
//        // 칩 크기 (피그마 간격 감안, 요일 라벨과 수평 정렬)
//        val chipWidth  = 44.dp
//        val chipHeight = 56.dp
//        val chipRadius = 12.dp
//
//        week.forEach { day ->
//            val isSelected = day == selectedDateLabel
//            val isToday = day == today
//            val studied = hasStudy(day)
//            val dayNum = day.takeLast(2).removePrefix("0")
//
//            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
//                DayChip(
//                    text = dayNum,              // ← 날짜(숫자), Pretendard 사용
//                    selected = isSelected,
//                    isToday = isToday,
//                    studied = studied,
//                    width = chipWidth,
//                    height = chipHeight,
//                    radius = chipRadius,
//                ) { onSelectDate(day) }
//            }
//        }
//
//        // > 다음주
//        Box(
//            modifier = Modifier
//                .width(arrowSlotWidth)
//                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onNextWeek() },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(">", fontSize = 18.sp, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, color = BrandBlue)
//        }
//    }
//}
//@Composable
//private fun DayChip(
//    text: String,
//    selected: Boolean,
//    isToday: Boolean,
//    studied: Boolean,
//    width: Dp,
//    height: Dp,
//    radius: Dp,
//    onClick: () -> Unit
//) {
//    val bg = if (selected) BrandBlue else GrayBg
//    val contentColor = if (selected) Color.White else Color.Black
//    val border = if (!selected && isToday) BorderStroke(1.5.dp, BrandBlue) else null
//
//    Surface(
//        shape = RoundedCornerShape(radius),
//        color = bg,
//        border = border,
//        shadowElevation = if (selected) 2.dp else 0.dp,
//        modifier = Modifier
//            .width(width)
//            .height(height)
//            .clip(RoundedCornerShape(radius))
//            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onClick() }
//    ) {
//        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                Spacer(Modifier.height(6.dp))
//                Text(
//                    text = text,
//                    fontSize = 18.sp,
//                    fontFamily = Pretendard,         // ✅ Pretendard로 날짜 숫자 표시
//                    fontWeight = FontWeight.Medium,
//                    color = contentColor
//                )
//                Spacer(Modifier.height(6.dp))
//                // 학습 여부 점(숫자 아래 도트)
//                if (studied) {
//                    Box(
//                        Modifier
//                            .size(6.dp)
//                            .clip(RoundedCornerShape(50))
//                            .background(if (selected) Color.White else BrandBlue)
//                    )
//                } else {
//                    Spacer(Modifier.height(6.dp))
//                }
//            }
//        }
//    }
//}
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
        // < 버튼
        Box(
            modifier = Modifier
                .width(arrowSlotWidth)
                .clickable { onPrevWeek() },
            contentAlignment = Alignment.Center
        ) {
            Text("<", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = BrandBlue)
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
        Box(
            modifier = Modifier
                .width(arrowSlotWidth)
                .clickable { onNextWeek() },
            contentAlignment = Alignment.Center
        ) {
            Text(">", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = BrandBlue)
        }
    }
}
///* ──────────────────────────────── 주간 캘린더 (피그마 칩) ──────────────────────────────── */
//@Composable
//private fun WeeklyCalendarBar(
//    selectedDateLabel: String, // "YYYY-MM-DD"
//    onPrevWeek: () -> Unit,
//    onNextWeek: () -> Unit,
//    onSelectDate: (String) -> Unit,
//    getProgressLevel: (String) -> Int  // ✅ 단계별 함수로 변경
//    //hasStudy: (String) -> Boolean // ★ 추가: 각 날짜 학습 여부
//
//) {
////    val week = remember(selectedDateLabel) { buildWeekFrom(selectedDateLabel) } // 월~일
////    val today = remember { toDateLabel(Calendar.getInstance()) }
////    val arrowSlotWidth = 28.dp
//    val week = remember(selectedDateLabel) { buildWeekFrom(selectedDateLabel) }
//    val today = remember { toDateLabel(Calendar.getInstance()) }
//    val arrowSlotWidth = 28.dp
//
//    // 1) 요일 라벨 행 (오늘 요일만 파란 텍스트 + 위쪽 파란 점)
//    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//        Box(Modifier.width(arrowSlotWidth)) { /* left spacer */ }
//
//        val todayDowIndex = remember(week, today) {
//            week.indexOfFirst { it == today }.coerceAtLeast(0)
//        }
//
//        listOf("월", "화", "수", "목", "금", "토", "일").forEachIndexed { i, label ->
//            val isTodayDow = i == todayDowIndex
//            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    // ● 오늘 점 표시 (위)
//                    if (isTodayDow) {
//                        Box(
//                            Modifier
//                                .size(6.dp)
//                                .clip(RoundedCornerShape(50))
//                                .background(BrandBlue)
//                        )
//                    } else {
//                        Spacer(Modifier.height(6.dp))
//                    }
//                    Spacer(Modifier.height(2.dp))
//                    Text(
//                        text = label,
//                        fontSize = 14.sp,
//                        fontFamily = Pretendard,
//                        fontWeight = FontWeight.Medium,
//                        color = if (isTodayDow) BrandBlue else Color.Black // 오늘은 파란 글자
//                    )
//                }
//            }
//        }
//
//        Box(Modifier.width(arrowSlotWidth)) { /* right spacer */ }
//    }
//
//    Spacer(Modifier.height(8.dp))
//
//    // 2) 날짜 칩 행
//    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//        // < 지난주
//        Box(
//            modifier = Modifier
//                .width(arrowSlotWidth)
//                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPrevWeek() },
//            contentAlignment = Alignment.Center
//        ) {
//            Text("<", fontSize = 18.sp, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, color = BrandBlue)
//        }
//
//        // 🔹 칩 크기: 정수 dp로 고정 (피그마 기준으로 맞춰도 됨)
//        val chipWidth  = 44.dp
//        val chipHeight = 56.dp
//        val chipRadius = 12.dp   // 모서리값은 '외부'에서만 적용(선택사항)
//
//        Row(
//            Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.Bottom // ✅ 모든 칸 밑 맞추기
//        ) {
//            week.forEach { day ->
//                val progress = getProgressLevel(day)
//                val resId = when (progress) {
//                    1 -> R.drawable.img_box_check1
//                    2 -> R.drawable.img_box_check2
//                    3 -> R.drawable.img_box_check3
//                    4 -> R.drawable.img_box_check   // 전체 완료
//                    else -> R.drawable.img_box_uncheck
//                }
//
//                // ✅ ▼ 여기서 직접 조절 가능 ▼
//                // ---------------------------------------
//                // 박스 크기 기본값
//                val baseWidth = 54.dp    // ← 전체 폭 (줄이거나 키우기)
//                val baseHeight = 62.dp   // ← 전체 높이
//                val cornerRadius = 12.dp // ← 둥근 모서리 정도
//
//                // 단계별 이미지 크기 (가로만 비율 다르게)
//                val imgWidthDp = when (progress) {
//                    1 -> 46.dp   // ← 1단계 이미지 폭
//                    2 -> 42.dp   // ← 2단계 이미지 폭
//                    3 -> 38.dp   // ← 3단계 이미지 폭
//                    4 -> 36.dp   // ← 전체 완료(가장 작게)
//                    else -> 50.dp // ← 빈칸 기본 크기
//                }
//                val imgHeightDp = 54.dp // ← 세로 크기 (필요시 조절)
//                // ---------------------------------------
//
//                // 빈칸만 살짝 아래로 내림 (밑선 맞추기용)
//                val yOffsetDp = if (progress == 0) 4.dp else 0.dp
//
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .height(72.dp)
//                        .padding(horizontal = 2.dp),
//                    contentAlignment = Alignment.BottomCenter
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .width(baseWidth)
//                            .height(baseHeight)
//                            .offset(y = yOffsetDp) // ✅ 빈칸만 밑으로 살짝
//                            .clip(RoundedCornerShape(cornerRadius))
//                            .background(Color.White)
//                            .clickable { onSelectDate(day) },
//                        contentAlignment = Alignment.BottomCenter
//                    ) {
//                        Image(
//                            painter = painterResource(id = resId),
//                            contentDescription = "학습 단계 $progress",
//                            modifier = Modifier
//                                .width(imgWidthDp)   // ✅ 폭 조절
//                                .height(imgHeightDp) // ✅ 높이 조절
//                                .align(Alignment.BottomCenter),
//                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
//                        )
//                    }
//                }
//            }
//        }
//
//        // > 다음주
//        Box(
//            modifier = Modifier
//                .width(arrowSlotWidth)
//                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onNextWeek() },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(">", fontSize = 18.sp, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, color = BrandBlue)
//        }
//    }
//    }

//파란 박스

/* ─────────────────────────────── 지난 본문 카드 ─────────────────────────────── */
@Composable
private fun OverviewCard(
    dateLabelForDisplay: String,   // "YYYY.MM.DD"
    bodyText: String?,
    onGoStudyClick: () -> Unit,
    onBodyClick: () -> Unit,
    showResetToToday: Boolean,          // ✅ 추가 + 오늘 날짜로 컴백
    onResetToToday: () -> Unit          // ✅ 추가 + 오늘 날짜로 컴택
) {
    // ✅ 오늘 날짜 계산 및 비교
    val today = remember { toDateLabel(Calendar.getInstance()) }      // "YYYY-MM-DD"
    val selectedDate = dateLabelForDisplay.replace(".", "-")          // "YYYY.MM.DD" → "YYYY-MM-DD"
    val btnEnabled = selectedDate == today                            // 오늘이면 true, 아니면 false

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp)  // ✅ 카드 높이 조금 늘림 (기본 높이 보장)
    ) {
        Column(Modifier.padding(16.dp)) {

            // ── 상단 헤더: 날짜 + (조건부) 오늘로 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateLabelForDisplay,
                    fontSize = 14.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF616161)
                )

                if (showResetToToday) {
                    ResetToTodayChip(
                        onClick = onResetToToday,
                        height = 28.dp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 글감 본문
            Text(
                text = bodyText?.takeIf { it.isNotBlank() } ?: "학습한 글감이 없습니다.",
                fontSize = 16.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                lineHeight = 25.6.sp,
                maxLines = 9,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onBodyClick() }
                    .padding(4.dp)
            )

            Spacer(Modifier.height(12.dp))

            // ── 하단 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val buttonColor =
                    if (btnEnabled) BrandBlue else Color(0xFFBDBDBD)  // 오늘은 파랑, 아니면 회색

                Text(
                    text = "학습하러 가기 >",
                    fontSize = 12.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    color = buttonColor,
                    modifier = Modifier.clickable(
                        enabled = btnEnabled, // ✅ 오늘만 클릭 가능
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { if (btnEnabled) onGoStudyClick() }
                )
            }
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

///* ─────────────────────────────── 프리뷰 ─────────────────────────────── */
//@Preview(showBackground = true, widthDp = 390)
//@Composable
//private fun Preview_StudyWeeklyScreen() {
//    val today = toDateLabel(Calendar.getInstance())
//
//    // 프리뷰용 더미: 월/화만 학습
//    val weekSet = remember(today) {
//        val week = buildWeekFrom(today)
//        setOf(week[0], week[1]) // 월, 화
//    }
//
//    val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//    StudyWeeklyScreen(
//        initialDateLabel = today,
//        onDateChange = { vm.refreshStudyProgressForWeek(LocalDate.parse(it)) },
//        bodyText = "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀가 있듯이 …”",
//        onBackClick = {},
//        onGoStudyClick = {},
//        onOpenPastStudy = {}
//       // getProgressLevel = { date -> if (weekSet.contains(date)) 4 else 0 } // ✅ 수정
//    )
//}

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

//    StudyWeeklyScreen(
//        initialDateLabel = today,
//        onDateChange = {},
//        bodyText = "단계별 박스 미리보기입니다.",
//        onGoStudyClick = {},
//        onOpenPastStudy = {},
//        getProgressLevel = { date -> progressMap[date] ?: 0 }
//    )
}