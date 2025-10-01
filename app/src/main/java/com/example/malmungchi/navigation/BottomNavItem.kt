package com.example.malmungchi.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.malmungchi.R

//sealed class BottomNavItem(val route: String, val label: String, val iconRes: Int) {
//    object Ai : BottomNavItem("ai", "AI", R.drawable.ic_ai)
//    object Friend : BottomNavItem("friend", "Friends", R.drawable.ic_friend)
//    object Quiz : BottomNavItem("quiz", "Quiz", R.drawable.ic_quiz)
//    object Study : BottomNavItem("study", "Study", R.drawable.ic_study)
//    object MyPage : BottomNavItem("mypage", "MyPage", R.drawable.ic_mypage)
//}

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: Int,
    val unselectedIcon: Int
) {
    //object Study  : BottomNavItem("study",  "오늘의 학습", R.drawable.ic_study,  R.drawable.ic_study_null)
    object Study  : BottomNavItem(
        "study_graph",            // ← "study" → "study_graph" 로 변경
        "오늘의 학습",
        R.drawable.ic_study,
        R.drawable.ic_study_null
    )
    object Quiz   : BottomNavItem("quiz_graph",   "퀴즈뭉치",       R.drawable.ic_quiz,   R.drawable.ic_quiz_null)
    object Ai     : BottomNavItem("ai_graph",     "AI 대화",    R.drawable.ic_ai,     R.drawable.ic_ai_null)


    object Friend : BottomNavItem("friend_graph", "친구",       R.drawable.ic_friend, R.drawable.ic_freind_null) // ← 파일명 오타 주의
    object MyPage : BottomNavItem("mypage", "마이페이지", R.drawable.ic_mypage, R.drawable.ic_mypage_null)
}

/**
 * ✅ 아이콘 Painter를 가져오는 확장 함수 //아이콘은 실제 디자인이 정해지면 수정!
 */
//@Composable
//fun BottomNavItem.iconPainter(): Painter = painterResource(id = this.iconRes)

