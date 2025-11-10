package com.example.malmungchi.navigation


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.feature.study.Pretendard

private val BrandBlue = Color(0xFF195FCF)
private val GrayNull  = Color(0xFFC9CAD4)

/**
 * 캡슐형 Bottom Navigation (프리뷰/실사용 공용)
 * - capsuleHeightDp: 캡슐 전체 높이
 * - contentHeightDp: 내부 콘텐츠(아이콘+라벨) 영역 높이
 * - allRounded: 모든 모서리 둥글게
 * - showPlusMark: 라벨 앞에 '＋' 표시 추가 (선택/비선택 색상 동일 규칙)
 * - forPreview: 프리뷰에서만 navigationBarsPadding 제거 및 바닥 16dp 마진 고정
 */
// 핵심 변경점만 발췌: plus 제거 + 초슬림 사이즈
@Composable
fun CapsuleBottomNav(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
    dense: Boolean = true,
    capsuleHeightDp: Dp = 32.dp,     // ✅ 이전 대비 절반 수준(초슬림)
    contentHeightDp: Dp = 24.dp,     // ✅ 내부 콘텐츠 높이 축소
    cornerRadius: Dp = 28.dp,
    forPreview: Boolean = false
) {
    val baseModifier = if (forPreview) {
        modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    } else {
        modifier.fillMaxWidth().navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
    }

    Surface(
        modifier = baseModifier.height(capsuleHeightDp),
        shape = RoundedCornerShape(cornerRadius), // ✅ 네 모서리 모두 둥글게
        color = Color.White,
        tonalElevation = 2.dp,
        shadowElevation = 8.dp
    ) {
        // ---- 초슬림 커스텀 레이아웃 ----
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val labelColor = if (selected) BrandBlue else GrayNull
                val iconRes = if (selected) item.selectedIcon else item.unselectedIcon

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(contentHeightDp)
                        .clickable { onItemClick(item) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(14.dp) // ✅ 아이콘 더 작게
                    )
                    Spacer(Modifier.height(0.dp))
                    Text(
                        text = item.label,              // ✅ ‘+’ 제거 (텍스트 유지)
                        fontSize = 8.sp,                // ✅ 폰트 축소
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        color = labelColor,
                        fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif
                    )
                }
            }
        }
    }
}

/* --------------------------- Preview --------------------------- */
@Preview(
    name = "Capsule BottomNav - Ultra Compact",
    showBackground = true,
    backgroundColor = 0xFFF0F0F2,
    widthDp = 360, heightDp = 120
)
@Composable
fun BottomNavPreview_Compact() {
    val items = remember {
        listOf(
            BottomNavItem.Study,
            BottomNavItem.Quiz,
            BottomNavItem.Ai,
            BottomNavItem.Friend,
            BottomNavItem.MyPage
        )
    }
    CapsuleBottomNav(
        items = items,
        currentRoute = BottomNavItem.MyPage.route,
        onItemClick = { /* preview */ },
        dense = true,
        capsuleHeightDp = 32.dp,   // ✅ 절반 수준
        contentHeightDp = 24.dp,
        cornerRadius = 28.dp,
        forPreview = true
    )
}



//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.NavigationBarItemDefaults
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.malmungchi.R
//import com.malmungchi.feature.study.Pretendard
//
//
//
//@Composable
//fun RoundedBottomNavPreviewContent() {
//    // 바깥 캡슐(둥근 모양) 컨테이너
//    Surface(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp), // 화면 가장자리와 간격
//        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
//        color = Color.White,              // 바탕(디자인에 맞게 조정)
//        tonalElevation = 2.dp,            // M3 톤높이(살짝 떠보이는 느낌)
//        shadowElevation = 8.dp,           // 그림자 (프리뷰에서 입체감)
//        border = null                     // 필요하면 BorderStroke(1.dp, Color(0xFFE5E7EB))
//    ) {
//        // NavigationBar의 기본 배경은 꺼서 Surface 색/모양이 보이게
//        NavigationBar(
//            containerColor = Color.Transparent,
//            tonalElevation = 0.dp,
//            windowInsets = androidx.compose.foundation.layout.WindowInsets(0),
//        ) {
//            val items = listOf(
//                BottomNavItem.Study,
//                BottomNavItem.Quiz,
//                BottomNavItem.Ai,
//                BottomNavItem.Friend,
//                BottomNavItem.MyPage
//            )
//
//            items.forEachIndexed { index, item ->
//                val selected = index == 0
//                val labelColor = if (selected) Color(0xFF195FCF) else Color(0xFFC9CAD4)
//                val iconRes = if (selected) item.selectedIcon else item.unselectedIcon
//
//                NavigationBarItem(
//                    selected = selected,
//                    onClick = { /* 프리뷰 전용 */ },
//                    icon = {
//                        Image(
//                            painter = painterResource(id = iconRes),
//                            contentDescription = item.label
//                        )
//                    },
//                    label = {
//                        Text(
//                            text = item.label,
//                            fontSize = 10.sp,              // 9~10sp 권장
//                            maxLines = 1,
//                            overflow = TextOverflow.Clip,
//                            color = labelColor,
//                            fontFamily = Pretendard as? FontFamily ?: FontFamily.SansSerif
//                        )
//                    },
//                    colors = NavigationBarItemDefaults.colors(
//                        selectedIconColor = Color.Unspecified,
//                        unselectedIconColor = Color.Unspecified,
//                        selectedTextColor = labelColor,
//                        unselectedTextColor = labelColor,
//                        indicatorColor = Color.Transparent
//                    )
//                )
//            }
//        }
//    }
//}
//
//@Preview(
//    name = "Rounded BottomNav Preview",
//    showBackground = true,
//    backgroundColor = 0xFFF7F8FA,
//    widthDp = 360, heightDp = 120
//)
//@Composable
//fun PreviewRoundedBottomNav() {
//    RoundedBottomNavPreviewContent()
//}



//@Composable
//fun BottomNavPreviewContent() {
//    NavigationBar {
//        val items = listOf(
//            BottomNavItem.Study,
//            BottomNavItem.Quiz,
//            BottomNavItem.Ai,
//            BottomNavItem.Friend,
//            BottomNavItem.MyPage
//        )
//
//        items.forEachIndexed { index, item ->
//            NavigationBarItem(
//                selected = index == 0, // 첫 번째만 선택된 상태
//                onClick = { /* 프리뷰용, 동작 없음 */ },
//                icon = {
//                    Image(
//                        painter = painterResource(
//                            id = if (index == 0) item.selectedIcon else item.unselectedIcon
//                        ),
//                        contentDescription = item.label
//                    )
//                },
//                label = { Text(item.label) }
//            )
//        }
//    }
//}
//
//@Preview(
//    name = "BottomNav Preview",
//    showBackground = true,
//    backgroundColor = 0xFFF7F8FA,
//    widthDp = 360, heightDp = 80
//)
//@Composable
//fun PreviewBottomNav() {
//    BottomNavPreviewContent()
//}