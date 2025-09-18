package com.example.malmungchi.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.malmungchi.feature.study.Pretendard // ← 기존 모듈의 Pretendard 재사용
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph


private val BrandBlue = Color(0xFF195FCF)
private val GrayNull  = Color(0xFFC9CAD4)

/**
 * 실제 바텀 네비게이션(초슬림 캡슐)
 * - 캡슐 전체 높이: 기본 32.dp (필요 시 28~36 사이 미세조정)
 * - 아이콘 14.dp / 라벨 8.sp
 * - 선택: 파랑, 비선택: 회색
 * - 모서리 전부 둥글게
 */
/** 캡슐형 바텀 네비게이션 (실사용) */

/** 캡슐형 바텀 네비게이션 (실사용) */

//탭 누를 때 없는 라우트로 이동해서 죽는 문제 방지
private fun NavController.hasRoute(route: String): Boolean =
    runCatching { graph.findNode(route) != null }.getOrDefault(false)

// 루트 그래프의 "실제" 시작 목적지 id 찾기
private fun NavGraph.findStartDestinationId(): Int {
    var dest: NavDestination = this
    while (dest is NavGraph) {
        dest = dest.findNode(dest.startDestinationId)
            ?: error("Start destination not found in $dest")
    }
    return dest.id
}

@Composable
fun BottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    capsuleHeight: Dp = 64.dp,
    cornerRadius: Dp = 28.dp
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val dest = backStackEntry?.destination

    // ★ 루트 라우트로 고정 (BottomNavItem.*.route 도 이 값들로 맞춰두면 베스트)
    val StudyRoot  = "study_graph"
    val QuizRoot   = "quiz_graph"
    val AiRoot     = "ai_graph"
//    val AiRoot     = "ai"
    val FriendRoot = "friend"
    val MyPageRoot = "mypage"

    val items = listOf(
        BottomNavItem.Study,  // route = StudyRoot
        BottomNavItem.Quiz,   // route = QuizRoot
        BottomNavItem.Ai,     // route = AiRoot
        BottomNavItem.Friend, // route = FriendRoot
        BottomNavItem.MyPage  // route = MyPageRoot
    )

    // 현재 목적지가 어느 탭에 속하는지 판정
    fun isSelected(itemRoute: String): Boolean {
        val route = dest?.route ?: return false
        return when (itemRoute) {
            StudyRoot  -> dest.hierarchy.any { it.route == "study_graph" } ||
                    route.startsWith("study_") || route.startsWith("past_study")
            QuizRoot   -> dest.hierarchy.any { it.route == "quiz_graph" } ||
                    route.startsWith("quiz_")
//            AiRoot     -> route == "ai"
            AiRoot     -> dest.hierarchy.any { it.route == "ai_graph" } || // ← 추가
                    route == "ai" || route.startsWith("ai_")          // (안전망)
            FriendRoot -> route == "friend"
            MyPageRoot -> route == "mypage" ||
                    route.startsWith("nickname_") ||
                    route == "settings" ||
                    route == "remind_settings" ||
                    route == "word_collection"
            else -> false
        }
    }

    // 실제 이동(항상 루트 그래프 대상)
    fun navigateTo(itemRoute: String) {
        if (!navController.hasRoute(itemRoute)) return
        navController.navigate(itemRoute) {
            // ★ 루트의 안정된 기준으로 popUpTo.
            //   루트 startDestination(onboarding/splash 등)까지 날리면 UX가 튀니,
            //   보통 메인 허브인 "study_graph"를 기준으로 두면 무난.
            popUpTo("study_graph") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(Color.White)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(capsuleHeight),
            shape = RectangleShape,
            color = Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp)
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                items.forEach { item ->
                    val selected = isSelected(item.route)
                    val labelColor = if (selected) BrandBlue else GrayNull
                    val iconRes = if (selected) item.selectedIcon else item.unselectedIcon

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                if (!selected) navigateTo(item.route)
                            },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = iconRes),
                            contentDescription = item.label,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip,
                            color = labelColor,
                            fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

