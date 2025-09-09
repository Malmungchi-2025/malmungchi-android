package com.malmungchi.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.core.model.VocabularyDto
import com.malmungchi.feature.mypage.R as MyPageR
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

// ===== Color & Dimens =====
private val Blue_195FCF = Color(0xFF195FCF)
private val Bg_EFF4FB = Color(0xFFEFF4FB)
private val Gray_616161 = Color(0xFF616161)
private val ScreenPadding = 20.dp
private val SectionGap = 24.dp
private val CardCorner = 16.dp

// ===== Entry: ViewModel 연동 버전 =====
@Composable
fun MyPageRoute(
    viewModel: MyPageViewModel = hiltViewModel(),
    onClickSettings: () -> Unit = {},
    onClickViewAllWords: () -> Unit = {},
    onClickViewAllBadges: () -> Unit = {},
    onClickViewNicknameTest: () -> Unit = {}
) {
    val ui by viewModel.ui.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    // 최근 단어 5개 인덱스
    val pageCount = ui.recentVocab.size
    var recentIndex by rememberSaveable(pageCount) { mutableStateOf(0) }
    if (recentIndex >= pageCount) recentIndex = (pageCount - 1).coerceAtLeast(0)

    when {
        ui.loading -> Box(
            Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            Text("불러오는 중…")
        }
        ui.error != null -> Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            Text("에러: ${ui.error}")
        }
        else -> {
            MyPageScreen(
                userName = ui.userName,
                levelLabel = ui.levelLabel,          // 0:입문, 1:기초, 2:활용, 3:심화, 그 외:고급
                levelProgress = ui.levelProgress,    // 0..1 (4단계 이상은 1.0)
                onClickSettings = onClickSettings,
                onClickViewAllWords = onClickViewAllWords,
                onClickViewAllBadges = onClickViewAllBadges,
                onClickNicknameTest = { onClickViewNicknameTest() },
                recentItems = ui.recentVocab,
                currentRecentIndex = recentIndex,
                onChangeRecentIndex = { next ->
                    if (pageCount > 0) {
                        recentIndex = next.coerceIn(0, pageCount - 1)
                    }
                }
            )
        }
    }
}

// ===== Public Screen (UI만) =====
@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    userName: String,
    levelLabel: String,
    levelProgress: Float,
    onClickSettings: () -> Unit = {},
    onClickViewAllWords: () -> Unit = {},
    onClickViewAllBadges: () -> Unit = {},
    onClickNicknameTest: () -> Unit = {},
    // 최근 단어 데이터/인덱스
    recentItems: List<VocabularyDto> = emptyList(),
    currentRecentIndex: Int = 0,
    onChangeRecentIndex: (Int) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(36.dp))
        MyPageTopBar(
            title = "마이페이지",
            onClickSettings = onClickSettings
        )

        Spacer(Modifier.height(32.dp))
        ProfileBlock(
            userName = userName,
            questionLabel = "치치의 어휘/문해력은?",
            profileIconRes = MyPageR.drawable.ic_mypage_icon,
            onClickQuestion = { onClickNicknameTest() }
        )

        Spacer(Modifier.height(20.dp))
        LevelBlock(
            userName = userName,
            levelLabel = levelLabel,
            progress = levelProgress
        )

        // ===== 단어 수집함 =====
        Spacer(Modifier.height(SectionGap))
        SectionHeader(title = "단어 수집함", action = "모두보기", onAction = onClickViewAllWords)
        Spacer(Modifier.height(12.dp))
        WordCollectionCard(
            items = recentItems,
            index = currentRecentIndex,
            //onPrev = { if (recentItems.isNotEmpty()) onChangeRecentIndex(currentRecentIndex - 1) },
            //onNext = { if (recentItems.isNotEmpty()) onChangeRecentIndex(currentRecentIndex + 1) },
            onClick = onClickViewAllWords,
            onSelectIndex = { tapped -> onChangeRecentIndex(tapped) } // ← 추가

        )

        // ===== 배지 수집함 =====
        Spacer(Modifier.height(SectionGap))
        SectionHeader(title = "배지 수집함", action = "모두보기", onAction = onClickViewAllBadges)
        Spacer(Modifier.height(12.dp))
        BadgeCollectionCard()
        Spacer(Modifier.height(24.dp))
    }
}

