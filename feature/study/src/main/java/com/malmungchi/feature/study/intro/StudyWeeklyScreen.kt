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
import com.google.accompanist.systemuicontroller.rememberSystemUiController

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
    initialDateLabel: String,
    onDateChange: (String) -> Unit,
    bodyText: String?,
    onBackClick: () -> Unit = {},
    onGoStudyClick: () -> Unit = {},
    onOpenPastStudy: (String) -> Unit = {},
    hasStudy: (String) -> Boolean
   // hasStudy: (String) -> Boolean = { false } // ★ 학습 여부 판단 주입
) {

    // ✨ 이 화면 진입 시 시스템바 색을 확실히 흰색으로
    val systemUi = rememberSystemUiController()
    SideEffect {
        systemUi.setStatusBarColor(color = Color.White, darkIcons = true)
        systemUi.setNavigationBarColor(color = Color.White, darkIcons = true)
    }

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

        // 주간 캘린더 바
        WeeklyCalendarBar(
            selectedDateLabel = selected,
            onPrevWeek = { selected = shiftDays(selected, -7) },
            onNextWeek = { selected = shiftDays(selected, +7) },
            onSelectDate = { picked ->
                selected = picked
                // LaunchedEffect(selected)에서 onDateChange 실행 → API 호출
            },
            hasStudy = hasStudy // ★ 학습 여부 전달
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

/* ──────────────────────────────── 주간 캘린더 (피그마 칩) ──────────────────────────────── */
@Composable
private fun WeeklyCalendarBar(
    selectedDateLabel: String, // "YYYY-MM-DD"
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onSelectDate: (String) -> Unit,
    hasStudy: (String) -> Boolean // ★ 추가: 각 날짜 학습 여부

) {
    val week = remember(selectedDateLabel) { buildWeekFrom(selectedDateLabel) } // 월~일
    val today = remember { toDateLabel(Calendar.getInstance()) }
    val arrowSlotWidth = 28.dp

    // 1) 요일 라벨 행 (오늘 요일만 파란 텍스트 + 위쪽 파란 점)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(arrowSlotWidth)) { /* left spacer */ }

        val todayDowIndex = remember(week, today) {
            week.indexOfFirst { it == today }.coerceAtLeast(0)
        }

        listOf("월", "화", "수", "목", "금", "토", "일").forEachIndexed { i, label ->
            val isTodayDow = i == todayDowIndex
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // ● 오늘 점 표시 (위)
                    if (isTodayDow) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(BrandBlue)
                        )
                    } else {
                        Spacer(Modifier.height(6.dp))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        color = if (isTodayDow) BrandBlue else Color.Black // 오늘은 파란 글자
                    )
                }
            }
        }

        Box(Modifier.width(arrowSlotWidth)) { /* right spacer */ }
    }

    Spacer(Modifier.height(8.dp))

    // 2) 날짜 칩 행
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        // < 지난주
        Box(
            modifier = Modifier
                .width(arrowSlotWidth)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPrevWeek() },
            contentAlignment = Alignment.Center
        ) {
            Text("<", fontSize = 18.sp, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, color = BrandBlue)
        }

        // 🔹 칩 크기: 정수 dp로 고정 (피그마 기준으로 맞춰도 됨)
        val chipWidth  = 44.dp
        val chipHeight = 56.dp
        val chipRadius = 12.dp   // 모서리값은 '외부'에서만 적용(선택사항)

        week.forEach { day ->
            val studied = hasStudy(day)

            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                // ⚠️ 외부에서만 동일하게 모서리 강제 (이미지 모서리가 달라도 보이는 모양 통일)
                Box(
                    modifier = Modifier
                        .width(chipWidth)
                        .height(chipHeight)
                        .clip(RoundedCornerShape(chipRadius))   // ← 외부에서만 클립
                        .clickable { onSelectDate(day) },
                    contentAlignment = Alignment.Center
                ) {
                    val resId = if (studied) R.drawable.img_box_check_new else R.drawable.img_box_uncheck
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = if (studied) "학습 완료" else "미학습",
                        modifier = Modifier
                            .width(chipWidth)
                            .height(chipHeight),
                        // ✅ 프레임 꽉 채우기: 내부 여백 차이 무시하고 동일 프레임에 맞춤
                        contentScale = androidx.compose.ui.layout.ContentScale.FillBounds
                    )
                }
            }
        }

        // > 다음주
        Box(
            modifier = Modifier
                .width(arrowSlotWidth)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onNextWeek() },
            contentAlignment = Alignment.Center
        ) {
            Text(">", fontSize = 18.sp, fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, color = BrandBlue)
        }
    }
    }

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
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
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
                    ResetToTodayChip(              // 👈 아래 3)에서 정의
                        onClick = onResetToToday,
                        height = 28.dp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = bodyText?.takeIf { it.isNotBlank() } ?: "학습한 글감이 없습니다.",
                fontSize = 16.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                lineHeight = 25.6.sp,   // 16sp × 1.6 = 160%
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "학습하러 가기 >",
                    fontSize = 12.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF616161),
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onGoStudyClick() }
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