//@Composable
//fun BottomNavBar(
//    navController: NavHostController,
//    modifier: Modifier = Modifier,
//    capsuleHeight: Dp = 64.dp,
//    cornerRadius: Dp = 28.dp
//) {
//
//    val backStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = backStackEntry?.destination
//
//    val items = listOf(
//        BottomNavItem.Study,
//        BottomNavItem.Quiz,
//        BottomNavItem.Ai,
//        BottomNavItem.Friend,
//        BottomNavItem.MyPage
//    )
//
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//            //.navigationBarsPadding()    // 인셋을 먼저 더하고
//            .navigationBarsPadding()   // ✅ 시스템 네비게이션바 만큼 여백 확보 (좌/우/하)
//            //.padding(bottom = 8.dp)    // ✅ 살짝 더 띄워서 손가락/그림자 공간 확보
//            .background(Color.White)    // 그 면적 전체에 흰 배경을 칠한다
//    ) {
//
//    Surface(
//        modifier = modifier
//            .fillMaxWidth()
//            //.navigationBarsPadding()
//            //.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
//            .height(capsuleHeight),
//        shape = RectangleShape, // ✅ 네모
//        //shape = RoundedCornerShape(cornerRadius),
//        color = Color.White,
//        tonalElevation = 2.dp,
//        shadowElevation = 8.dp
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 14.dp)
//                .background(Color.White),    // ✅ 내부도 보강,
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            items.forEach { item ->
//                val selected = currentDestination
//                    ?.hierarchy
//                    ?.any { it.route == item.route } == true
//
//                val labelColor = if (selected) BrandBlue else GrayNull
//                val iconRes = if (selected) item.selectedIcon else item.unselectedIcon
//
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxHeight()
//                        .clickable {
//                            if (!selected) {
//                                val host = navController as NavHostController
//                                if (host.hasRoute(item.route)) {          // ✅ 라우트 존재할 때만 이동
////                                    navController.navigate(item.route) {
////                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
////                                        launchSingleTop = true
////                                        restoreState = true
////                                    }
//                                    navController.navigate(item.route) {
//                                        val startId = navController.graph.findStartDestinationId() // ★ 핵심
//                                        popUpTo(startId) { saveState = true }
//                                        launchSingleTop = true
//                                        restoreState = true
//                                    }
//                                } else {
//                                    // TODO: 스낵바/토스트로 "준비 중" 안내하고 종료 (크래시 방지)
//                                }
//                            }
//                        },
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    // 아이콘: 선택/비선택 리소스 교체 방식
//                    Image(
//                        painter = painterResource(id = iconRes),
//                        contentDescription = item.label,
//                        modifier = Modifier.size(20.dp)
//                    )
//                    Spacer(Modifier.height(2.dp))
//                    Text(
//                        text = item.label,
//                        fontSize = 12.sp,            // ✅ 요청: 글자 크기 12
//                        maxLines = 1,                // ✅ 한 줄 고정
//                        softWrap = false,            // ✅ 줄바꿈 비허용
//                        overflow = TextOverflow.Clip,// ✅ 잘라도 말줄임표 없이 자르기
//                        color = labelColor,
//                        fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif,
//                        fontWeight = FontWeight.Medium    // ★ 미디엄
//                    )
//                }
//            }
//        }
//    }}
//}

/* ───────────── 프리뷰 ───────────── */

@Preview(showBackground = true, backgroundColor = 0xFFF2F4F7)
@Composable
private fun BottomNavBarPreview() {
    val navController = rememberNavController()
    Box(Modifier.fillMaxWidth()) {
        BottomNavBar(navController = navController)
    }
}
//@Composable
//fun BottomNavBar(
//    navController: NavHostController,
//    modifier: Modifier = Modifier,
//    capsuleHeight: Dp = 64.dp,      // ✅ 요청: 높이 2배
//    cornerRadius: Dp = 28.dp
//) {
//    val backStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = backStackEntry?.destination
//
//    val items = listOf(
//        BottomNavItem.Study,
//        BottomNavItem.Quiz,
//        BottomNavItem.Ai,
//        BottomNavItem.Friend,
//        BottomNavItem.MyPage
//    )
//
//    Surface(
//        modifier = modifier
//            .fillMaxWidth()
//            .navigationBarsPadding()
//            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
//            .height(capsuleHeight),
//        shape = RoundedCornerShape(cornerRadius),   // ✅ 네 모서리 모두 둥글게(캡슐)
//        color = Color.White,
//        tonalElevation = 2.dp,
//        shadowElevation = 8.dp
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 14.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            items.forEach { item ->
//                val selected = currentDestination
//                    ?.hierarchy
//                    ?.any { it.route == item.route } == true
//
//                val labelColor = if (selected) BrandBlue else GrayNull
//                val iconRes = if (selected) item.selectedIcon else item.unselectedIcon
//
//                Column(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxHeight()
//                        .clickable {
//                            if (!selected) {
//                                navController.navigate(item.route) {
//                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            }
//                        },
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.Center
//                ) {
//                    // ✅ 아이콘: tint 사용 X, 리소스 교체만
//                    Image(
//                        painter = painterResource(id = iconRes),
//                        contentDescription = item.label,
//                        modifier = Modifier.size(20.dp)  // 높이 커진 만큼 아이콘도 키움
//                    )
//                    Spacer(Modifier.height(2.dp))
//                    Text(
//                        text = item.label,
//                        fontSize = 10.sp,               // 가독성 확보
//                        maxLines = 1,
//                        overflow = TextOverflow.Clip,
//                        color = labelColor,             // ✅ 선택/비선택 색 분리
//                        fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif
//                    )
//                }
//            }
//        }
//    }
//}
//@Composable
//fun BottomNavBar(navController: NavHostController) {
//    // 요구 순서: 오늘의 학습 → 퀴즈 → AI 대화 → 친구 → 마이페이지
//    val items = listOf(
//        BottomNavItem.Study,
//        BottomNavItem.Quiz,
//        BottomNavItem.Ai,
//        BottomNavItem.Friend,
//        BottomNavItem.MyPage
//    )
//
//    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
//
//    NavigationBar {
//        items.forEach { item ->
//            val selected = currentRoute == item.route
//            val labelColor = if (selected) Color(0xFF195FCF) else Color(0xFFC9CAD4)
//            val iconRes = if (selected) item.selectedIcon else item.unselectedIcon
//
//            NavigationBarItem(
//                selected = selected,
//                onClick = {
//                    if (!selected) {
//                        navController.navigate(item.route) {
//                            popUpTo(navController.graph.startDestinationId) { saveState = true }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                },
//                icon = {
//                    Image(
//                        painter = painterResource(id = iconRes),
//                        contentDescription = item.label
//                    )
//                },
//                label = {
//                    Text(
//                        text = item.label,
//                        fontSize = 9.sp, // 글자 크기 줄이기
//                        maxLines = 1,     // 한 줄만 허용
//                        overflow = TextOverflow.Clip, // 줄바꿈 방지
//                        color = labelColor,
//                        fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif
//                    )
//                },
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Color.Unspecified,
//                    unselectedIconColor = Color.Unspecified,
//                    selectedTextColor = labelColor,
//                    unselectedTextColor = labelColor,
//                    indicatorColor = Color.Transparent
//                )
//            )
//        }
//    }
//}

//@Composable
//fun BottomNavBar(navController: NavHostController) {
//    // 요구 순서: 오늘의 학습 → 퀴즈 → AI 대화 → 친구 → 마이페이지
//    val items = listOf(
//        BottomNavItem.Study,
//        BottomNavItem.Quiz,
//        BottomNavItem.Ai,
//        BottomNavItem.Friend,
//        BottomNavItem.MyPage
//    )
//
//    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
//
//    NavigationBar {
//        items.forEach { item ->
//            val selected = currentRoute == item.route
//            val labelColor = if (selected) Color(0xFF195FCF) else Color(0xFFC9CAD4)
//            val iconRes = if (selected) item.selectedIcon else item.unselectedIcon
//
//            NavigationBarItem(
//                selected = selected,
//                onClick = {
//                    if (!selected) {
//                        navController.navigate(item.route) {
//                            popUpTo(navController.graph.startDestinationId) { saveState = true }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                },
//                icon = {
//                    Image(
//                        painter = painterResource(id = iconRes),
//                        contentDescription = item.label
//                    )
//                },
//                label = {
//                    Text(
//                        text = item.label,
//                        color = labelColor,
//                        fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif
//                    )
//                },
//                // 아이콘 색은 리소스로 처리하므로 NavigationBarItemDefaults.colors는 라벨만 신경 쓰면 됨
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = Color.Unspecified,
//                    unselectedIconColor = Color.Unspecified,
//                    selectedTextColor = labelColor,
//                    unselectedTextColor = labelColor,
//                    indicatorColor = Color.Transparent
//                )
//            )
//        }
//    }
//}
//@Composable
//fun BottomNavBar(navController: NavHostController) {
//    val items = listOf(
//        BottomNavItem.Ai,
//        BottomNavItem.Friend,
//        BottomNavItem.Quiz,
//        BottomNavItem.Study,
//        BottomNavItem.MyPage
//    )
//
//    NavigationBar {
//        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
//        items.forEach { item ->
//            NavigationBarItem(
//                icon = { Image(painter = item.iconPainter(), contentDescription = item.label) },
//                label = { Text(item.label) },
//                selected = currentRoute == item.route,
//                onClick = {
//                    if (currentRoute != item.route) {
//                        navController.navigate(item.route) {
//                            popUpTo(navController.graph.startDestinationId) { saveState = true }
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                }
//            )
//        }
//    }
//}