// ===== Components =====
@Composable
private fun MyPageTopBar(
    title: String,
    onClickSettings: () -> Unit
) {
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
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = onClickSettings) {
                Icon(
                    painter = painterResource(id = MyPageR.drawable.ic_setting),
                    contentDescription = "설정"
                )
            }
        }
    }
}

// ==== Profile ====
private val AVATAR_SIZE = 80.dp
private val AVATAR_TO_TEXT_GAP = 12.dp
private val TOP_PADDING = (-4).dp
private val BOTTOM_PADDING = 2.dp
private val BUBBLE_H_PADDING = 16.dp
private val BUBBLE_V_PADDING = 10.dp
private val BUBBLE_CORNER = RoundedCornerShape(999.dp)
private val NAME_TEXT_SIZE = 16.sp
private val NAME_TEXT_WEIGHT = FontWeight.SemiBold
private val BUBBLE_TEXT_SIZE = 12.sp
private val BUBBLE_TEXT_WEIGHT = FontWeight.Medium

@Composable
private fun ProfileBlock(
    userName: String,
    questionLabel: String,
    profileIconRes: Int,
    onClickQuestion: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AVATAR_SIZE),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = profileIconRes),
            contentDescription = "프로필",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(AVATAR_SIZE)
        )

        Spacer(Modifier.width(AVATAR_TO_TEXT_GAP))

        Column(
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
                .fillMaxHeight(),

        ) {
            Row(
                modifier = Modifier
                    .padding(top = 0.dp)
                    .offset(y = TOP_PADDING),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = userName, // ← 서버 me() 값 바인딩
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = NAME_TEXT_WEIGHT,
                        fontSize = NAME_TEXT_SIZE,
                        color = Color(0xFF262626)
                    )
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = MyPageR.drawable.ic_pencil),
                    contentDescription = "이름 수정",
                    tint = Gray_616161,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .padding(bottom = BOTTOM_PADDING)
                    .background(Bg_EFF4FB, shape = BUBBLE_CORNER)
                    .clickable { onClickQuestion() }
                    .padding(horizontal = BUBBLE_H_PADDING, vertical = BUBBLE_V_PADDING)
            ) {
                Text(
                    text = questionLabel,
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = BUBBLE_TEXT_SIZE,
                        fontWeight = BUBBLE_TEXT_WEIGHT,
                        color = Color(0xFF262626)
                    )
                )
            }
        }
    }
}

// ==== Level ====
@Composable
private fun LevelBlock(
    userName: String,
    levelLabel: String,
    progress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "${userName}님의 수준은 ‘${levelLabel}’", // ← 이름/단계 문구
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = MyPageR.drawable.ic_question),
            contentDescription = "레벨 설명",
            tint = Color(0xFF262626),
            modifier = Modifier.size(20.dp)
        )
    }

    Spacer(Modifier.height(8.dp))
    ProgressBar(
        progress = progress.coerceIn(0f, 1f),
        height = 12.dp,
        trackColor = Bg_EFF4FB,
        progressColor = Blue_195FCF
    )
}

@Composable
private fun ProgressBar(
    progress: Float,
    height: Dp,
    trackColor: Color,
    progressColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(999.dp))
                .background(progressColor)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = action,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Gray_616161
            )
        )
    }
}

