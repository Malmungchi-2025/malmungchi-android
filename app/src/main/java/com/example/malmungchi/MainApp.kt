package com.example.malmungchi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navigation
import com.example.malmungchi.navigation.MainScreen
import com.malmungchi.feature.login.LoginScreen
import com.malmungchi.feature.study.first.StudyIntroScreen
import com.malmungchi.feature.study.first.StudyReadingScreen
import com.malmungchi.feature.study.first.StudyAppendixScreen
import com.malmungchi.feature.study.first.StudyAppendixListScreen
import com.malmungchi.feature.study.StudyReadingViewModel
import com.malmungchi.feature.study.second.StudySecondIntroScreen
import com.malmungchi.feature.study.second.StudySecondScreen
import com.malmungchi.feature.study.third.StudyCompleteScreen
import com.malmungchi.feature.study.third.StudyResultQuestion
import com.malmungchi.feature.study.third.StudyThirdIntroScreen
import com.malmungchi.feature.study.third.StudyThirdResultScreen
import com.malmungchi.feature.study.third.StudyThirdResultScreenWrapper
import com.malmungchi.feature.study.third.StudyThirdScreen
import androidx.compose.material3.Text
import com.example.malmungchi.navigation.BottomNavItem
import com.malmungchi.feature.login.EmailLoginScreen

