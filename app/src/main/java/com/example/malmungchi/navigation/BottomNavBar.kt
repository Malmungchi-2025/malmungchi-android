package com.example.malmungchi.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.malmungchi.feature.study.Pretendard // ← 기존 모듈의 Pretendard 재사용

@Composable
fun BottomNavBar(navController: NavHostController) {
    // 요구 순서: 오늘의 학습 → 퀴즈 → AI 대화 → 친구 → 마이페이지
    val items = listOf(
        BottomNavItem.Study,
        BottomNavItem.Quiz,
        BottomNavItem.Ai,
        BottomNavItem.Friend,
        BottomNavItem.MyPage
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute == item.route
            val labelColor = if (selected) Color(0xFF195FCF) else Color(0xFFC9CAD4)
            val iconRes = if (selected) item.selectedIcon else item.unselectedIcon

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        color = labelColor,
                        fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif
                    )
                },
                // 아이콘 색은 리소스로 처리하므로 NavigationBarItemDefaults.colors는 라벨만 신경 쓰면 됨
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Unspecified,
                    unselectedIconColor = Color.Unspecified,
                    selectedTextColor = labelColor,
                    unselectedTextColor = labelColor,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
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