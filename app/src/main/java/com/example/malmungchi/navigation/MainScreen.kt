package com.example.malmungchi.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.malmungchi.feature.ai.AiScreen
import com.malmungchi.feature.friend.FriendScreen
import com.malmungchi.feature.quiz.QuizScreen
import com.malmungchi.feature.study.StudyScreen
import com.malmungchi.feature.mypage.MyPageScreen
import androidx.navigation.navigation
import com.malmungchi.feature.study.first.StudyIntroScreen

@Composable
fun MainScreen(
    onStartStudyFlow: () -> Unit // 로그인 이후, "시작하기" 누르면 루트의 study_graph로 진입시키는 콜백
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Study.route, // 로그인 후 기본 탭
            modifier = Modifier.padding(innerPadding)
        ) {
            // 오늘의 학습 (탭 내부 네비게이션)
            navigation(
                route = BottomNavItem.Study.route,
                startDestination = "study/home"
            ) {
                composable("study/home") {
                    // 탭 홈 인트로: "시작하기" 버튼만 동작하면 됨 (자동 진행 X)
                    StudyIntroScreen(
                        onStart = onStartStudyFlow,
                        onNavigateNext = onStartStudyFlow  // ⬅️ 타이머가 호출하는 next를 'study_graph' 진입으로 연결
                    )
                }
            }

            // 퀴즈
            navigation(
                route = BottomNavItem.Quiz.route,
                startDestination = "quiz/home"
            ) {
                composable("quiz/home") { QuizScreen() }
            }

            // AI
            navigation(
                route = BottomNavItem.Ai.route,
                startDestination = "ai/home"
            ) {
                composable("ai/home") { AiScreen() }
            }

            // 친구
            navigation(
                route = BottomNavItem.Friend.route,
                startDestination = "friend/home"
            ) {
                composable("friend/home") { FriendScreen() }
            }

            // 마이페이지
            navigation(
                route = BottomNavItem.MyPage.route,
                startDestination = "mypage/home"
            ) {
                composable("mypage/home") { MyPageScreen() }
            }
        }
    }
}

//@Composable
//fun MainScreen(
//    onStartStudyFlow: () -> Unit // ← 추가
//) {
//    val navController = rememberNavController()
//
//    Scaffold(
//        bottomBar = { BottomNavBar(navController) }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = BottomNavItem.Study.route, // 로그인 후 기본 선택 탭
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            // 오늘의 학습 그래프(탭 내부)
//            navigation(
//                route = BottomNavItem.Study.route,
//                startDestination = "study/home"
//            ) {
//                composable("study/home") {
//                    // StudyScreen에서 “시작하기” 누르면 루트의 study_graph로 진입
//                    StudyIntroScreen(
//                        onStart = onStartStudyFlow   // ← 전달
//                    )
//                }
//            }
//
//            navigation(route = BottomNavItem.Quiz.route,   startDestination = "quiz/home")   {
//                composable("quiz/home")   { QuizScreen() }
//            }
//            navigation(route = BottomNavItem.Ai.route,     startDestination = "ai/home")     {
//                composable("ai/home")     { AiScreen() }
//            }
//            navigation(route = BottomNavItem.Friend.route, startDestination = "friend/home") {
//                composable("friend/home") { FriendScreen() }
//            }
//            navigation(route = BottomNavItem.MyPage.route, startDestination = "mypage/home") {
//                composable("mypage/home") { MyPageScreen() }
//            }
//        }
//    }
//}
//@Composable
//fun MainScreen() {
//    val navController = rememberNavController()
//
//    Scaffold(
//        bottomBar = { BottomNavBar(navController) }
//    ) { innerPadding ->
//        NavHost(
//            navController = navController,
//            startDestination = BottomNavItem.Study.route, // ← 왼쪽 첫 탭을 시작으로
//            modifier = Modifier.padding(innerPadding)
//        ) {
//            composable(BottomNavItem.Study.route)  { StudyScreen() }
//            composable(BottomNavItem.Quiz.route)   { QuizScreen() }
//            composable(BottomNavItem.Ai.route)     { AiScreen() }
//            composable(BottomNavItem.Friend.route) { FriendScreen() }
//            composable(BottomNavItem.MyPage.route) { MyPageScreen() }
//        }
//    }
//}