@Composable
fun MainApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = "login") {
    // 0) 인트로
        composable("login") {
            LoginScreen(
                onEmailLogin = {   // “로그인하기” 텍스트 → 이메일 로그인 화면
                    navController.navigate("email_login") { launchSingleTop = true }
                },
                onSignUp = {       // 회원가입 버튼 → 회원가입 화면(미구현 시 TODO)
                    // navController.navigate("sign_up") { launchSingleTop = true }
                },
                onKakao = { /* ... */ },
                onNaver = { /* ... */ },
                onGoogle = { /* ... */ }
            )
        }

    //  신규: 이메일/비번 폼 화면
    composable("email_login") {
        EmailLoginScreen(
            onBack = { navController.popBackStack() },
            onLoginSuccess = {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }  // 인트로 스택 제거
                    launchSingleTop = true
                }
            }
        )
    }


    // 1) 메인(하단바) - 로그인 후 진입
        composable("main") {
            MainScreen(
                onStartStudyFlow = {
                    // 메인에서 "시작" 누르면 학습 플로우 그래프로 진입
                    navController.navigate("study_graph") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // 2) 오늘의 학습 플로우 그래프 (Intro → Reading → … → Complete)
        navigation(
            route = "study_graph",
            startDestination = "study_intro"
        ) {
            // 1️⃣ Intro → Reading
            composable("study_intro") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
                StudyIntroScreen(
                    onStart = {                       // ← 이름 통일
                        navController.navigate("study_reading") {
                            popUpTo("study_intro") { inclusive = true }
                        }
                    }
                )
            }


            // 2️⃣ StudyReadingScreen → Appendix
            composable("study_reading") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
                StudyReadingScreen(
                    viewModel = vm,
                    onNextClick = {
                        navController.navigate("appendix") {
                            popUpTo("study_reading") { inclusive = true }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // 3️⃣ Appendix → 3초 후 AppendixList
            composable("appendix") {
                StudyAppendixScreen(
                    onNavigateNext = {
                        navController.navigate("appendix_list") {
                            popUpTo("appendix") { inclusive = true }
                        }
                    }
                )
            }

            // 4️⃣ AppendixList → 2단계 Intro 로 이동하도록 수정
            composable("appendix_list") {
                val viewModel = hiltViewModel<StudyReadingViewModel>()
                StudyAppendixListScreen(
                    token = "dummy_token",
                    studyId = 1,
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigate("study_reading") {
                            popUpTo("appendix_list") { inclusive = true }
                        }
                    },
                    onNavigateNext = {
                        navController.navigate("study_second_intro") { // ✅ study_second_intro로 변경
                            popUpTo("appendix_list") { inclusive = true }
                        }
                    }
                )
            }

            // ✅ 5️⃣ 2단계 Intro → 2단계 본문 화면
            composable("study_second_intro") {
                StudySecondIntroScreen(
                    onNavigateNext = {
                        navController.navigate("study_second") {
                            popUpTo("study_second_intro") { inclusive = true }
                        }
                    }
                )
            }

            // ✅ 6️⃣ 2단계 본문 화면 (StudySecondScreen 연결)
            // StudySecondScreen → onNextClick 에서 다음으로 이동
            composable("study_second") {
                val viewModel = hiltViewModel<StudyReadingViewModel>()
                StudySecondScreen(
                    token = "dummy_token",
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNextClick = {
                        navController.navigate("study_third_intro") {
                            popUpTo("study_second") { inclusive = true }
                        }
                    }
                )
            }

            // ✅ 7️⃣ StudyThirdIntroScreen → 3초 후 StudyThirdScreen
            composable("study_third_intro") {
                StudyThirdIntroScreen(
                    onNavigateNext = {
                        navController.navigate("study_third") {
                            popUpTo("study_third_intro") { inclusive = true }
                        }
                    }
                )
            }
            composable("study_third") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val token = "dummy_token"
                val id = vm.studyId.collectAsState().value
                val text = vm.quote.collectAsState().value

                // 🔁 혹시 이전 단계에서 못 채웠다면 여기서라도 한 번 확보
                LaunchedEffect(id, text) {
                    if (id == null || text.isBlank()) {
                        vm.fetchTodayQuote(token)   // 최소한 진행 가능하도록 방어
                    }
                }

                if (id == null || text.isBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    StudyThirdScreen(
                        token = token,
                        studyId = id,
                        text = text,
                        viewModel = vm,
                        onBackClick = { navController.popBackStack() },
                        onNextClick = {
                            navController.navigate("study_third_result/$id") {
                                // ✅ popUpTo는 “고정 라우트”만!
                                popUpTo("study_third") { inclusive = true }
                            }
                        }
                    )
                }
            }

            composable("study_third_result/{studyId}") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val id = backStackEntry.arguments?.getString("studyId")?.toIntOrNull()
                if (id == null) {
                    // 안전 처리
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("잘못된 접근입니다.")
                    }
                } else {
                    StudyThirdResultScreenWrapper(
                        token = "dummy_token",
                        studyId = id,
                        viewModel = vm,
                        onBackClick = { navController.popBackStack() },
                        onFinishClick = {
                            navController.navigate("study_third_complete") {
                                popUpTo("study_third") { inclusive = true }
                            }
                        }
                    )
                }
            }

//            // ✅ 8️⃣ StudyThirdScreen (3단계 본문)
//            composable("study_third") {
//                val viewModel = hiltViewModel<StudyReadingViewModel>()
//                val token = "dummy_token" // TODO 실제 토큰
//                val studyIdState = viewModel.studyId.collectAsState()
//                val quoteState = viewModel.quote.collectAsState()
//
//                val id = studyIdState.value
//                val text = quoteState.value
//
//                if (id == null || text.isBlank()) {
//                    Box(
//                        Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) { CircularProgressIndicator() }
//                } else {
//                    StudyThirdScreen(
//                        token = token,
//                        studyId = id,
//                        text = text,
//                        viewModel = viewModel,
//                        onBackClick = { navController.popBackStack() },
//                        onNextClick = {
//                            navController.navigate("study_third_result/$id") {
//                                popUpTo("study_third") { inclusive = true }
//                            }
//                        }
//                    )
//                }
//            }
//
//            // ✅ 결과 화면: studyId 파라미터 받음
//            composable("study_third_result/{studyId}") { backStackEntry ->
//                val id = backStackEntry.arguments?.getString("studyId")!!.toInt()
//                val viewModel = hiltViewModel<StudyReadingViewModel>()
//                val token = "dummy_token" // TODO 실제 토큰
//
//                StudyThirdResultScreenWrapper(
//                    token = token,
//                    studyId = id,
//                    viewModel = viewModel,
//                    onBackClick = { navController.popBackStack() },
//                    onFinishClick = {
//                        navController.navigate("study_third_complete") {
//                            popUpTo("study_third") { inclusive = true }
//                        }
//                    }
//                )
//            }

            // ✅ 🔟 StudyCompleteScreen (완료 화면)
            composable("study_third_complete") {
                StudyCompleteScreen(
                    onNextClick = {
                        // 완료 후 메인으로
                        navController.navigate("main") {
                            popUpTo("study_third_complete") { inclusive = true }
                        }
                    }
                )
            }
        }


    }
}

