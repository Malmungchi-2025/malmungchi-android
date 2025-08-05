package com.example.malmungchi

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.malmungchi.navigation.MainScreen
import com.malmungchi.feature.login.LoginScreen
import com.malmungchi.feature.study.first.StudyIntroScreen
import com.malmungchi.feature.study.first.StudyReadingScreen
import com.malmungchi.feature.study.StudyReadingViewModel
import androidx.hilt.navigation.compose.hiltViewModel

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

        // 2️⃣ StudyReadingScreen (버튼 클릭 → main)
        composable("study_reading") {
            val viewModel = hiltViewModel<StudyReadingViewModel>()
            StudyReadingScreen(
                viewModel = viewModel,
                onNextClick = {
                    navController.navigate("main") {
                        popUpTo("study_reading") { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 3️⃣ 로그인 화면 (현재 미사용)
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // 4️⃣ 메인 화면
        composable("main") { MainScreen() }
    }
}

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