// ==== 최근 단어 카드 + 인디케이터 (API 연동) ====
@Composable
private fun WordCollectionCard(
    items: List<VocabularyDto> = emptyList(),
    index: Int = 0,                       // 부모에서 내려주는 현재 인덱스
    onClick: () -> Unit = {},
    onSelectIndex: (Int) -> Unit = {}     // 부모로 페이지 변경 전달
) {
    val pageCount = items.size.coerceAtLeast(1)   // 빈 리스트 대비
    val pagerState = rememberPagerState(
        initialPage = index.coerceIn(0, pageCount - 1),
        pageCount = { pageCount }
    )
    val scope = rememberCoroutineScope()

    // ✅ 부모에서 index가 바뀌면 Pager를 그 위치로 스크롤 (동기화)
    LaunchedEffect(index, pageCount) {
        val target = index.coerceIn(0, pageCount - 1)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)  // 순간이동; 애니메이션 원하면 animateScrollToPage
        }
    }

    // ✅ Pager 쪽에서 스와이프(스크롤)로 페이지가 바뀌면 부모에 알려주기
    LaunchedEffect(pagerState.currentPage, pageCount) {
        val cp = pagerState.currentPage.coerceIn(0, pageCount - 1)
        if (cp != index) onSelectIndex(cp)
    }

    // ===== 카드 영역: 페이지별로 다른 단어 보여주기 =====
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(CardCorner),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .heightIn(min = 120.dp) // 높이 살짝 보장(선택)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = items.getOrNull(page)
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = item?.word ?: "최근 단어가 없어요",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item?.meaning ?: "단어를 저장하면 여기에서 바로 볼 수 있어요.",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                val ex = item?.example
                if (!ex.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "예문) $ex",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Gray_616161
                        )
                    )
                }
            }
        }
    }

    // ===== 도트 인디케이터: 탭해서 해당 페이지로 이동 =====
    if (items.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        DotsIndicator(
            count = pageCount,
            selectedIndex = pagerState.currentPage.coerceIn(0, pageCount - 1),
            selectedColor = Blue_195FCF,
            unselectedColor = Color(0xFFE0E0E0),
            onSelect = { tapped ->
                scope.launch {
                    pagerState.animateScrollToPage(tapped)
                    // animateScrollToPage가 끝나면 LaunchedEffect가 onSelectIndex(tapped) 호출해 부모와 동기화합니다.
                }
            }
        )
    }
}
//@Composable
//private fun WordCollectionCard(
//    items: List<VocabularyDto> = emptyList(),
//    index: Int = 0,
//    onClick: () -> Unit = {},
//    onSelectIndex: (Int) -> Unit = {}
//) {
//    val item = items.getOrNull(index)
//
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        shape = RoundedCornerShape(CardCorner),
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//    ) {
//        Column(modifier = Modifier.padding(20.dp)) {
//            Text(
//                text = item?.word ?: "최근 단어가 없어요",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 16.sp,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            )
//
//            Spacer(Modifier.height(8.dp))
//            Text(
//                text = item?.meaning ?: "단어를 저장하면 여기에서 바로 볼 수 있어요.",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 14.sp,
//                    lineHeight = 22.sp,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            )
//
//            Spacer(Modifier.height(12.dp))
//            val ex = item?.example
//            if (!ex.isNullOrBlank()) {
//                Text(
//                    text = "예문) $ex",
//                    style = TextStyle(
//                        fontFamily = Pretendard,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 12.sp,
//                        lineHeight = 18.sp,
//                        color = Gray_616161
//                    )
//                )
//            }
//        }
//    }
//
//    // 카드 아래 도트 인디케이터
//    if (items.isNotEmpty()) {
//        Spacer(Modifier.height(12.dp))
//        DotsIndicator(
//            count = items.size,       // 최근 단어 개수
//            selectedIndex = index,    // 현재 보고 있는 인덱스
//            selectedColor = Blue_195FCF,
//            unselectedColor = Color(0xFFE0E0E0),
//            onSelect = onSelectIndex
//        )
//    }
//}

