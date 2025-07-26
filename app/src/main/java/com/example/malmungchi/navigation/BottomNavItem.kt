package com.example.malmungchi.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.malmungchi.R

sealed class BottomNavItem(val route: String, val label: String, val iconRes: Int) {
    object Ai : BottomNavItem("ai", "AI", R.drawable.ic_ai)
    object Friend : BottomNavItem("friend", "Friends", R.drawable.ic_friend)
    object Quiz : BottomNavItem("quiz", "Quiz", R.drawable.ic_quiz)
    object Study : BottomNavItem("study", "Study", R.drawable.ic_study)
    object MyPage : BottomNavItem("mypage", "MyPage", R.drawable.ic_mypage)
}

/**
 * ✅ 아이콘 Painter를 가져오는 확장 함수 //아이콘은 실제 디자인이 정해지면 수정!
 */
@Composable
fun BottomNavItem.iconPainter(): Painter = painterResource(id = this.iconRes)

