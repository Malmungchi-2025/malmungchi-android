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

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Ai.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Ai.route) { AiScreen() }
            composable(BottomNavItem.Friend.route) { FriendScreen() }
            composable(BottomNavItem.Quiz.route) { QuizScreen() }
            composable(BottomNavItem.Study.route) { StudyScreen() }
            composable(BottomNavItem.MyPage.route) { MyPageScreen() }
        }
    }
}