//// 7️⃣ 메인 화면
//composable("main") { MainScreen() }
//
//// 8️⃣ 로그인 화면
//composable("login") {
//    LoginScreen(
//        onLoginSuccess = {
//            navController.navigate("main") {
//                popUpTo("login") { inclusive = true }
//            }
//        }
//    )
//}
//import androidx.compose.runtime.Composable
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.malmungchi.navigation.MainScreen
//import com.malmungchi.feature.login.LoginScreen
//import com.malmungchi.feature.study.first.StudyIntroScreen
//import com.malmungchi.feature.study.first.StudyReadingScreen
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.malmungchi.feature.study.StudyReadingViewModel
//import com.malmungchi.feature.study.first.StudyAppendixScreen
//import com.malmungchi.feature.study.first.StudyAppendixListScreen
//import com.malmungchi.feature.study.second.StudySecondScreen
//
//@Composable
//fun MainApp() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "study_intro"
//    ) {
//        // 1️⃣ Intro → 3초 후 ReadingScreen
//        composable("study_intro") {
//            StudyIntroScreen(
//                onNavigateNext = {
//                    navController.navigate("study_reading") {
//                        popUpTo("study_intro") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 2️⃣ StudyReadingScreen → Appendix
//        composable("study_reading") {
//            val viewModel = hiltViewModel<StudyReadingViewModel>()
//            StudyReadingScreen(
//                viewModel = viewModel,
//                onNextClick = {
//                    navController.navigate("appendix") {   // ✅ 수정됨
//                        popUpTo("study_reading") { inclusive = true }
//                    }
//                },
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        // 3️⃣ Appendix → 3초 후 AppendixList
//        composable("appendix") {
//            StudyAppendixScreen(
//                onNavigateNext = {
//                    navController.navigate("appendix_list") {   // ✅ 수정됨
//                        popUpTo("appendix") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // MainApp.kt
//        composable("appendix_list") {
//            val viewModel = hiltViewModel<StudyReadingViewModel>()
//            StudyAppendixListScreen(
//                token = "dummy_token",
//                studyId = 1,
//                viewModel = viewModel,
//                onBackClick = {
//                    navController.navigate("study_reading") {   // ✅ 명시적으로 이동
//                        popUpTo("appendix_list") { inclusive = true }
//                    }
//                },
//                onNavigateNext = {
//                    navController.navigate("main") {
//                        popUpTo("appendix_list") { inclusive = true }
//                    }
//                }
//            )
//        }
//        composable("study_second_intro") {
//            StudySecondIntroScreen(
//                onNavigateNext = {
//                    navController.navigate("study_second") {   // ✅ 2단계 본문 화면으로 이동
//                        popUpTo("study_second_intro") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 5️⃣ 메인 화면
//        composable("main") { MainScreen() }
//
//        // 6️⃣ 로그인 화면
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = {
//                    navController.navigate("main") {
//                        popUpTo("login") { inclusive = true }
//                    }
//                }
//            )
//        }
//    }
//}



//@Composable
//fun MainApp() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "study_intro"
//    ) {
//        // 1️⃣ Intro → 3초 후 ReadingScreen
//        composable("study_intro") {
//            StudyIntroScreen(
//                onNavigateNext = {
//                    navController.navigate("study_reading") {
//                        popUpTo("study_intro") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 2️⃣ StudyReadingScreen (버튼 클릭 → main)
//        composable("study_reading") {
//            val viewModel = hiltViewModel<StudyReadingViewModel>()
//            StudyReadingScreen(
//                viewModel = viewModel,
//                onNextClick = {
//                    navController.navigate("main") {
//                        popUpTo("study_reading") { inclusive = true }
//                    }
//                },
//                onBackClick = {
//                    navController.popBackStack()
//                }
//            )
//        }
//
//        // 3️⃣ Appendix (3초 후 → main)
//        composable("appendix") {
//            StudyAppendixScreen(
//                onNavigateNext = {
//                    navController.navigate("main") {   // ✅ appendix → main
//                        popUpTo("appendix") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 4️⃣ AppendixList (단어 리스트 → main)
//        composable("appendix_list") {
//            val viewModel = hiltViewModel<StudyReadingViewModel>()
//            // ✅ API는 나중에 연결, 지금은 목업으로만 호출
//            StudyAppendixListScreen(
//                token = "dummy_token",
//                studyId = 1,  // 일단 더미 ID
//                viewModel = viewModel,
//                onBackClick = { navController.popBackStack() },
//                onNavigateNext = {
//                    navController.navigate("main") {
//                        popUpTo("appendix_list") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 5️⃣ 메인 화면
//        composable("main") { MainScreen() }
//
//        // 6️⃣ 로그인 화면 (현재 미사용)
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = {
//                    navController.navigate("main") {
//                        popUpTo("login") { inclusive = true }
//                    }
//                }
//            )
//        }
//    }
//}

//@Composable
//fun MainApp() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "login"   // ✅ 첫 화면은 로그인
//    ) {
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = {
//                    navController.navigate("main") {
//                        popUpTo("login") { inclusive = true } // 로그인 화면 제거
//                    }
//                }
//            )
//        }
//        composable("main") { MainScreen() }  // ✅ 메인 네비게이션 화면
//    }
//}