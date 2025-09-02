package com.example.malmungchi.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.malmungchi.feature.study.StudyReadingViewModel
import com.malmungchi.feature.study.first.StudyIntroScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import com.malmungchi.feature.study.intro.StudyWeeklyScreen


@Composable
fun MainScreen(
    onStartStudyFlow: () -> Unit, // 로그인 이후, "시작하기" 누르면 루트의 study_graph로 진입시키는 콜백
    initialTab: String? = null
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
            // ✅ 오늘의 학습 (탭 내부 네비게이션)
            navigation(
                route = BottomNavItem.Study.route,
                startDestination = "study/weekly"   // <-- 변경: 주간 허브로 시작
            ) {
                composable("study/weekly") {
                    val vm: StudyReadingViewModel = hiltViewModel()

                    val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    val body by vm.quote.collectAsState()
                    // ✅ 주간 학습일 세트 수집
                    val studiedDates by vm.studiedDates.collectAsState(initial = emptySet())

                    // ✅ 진입 시 오늘이 포함된 주 학습일 미리 로드
                    LaunchedEffect(today) {
                        vm.refreshStudiedDatesForWeek(LocalDate.parse(today))
                    }

                    StudyWeeklyScreen(
                        initialDateLabel = today,
                        onDateChange = { label ->
                            runCatching { LocalDate.parse(label) }.onSuccess { picked ->
                                // 본문 로드
                                vm.fetchPastStudyByDate(picked)
                                // ✅ 주간 학습일도 갱신
                                vm.refreshStudiedDatesForWeek(picked)
                            }
                        },
                        bodyText = body,
                        onBackClick = { /* 탭 루트라 noop */ },
                        onGoStudyClick = { onStartStudyFlow() },
                        onOpenPastStudy = { /* 필요 시 라우팅 확장 */ },

                        // ✅ 여기 추가! 정확히 일치 비교
                        hasStudy = { day -> studiedDates.contains(day) }
                    )
                }
//                composable("study/weekly") {
//                    // 탭 안에서도 프리뷰/날짜 조회가 필요하면 VM 사용
//                    val vm: StudyReadingViewModel = hiltViewModel()
//                    val today = LocalDate.now()
//                        .format(DateTimeFormatter.ISO_DATE)
//                    val body = vm.quote.collectAsState().value
//
//                    StudyWeeklyScreen(
//                        initialDateLabel = today,
//                        onDateChange = { label ->
//                            runCatching { LocalDate.parse(label) }
//                                .onSuccess { vm.fetchPastStudyByDate(it) }
//                        },
//                        bodyText = body,
//                        onBackClick = { /* 탭 루트라 보통 noop */ },
//                        onGoStudyClick = {
//                            // “학습하러 가기 >” → 루트 NavHost의 study_graph 진입
//                            onStartStudyFlow()
//                        },
//                        onOpenPastStudy = { /* 필요시 루트로 진입 후 해당 화면 열도록 확장 */ }
//                    )
//                }
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
                composable("mypage/home") {
                    MyPageScreen(
                        userName = "...",
                        levelLabel = "...",
                        levelProgress = 0.6f,
                        onClickViewAllWords = { navController.navigate("word_collection") }  // ✅ 여기서 호출
                    )
                }
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
