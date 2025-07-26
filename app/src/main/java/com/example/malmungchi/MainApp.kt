package com.example.malmungchi


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.malmungchi.navigation.MainScreen
import com.malmungchi.feature.login.LoginScreen

@Composable
fun MainApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"   // ✅ 첫 화면은 로그인
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true } // 로그인 화면 제거
                    }
                }
            )
        }
        composable("main") { MainScreen() }  // ✅ 메인 네비게이션 화면
    }
}