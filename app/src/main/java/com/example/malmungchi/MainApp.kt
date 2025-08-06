package com.example.malmungchi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.malmungchi.navigation.MainScreen
import com.malmungchi.feature.login.LoginScreen
import com.malmungchi.feature.study.first.StudyIntroScreen
import com.malmungchi.feature.study.first.StudyReadingScreen
import com.malmungchi.feature.study.first.StudyAppendixScreen
import com.malmungchi.feature.study.first.StudyAppendixListScreen
import com.malmungchi.feature.study.StudyReadingViewModel
import com.malmungchi.feature.study.second.StudySecondIntroScreen   // ✅ 추가
import com.malmungchi.feature.study.second.StudySecondScreen       // ✅ 추가

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "study_intro"
    ) {
        // 1️⃣ Intro → 3초 후 ReadingScreen
        composable("study_intro") {
            StudyIntroScreen(
                onNavigateNext = {
                    navController.navigate("study_reading") {
                        popUpTo("study_intro") { inclusive = true }
                    }
                }
            )
        }

        // 2️⃣ StudyReadingScreen → Appendix
        composable("study_reading") {
            val viewModel = hiltViewModel<StudyReadingViewModel>()
            StudyReadingScreen(
                viewModel = viewModel,
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
        composable("study_second") {
            val viewModel = hiltViewModel<StudyReadingViewModel>()
            StudySecondScreen(
                token = "dummy_token", // 🔹 실제 토큰으로 교체 필요
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onNextClick = {
                    navController.navigate("main") {
                        popUpTo("study_second") { inclusive = true }
                    }
                }
            )
        }

        // 7️⃣ 메인 화면
        composable("main") { MainScreen() }

        // 8️⃣ 로그인 화면
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
    }
}
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

@Composable
fun StudySecondIntroScreen(onNavigateNext: () -> Unit) {

}

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