@Composable
private fun DotsIndicator(
    count: Int,
    selectedIndex: Int,
    selectedColor: Color,
    unselectedColor: Color,
    onSelect: (Int) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) { idx ->
            Box(
                modifier = Modifier
                    .size(if (idx == selectedIndex) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (idx == selectedIndex) selectedColor else unselectedColor)
                    .clickable { onSelect(idx) }    // ← 탭해서 페이지 이동
            )
            if (idx != count - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

// ===== 배지 수집함 (더미) =====
@Composable
private fun BadgeCollectionCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 13.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = MyPageR.drawable.img_empty),
                            contentDescription = "배지",
                            modifier = Modifier.size(88.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "일주일 출석",
                            style = TextStyle(
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}



//package com.malmungchi.feature.mypage
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//
//import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.layout.FlowColumnScopeInstance.weight
//
////import androidx.compose.foundation.layout.ColumnScopeInstance.weight
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.tooling.preview.Preview
//
//// Pretendard
//import com.malmungchi.core.designsystem.Pretendard
//
//// 리소스(아이콘/이미지) – 프로젝트 리소스에 맞춰 이름만 연결
//
//import com.malmungchi.feature.mypage.R as MyPageR
//
//// ===== Color & Dimens (피그마 스펙) =====
//private val Blue_195FCF = Color(0xFF195FCF)
//private val Bg_EFF4FB = Color(0xFFEFF4FB)
//private val Gray_616161 = Color(0xFF616161)
//private val Card_E0E0E0 = Color(0xFFE0E0E0)
//
//private val ScreenPadding = 20.dp
//private val SectionGap = 24.dp
//private val TitleBodyGap = 8.dp
//private val CardCorner = 16.dp
//
//// ===== Public Screen Composable =====
//@Composable
//fun MyPageScreen(
//    modifier: Modifier = Modifier,
//    userName: String,
//    levelLabel: String,              // 예: "심화"
//    levelProgress: Float,            // 0f..1f
//    onClickSettings: () -> Unit = {},
//    onClickViewAllWords: () -> Unit = {},   // ✅ 타입 지정 + 기본값은 비어있는 람다
//    onClickViewAllBadges: () -> Unit = {}
//) {
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(horizontal = ScreenPadding)
//    ) {
//        Spacer(Modifier.height(12.dp))
//        MyPageTopBar(
//            title = "마이페이지",
//            onClickSettings = onClickSettings
//        )
//
//        Spacer(Modifier.height(20.dp))
//        ProfileBlock(
//            userName = userName,
//            questionLabel = "지치의 어휘/문해력은?",
//            profileIconRes = MyPageR.drawable.ic_mypage_icon
//        )
//
//        Spacer(Modifier.height(16.dp))
//        LevelBlock(
//            userName = userName,
//            levelLabel = levelLabel,
//            progress = levelProgress
//        )
//
//        // ===== 단어 수집함 =====
//        Spacer(Modifier.height(SectionGap))
//        SectionHeader(title = "단어 수집함", action = "모두보기", onAction = onClickViewAllWords)
//        Spacer(Modifier.height(12.dp))
//        //WordCollectionCard()       // 카드 + 아래 도트
//        WordCollectionCard(onClick = onClickViewAllWords)
//
//        // ===== 배지 수집함 =====
//        Spacer(Modifier.height(SectionGap))
//        SectionHeader(title = "배지 수집함", action = "모두보기", onAction = onClickViewAllBadges)
//        Spacer(Modifier.height(12.dp))
//        BadgeCollectionCard()      // 하나의 큰 카드 안에 배지들
//        Spacer(Modifier.height(12.dp))
//        //BadgeRow()
//        Spacer(Modifier.height(24.dp))
//    }
//}
//
//// ===== UI Pieces =====
//
//@Composable
//private fun MyPageTopBar(
//    title: String,
//    onClickSettings: () -> Unit
//) {
//    Box(
//        modifier = Modifier.fillMaxWidth(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = title,
//            modifier = Modifier.fillMaxWidth(),
//            textAlign = TextAlign.Center,
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//        )
//        // 우측 톱니 아이콘 (타이틀과 수평 정렬)
//        Row(
//            modifier = Modifier
//                .align(Alignment.CenterEnd)
//        ) {
//            IconButton(onClick = onClickSettings) {
//                Icon(
//                    painter = painterResource(id = MyPageR.drawable.ic_setting), // ic_setting 준비
//                    contentDescription = "설정"
//                )
//            }
//        }
//    }
//}
//// ==== 조절용 상수(여기 숫자만 바꾸면 돼요) ====
//private val AVATAR_SIZE = 80.dp          // 아바타(아이콘) 크기
//private val AVATAR_TO_TEXT_GAP = 12.dp    // 아바타 ↔ 텍스트 열 사이 간격
//private val TOP_PADDING = -4.dp            // 이름을 아바타 상단에서 얼마나 띄울지
//private val BOTTOM_PADDING = 2.dp         // 말풍선을 아바타 하단에서 얼마나 띄울지
//
//private val BUBBLE_H_PADDING = 16.dp      // 말풍선 가로 패딩(배경 너비)
//private val BUBBLE_V_PADDING = 10.dp      // 말풍선 세로 패딩(배경 높이)
//private val BUBBLE_CORNER = RoundedCornerShape(999.dp)
//
//private val NAME_TEXT_SIZE = 16.sp
//private val NAME_TEXT_WEIGHT = FontWeight.SemiBold
//private val BUBBLE_TEXT_SIZE = 12.sp
//private val BUBBLE_TEXT_WEIGHT = FontWeight.Medium
//
//@Composable
//private fun ProfileBlock(
//    userName: String,
//    questionLabel: String,
//    profileIconRes: Int
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(AVATAR_SIZE),                     // ✅ Row 높이를 아바타와 동일하게 고정
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // 아바타 (배경 없음)
//        Image(
//            painter = painterResource(id = profileIconRes),
//            contentDescription = "프로필",
//            contentScale = ContentScale.Fit,
//            modifier = Modifier.size(AVATAR_SIZE)
//        )
//
//        Spacer(Modifier.width(AVATAR_TO_TEXT_GAP))
//
//        // 오른쪽 컬럼을 아바타 높이에 맞춰 꽉 채움
//        Column(
//            modifier = Modifier
//                .weight(1f)
//                .fillMaxHeight(),                      // ✅ 세로 공간을 전부 차지
//        ) {
//            // ===== 상단 고정: 이름+연필 =====
//            Row(
//                modifier = Modifier
//                    .padding(top = 0.dp)
//                    .offset(y = TOP_PADDING),
//                    //.padding(top = TOP_PADDING), // ↔ 상단 여백 조절 지점
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = userName,
//                    style = TextStyle(
//                        fontFamily = Pretendard,
//                        fontWeight = NAME_TEXT_WEIGHT,
//                        fontSize = NAME_TEXT_SIZE,
//                        color = Color(0xFF262626)
//                    )
//                )
//                Spacer(Modifier.width(6.dp))
//                Icon(
//                    painter = painterResource(id = MyPageR.drawable.ic_pencil),
//                    contentDescription = "이름 수정",
//                    tint = Gray_616161,
//                    modifier = Modifier.size(14.dp)
//                )
//            }
//
//            // 가운데 공간을 모두 먹어 하단으로 밀어내기
//            Spacer(Modifier.weight(1f))
//
//            // ===== 하단 고정: 말풍선 =====
//            Box(
//                modifier = Modifier
//                    .padding(bottom = BOTTOM_PADDING)      // ↔ 하단 여백 조절 지점
//                    .background(Bg_EFF4FB, shape = BUBBLE_CORNER)
//                    .padding(
//                        horizontal = BUBBLE_H_PADDING,     // ← 말풍선 가로 크기 조절
//                        vertical = BUBBLE_V_PADDING        // ← 말풍선 세로 크기 조절
//                    )
//            ) {
//                Text(
//                    text = questionLabel,
//                    style = TextStyle(
//                        fontFamily = Pretendard,
//                        fontSize = BUBBLE_TEXT_SIZE,
//                        fontWeight = BUBBLE_TEXT_WEIGHT,
//                        color = Color(0xFF262626)
//                    )
//                )
//            }
//        }
//    }
//}
//
//
//// ====== 프로그레스바 : 12.dp 로 두껍게 ======
//@Composable
//private fun LevelBlock(
//    userName: String,
//    levelLabel: String,
//    progress: Float
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = "${userName}님의 수준은 ‘${levelLabel}’",
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black // 불투명 검정
//            ),
//            modifier = Modifier.weight(1f) // 텍스트가 남는 공간 다 차지
//        )
//
//        Icon(
//            painter = painterResource(id = MyPageR.drawable.ic_question), // ✅ 준비해둔 ic_question
//            contentDescription = "레벨 설명",
//            tint = Color(0xFF262626), // 동일한 색상 적용
//            modifier = Modifier.size(20.dp)
//        )
//    }
//
//    Spacer(Modifier.height(8.dp))
//    ProgressBar(
//        progress = progress.coerceIn(0f, 1f),
//        height = 12.dp, // ← 기존보다 두껍게
//        trackColor = Bg_EFF4FB,
//        progressColor = Blue_195FCF
//    )
//}
//
//@Composable
//private fun ProgressBar(
//    progress: Float,
//    height: Dp,
//    trackColor: Color,
//    progressColor: Color
//) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(height)
//            .clip(RoundedCornerShape(999.dp))
//            .background(trackColor)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxHeight()
//                .fillMaxWidth(progress)
//                .clip(RoundedCornerShape(999.dp))
//                .background(progressColor)
//        )
//    }
//}
//
//@Composable
//private fun SectionHeader(
//    title: String,
//    action: String,
//    onAction: () -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = title,
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 18.sp,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//        )
//        Spacer(Modifier.weight(1f))
//        Text(
//            text = action,
//            modifier = Modifier
//                .clip(RoundedCornerShape(8.dp))
//                .clickable(onClick = onAction)
//                .padding(horizontal = 4.dp, vertical = 2.dp),
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                fontSize = 12.sp,
//                color = Gray_616161
//            )
//        )
//    }
//}
//
//// ====== 단어 수집함 : 흰 배경 + 회색 테두리, 예문 추가, 도트는 카드 밖 ======
//// ====== 단어 수집함 : 흰 배경 + 그림자 + 예문 간격 조정 ======
//@Composable
//private fun WordCollectionCard(onClick: () -> Unit = {}) {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color.White), // ✅ 흰 배경
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // ✅ 그림자 효과
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//    ) {
//        Column(modifier = Modifier.padding(20.dp)) {
//            Text(
//                text = "말뭉치",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 16.sp,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            )
//            Spacer(Modifier.height(8.dp))
//            Text(
//                text = "언어 연구를 위해 텍스트를 컴퓨터가 읽을 수 있는 형태로 모아 놓은 언어 자료.",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 14.sp,
//                    lineHeight = 22.sp,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            )
//            // ✅ 본문과 예문 사이 간격 여유
//            Spacer(Modifier.height(12.dp))
//            Text(
//                text = "예문) 어휘력, 문해력을 키우기 위해서는 어휘/문해 이것저것을 배울 수 있는 ‘말뭉치’ 사용이 필수적이다.",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 12.sp,
//                    lineHeight = 18.sp,
//                    color = Gray_616161
//                )
//            )
//        }
//    }
//
//    // 도트는 카드 "밖" 아래에 위치
//    Spacer(Modifier.height(12.dp))
//    DotsIndicator(
//        count = 5,
//        selectedIndex = 1,
//        selectedColor = Blue_195FCF,
//        unselectedColor = Color(0xFFE0E0E0)
//    )
//}
//
//
//@Composable
//private fun DotsIndicator(
//    count: Int,
//    selectedIndex: Int,
//    selectedColor: Color,
//    unselectedColor: Color
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.Center
//    ) {
//        repeat(count) { idx ->
//            Box(
//                modifier = Modifier
//                    .size(if (idx == selectedIndex) 8.dp else 6.dp)
//                    .clip(CircleShape)
//                    .background(if (idx == selectedIndex) selectedColor else unselectedColor)
//            )
//            if (idx != count - 1) Spacer(Modifier.width(8.dp))
//        }
//    }
//}
//
//
//
////@Composable
////private fun BadgeRow() {
////    Row(
////        modifier = Modifier.fillMaxWidth(),
////        horizontalArrangement = Arrangement.spacedBy(12.dp)
////    ) {
////        repeat(4) {
////            BadgeCard(
////                title = "일주일 출석",
////                iconRes = MyPageR.drawable.img_empty,
////                modifier = Modifier.weight(1f)   // ✅ RowScope에서 weight 적용
////            )
////        }
////    }
////}
//
////// modifier 파라미터 추가
////@Composable
////private fun BadgeCard(
////    title: String,
////    iconRes: Int,
////    modifier: Modifier = Modifier
////) {
////    Column(
////        modifier = modifier                             // ✅ 전달 받은 modifier 사용
////            .border(1.dp, Gray_616161, RoundedCornerShape(CardCorner))
////            .clip(RoundedCornerShape(CardCorner))
////            .background(Card_E0E0E0)
////            .padding(vertical = 16.dp),
////        horizontalAlignment = Alignment.CenterHorizontally
////    ) {
////        Image(
////            painter = painterResource(id = iconRes),
////            contentDescription = title,
////            modifier = Modifier.size(56.dp)
////        )
////        Spacer(Modifier.height(12.dp))
////        Text(
////            text = title,
////            textAlign = TextAlign.Center,
////            style = TextStyle(
////                fontFamily = Pretendard,
////                fontWeight = FontWeight.Medium,
////                fontSize = 12.sp,
////                color = MaterialTheme.colorScheme.onBackground
////            )
////        )
////    }
////}
//
//// ====== 배지 수집함 : 하나의 큰 카드(흰 배경, 회색 테두리) 안에 배지 3개 ======
//@Composable
//private fun BadgeCollectionCard() {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        // ① 카드 내부 패딩: 왼쪽만 13dp로 축소(기존 20dp → 13dp)
//        Column(modifier = Modifier.padding(
//            start = 13.dp,
//            end = 20.dp,
//            top = 20.dp,
//            bottom = 20.dp
//        )) {
//            // ② Row 배치: 외곽 여백을 과도하게 키우는 SpaceEvenly → SpaceBetween
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                repeat(3) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                        Image(
//                            painter = painterResource(id = MyPageR.drawable.img_empty),
//                            contentDescription = "배지",
//                            modifier = Modifier.size(88.dp)
//                        )
//                        Spacer(Modifier.height(12.dp))
//                        Text(
//                            text = "일주일 출석",
//                            style = TextStyle(
//                                fontFamily = Pretendard,
//                                fontWeight = FontWeight.Medium,
//                                fontSize = 14.sp,
//                                color = MaterialTheme.colorScheme.onBackground
//                            ),
//                            textAlign = TextAlign.Center
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun MyPageScreenPreview() {
//    // 앱 공용 테마가 있으면 그걸로 교체 (e.g., AppTheme { ... })
//    MaterialTheme {
//        androidx.compose.material3.Surface {
//            MyPageScreen(
//                userName = "김뭉치",
//                levelLabel = "심화",
//                levelProgress = 0.6f
//            )
//        }
//    }
//}