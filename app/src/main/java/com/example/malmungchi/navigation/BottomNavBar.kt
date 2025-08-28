package com.example.malmungchi.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.malmungchi.feature.study.Pretendard // ← 기존 모듈의 Pretendard 재사용

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
@Composable
fun BottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    capsuleHeight: Dp = 64.dp,      // ✅ 요청: 높이 2배
    cornerRadius: Dp = 28.dp
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val items = listOf(
        BottomNavItem.Study,
        BottomNavItem.Quiz,
        BottomNavItem.Ai,
        BottomNavItem.Friend,
        BottomNavItem.MyPage
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .height(capsuleHeight),
        shape = RoundedCornerShape(cornerRadius),   // ✅ 네 모서리 모두 둥글게(캡슐)
        color = Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEach { item ->
                val selected = currentDestination
                    ?.hierarchy
                    ?.any { it.route == item.route } == true

                val labelColor = if (selected) BrandBlue else GrayNull
                val iconRes = if (selected) item.selectedIcon else item.unselectedIcon

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable {
                            if (!selected) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // ✅ 아이콘: tint 사용 X, 리소스 교체만
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp)  // 높이 커진 만큼 아이콘도 키움
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        fontSize = 10.sp,               // 가독성 확보
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        color = labelColor,             // ✅ 선택/비선택 색 분리
                        fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif
                    )
                }
            }
        }
    }
}
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