/* ─────────────────────────────── 프리뷰 ─────────────────────────────── */
@Preview(showBackground = true, widthDp = 390)
@Composable
private fun Preview_StudyWeeklyScreen() {
    val today = toDateLabel(Calendar.getInstance())

    // 프리뷰용 더미: 월/화만 학습
    val weekSet = remember(today) {
        val week = buildWeekFrom(today)
        setOf(week[0], week[1]) // 월, 화
    }

    StudyWeeklyScreen(
        initialDateLabel = today,
        onDateChange = { /* 프리뷰: 서버 호출 생략 */ },
        bodyText = "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀가 있듯이 …”",
        onBackClick = {},
        onGoStudyClick = {},
        hasStudy = { date -> weekSet.contains(date) }
    )
}

/* ─────────────────────────────── 참고: 기존 날짜 숫자 칩 버전은 전부 주석 처리 ───────────────────────────────
   (요청에 따라 날짜 숫자를 제거하고, 학습 여부 이미지로 대체되었습니다)
*/
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.layout.size
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.layout.size
//
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberUpdatedState
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalInspectionMode
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.malmungchi.feature.study.Pretendard
//import com.malmungchi.feature.study.R
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//
///**
// * 주간 캘린더 + 지난 본문 카드 (별도 페이지)
// *
// * - initialDateLabel: "YYYY-MM-DD" (보통 오늘)
// * - onDateChange(dateLabel): 날짜가 바뀔 때마다 프론트에서 서버 호출
// * - bodyText: 현재 선택 날짜의 본문(없으면 null/빈 문자열)
// */
//
//@Composable
//private fun AppLogo(
//    modifier: Modifier = Modifier,
//    defaultHeight: Dp = 36.dp   //  기본 48dp
//) {
//    val isPreview = LocalInspectionMode.current
//    val painter = if (!isPreview)
//        runCatching { painterResource(R.drawable.ic_mal) }.getOrNull()
//    else null
//
//    if (painter != null) {
//        Image(painter = painter, contentDescription = "말뭉치 로고", modifier = modifier.height(defaultHeight))
//    } else {
//        Text(
//            text = "말뭉치",
//            fontSize = 20.sp,
//            fontFamily = Pretendard,
//            fontWeight = FontWeight.SemiBold,
//            color = Color(0xFF195FCF),
//            modifier = modifier
//        )
//    }
//}
//
//@Composable
//fun StudyWeeklyScreen(
//    initialDateLabel: String,
//    onDateChange: (String) -> Unit,
//    bodyText: String?,
//    onBackClick: () -> Unit = {},
//    onGoStudyClick: () -> Unit = {},
//    onOpenPastStudy: (String) -> Unit = {}
//) {
//    var selected by remember { mutableStateOf(initialDateLabel) }
//    val latestOnDateChange by rememberUpdatedState(onDateChange)
//    val isPreview = LocalInspectionMode.current
//
//
//    // 오늘 날짜 ("YYYY-MM-DD")
//    val today = remember { toDateLabel(Calendar.getInstance()) }
//    val isPast = selected < today // YYYY-MM-DD 포맷은 문자열 비교로 과거 판별 가능
//
//    // 프리뷰에선 네트워크/콜백 실행 X
//    if (!isPreview) {
//        LaunchedEffect(selected) { latestOnDateChange(selected) }
//    }
//
//    Column(
//        Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 32.dp) // 위 32 / 좌우·아래 16
//    ) {
//        // 상단 로고(센터)
//        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
//            AppLogo(modifier = Modifier.height(36.dp))
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        // 주간 캘린더 바
//        WeeklyCalendarBar(
//            selectedDateLabel = selected,
//            onPrevWeek = { selected = shiftDays(selected, -7) },
//            onNextWeek = { selected = shiftDays(selected, +7) },
//            onSelectDate = { picked ->
//                // 이동하지 않고, 선택 날짜의 본문만 로드
//                selected = picked
//                // LaunchedEffect(selected)에서 onDateChange 실행 → API 호출
//            }
//        )
//
//        Spacer(Modifier.height(24.dp))
//
//        // 본문 카드 탭 → 과거면 지난학습으로 이동
//        OverviewCard(
//            dateLabelForDisplay = selected.replace("-", "."),
//            bodyText = bodyText,
//            onGoStudyClick = onGoStudyClick,
//            onBodyClick = {
//                if (isPast) onOpenPastStudy(selected)
//                // 오늘/미래는 아무 동작 없이 그대로 두거나, 필요 시 토스트/스낵바 처리 가능
//            }
//        )
//    }
//}
//
///* ──────────────────────────────── 주간 캘린더 ──────────────────────────────── */
//
//// ──────────────────────────────── 주간 캘린더 (정렬 고정 버전) ────────────────────────────────
//@Composable
//private fun WeeklyCalendarBar(
//    selectedDateLabel: String, // "YYYY-MM-DD"
//    onPrevWeek: () -> Unit,
//    onNextWeek: () -> Unit,
//    onSelectDate: (String) -> Unit
//) {
//    val week = remember(selectedDateLabel) { buildWeekFrom(selectedDateLabel) } // 월~일
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
//        listOf("월","화","수","목","금","토","일").forEachIndexed { i, label ->
//            val isTodayDow = i == todayDowIndex
//            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
//                Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                    // 위쪽 파란 점
//                    if (isTodayDow) {
//                        Box(
//                            Modifier
//                                .size(6.dp)
//                                .clip(RoundedCornerShape(50))
//                                .background(Color(0xFF195FCF))
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
//                        color = if (isTodayDow) Color(0xFF195FCF) else Color.Black
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
//    // 2) 날짜 칩 행 (오늘 점 제거, 오늘은 미선택이면 파란 글자 + 파란 테두리)
//    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//        // < 지난주
//        Box(
//            modifier = Modifier
//                .width(arrowSlotWidth)
//                .clickable(
//                    indication = null,
//                    interactionSource = remember { MutableInteractionSource() }
//                ) { onPrevWeek() },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "<",
//                fontSize = 18.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.SemiBold,
//                color = Color(0xFF195FCF)
//            )
//        }
//
//        week.forEach { day ->
//            val dayNum = day.takeLast(2).removePrefix("0")
//            val isSelected = day == selectedDateLabel
//            val isToday = day == today
//
//            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
//                Surface(
//                    shape = RoundedCornerShape(12.dp),
//                    color = if (isSelected) Color(0xFF195FCF) else Color(0xFFF2F4F7),
//                    shadowElevation = if (isSelected) 2.dp else 0.dp,
//                    border = if (!isSelected && isToday) BorderStroke(1.5.dp, Color(0xFF195FCF)) else null,
//                    modifier = Modifier
//                        .width(38.dp)
//                        .height(44.dp)
//                        .clickable(
//                            indication = null,
//                            interactionSource = remember { MutableInteractionSource() }
//                        ) { onSelectDate(day) }
//                ) {
//                    Box(contentAlignment = Alignment.Center) {
//                        Text(
//                            text = dayNum,
//                            fontSize = 16.sp,
//                            fontFamily = Pretendard,
//                            fontWeight = FontWeight.Medium,
//                            color = when {
//                                isSelected -> Color.White
//                                isToday -> Color(0xFF195FCF) // 오늘(미선택) 파란 글자
//                                else -> Color.Black
//                            }
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
//                .clickable(
//                    indication = null,
//                    interactionSource = remember { MutableInteractionSource() }
//                ) { onNextWeek() },
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = ">",
//                fontSize = 18.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.SemiBold,
//                color = Color(0xFF195FCF)
//            )
//        }
//    }
//}
//
///* ─────────────────────────────── 지난 본문 카드 ─────────────────────────────── */
//
//@Composable
//private fun OverviewCard(
//    dateLabelForDisplay: String,   // "YYYY.MM.DD"
//    bodyText: String?,
//    onGoStudyClick: () -> Unit,
//    onBodyClick: () -> Unit
//) {
//    Surface(
//        shape = RoundedCornerShape(16.dp),
//        color = Color(0xFFF5F5F5),
//        shadowElevation = 0.dp,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(Modifier.padding(16.dp)) {
//            Text(
//                text = dateLabelForDisplay,
//                fontSize = 14.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                color = Color(0xFF616161)
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            Text(
//                text = bodyText?.takeIf { it.isNotBlank() } ?: "학습한 글감이 없습니다.",
//                fontSize = 16.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                color = Color(0xFF333333),
//                lineHeight = 25.6.sp,   // 16sp × 1.6 = 160%
//                maxLines = 9,
//                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier
//                    .clip(RoundedCornerShape(8.dp))
//                    .clickable(
//                        indication = null,
//                        interactionSource = remember { MutableInteractionSource() }
//                    ) { onBodyClick() }
//                    .padding(4.dp)
//            )
//
//            Spacer(Modifier.height(12.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                Text(
//                    text = "학습하러 가기 >",
//                    fontSize = 12.sp,
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    color = Color(0xFF616161),
//                    modifier = Modifier.clickable(
//                        indication = null,
//                        interactionSource = remember { MutableInteractionSource() }
//                    ) { onGoStudyClick() }
//                )
//            }
//        }
//    }
//}
//
///* ──────────────────────────────── 날짜 유틸 ──────────────────────────────── */
//
//// "YYYY-MM-DD" → 해당 날짜가 포함된 주의 월~일까지 7일
//private fun buildWeekFrom(dateLabel: String): List<String> {
//    val cal = parseCal(dateLabel)
//    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 일(1)~토(7)
//    val offsetToMonday = when (dayOfWeek) {
//        Calendar.SUNDAY -> -6
//        else -> Calendar.MONDAY - dayOfWeek
//    }
//    cal.add(Calendar.DAY_OF_MONTH, offsetToMonday)
//    return (0 until 7).map {
//        val label = toDateLabel(cal)
//        cal.add(Calendar.DAY_OF_MONTH, 1)
//        label
//    }
//}
//
//// 날짜 이동
//private fun shiftDays(dateLabel: String, deltaDays: Int): String {
//    val cal = parseCal(dateLabel)
//    cal.add(Calendar.DAY_OF_MONTH, deltaDays)
//    return toDateLabel(cal)
//}
//
//// Parser
//private fun parseCal(dateLabel: String): Calendar {
//    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
//    return Calendar.getInstance().apply {
//        time = sdf.parse(dateLabel) ?: Date()
//        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
//    }
//}
//
//// Calendar → "YYYY-MM-DD"
//private fun toDateLabel(cal: Calendar): String {
//    val y = cal.get(Calendar.YEAR)
//    val m = cal.get(Calendar.MONTH) + 1
//    val d = cal.get(Calendar.DAY_OF_MONTH)
//    return "%04d-%02d-%02d".format(y, m, d)
//}
//
///* ─────────────────────────────── 프리뷰 ─────────────────────────────── */
//
//@Preview(showBackground = true, widthDp = 390)
//@Composable
//private fun Preview_StudyWeeklyScreen() {
//    val today = toDateLabel(Calendar.getInstance())
//
//    StudyWeeklyScreen(
//        initialDateLabel = today,
//        onDateChange = { /* 프리뷰: 서버 호출 생략 */ },
//        bodyText = "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀가 있듯이 …”",
//        onBackClick = {},
//        onGoStudyClick = {}
//    )
//}