package com.malmungchi.feature.study.first

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R

/**
 * 오늘의 학습 - 기능 가이드
 * StudyReadingScreen 진입 전에 1번만 보여주는 오버레이 화면
 */
@Composable
fun StudyReadingGuideScreen(
    onDismiss: () -> Unit
) {
    // DIM 오버레이 전체
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC4D4D4D))
            .clickable { onDismiss() }   // 전체 화면 클릭 → 닫힘
    ) {

        // X 버튼 그대로 유지
        Image(
            painter = painterResource(id = R.drawable.ic_guide_exit),
            contentDescription = "닫기",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 24.dp)
                .size(40.dp)
                .clickable { onDismiss() }
        )

        // 여기만 수정됨: offset 제거 + bottom 패딩 적용 ⭐⭐
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)   // 화면 아래 기준
                .padding(start = 20.dp, bottom = 80.dp), // 디자이너 레이아웃과 최대한 동일한 비율
            horizontalAlignment = Alignment.Start
        ) {

            GuideRowItem(
                icon = R.drawable.ic_study_black_pen_guide,
                text = "처음 진입 상태"
            )

            Spacer(Modifier.height(20.dp))

            GuideRowItem(
                icon = R.drawable.ic_study_yellow_pen_guide,
                text = "단어 수집 중"
            )

            Spacer(Modifier.height(20.dp))

            GuideRowItem(
                icon = R.drawable.ic_study_blue_pen_guide,
                text = "단어 수집 후"
            )

            Spacer(Modifier.height(40.dp))

            Row(
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_study_black_pen_highlight_guide),
                    contentDescription = "가이드 펜",
                    modifier = Modifier.size(44.dp)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = "형광펜을 활성화시켜\n모르는 단어를 수집해보세요.",
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCCFF00),
                    lineHeight = 24.sp
                )
            }
        }
    }
}
//@Composable
//fun StudyReadingGuideScreen(
//    onDismiss: () -> Unit
//) {
//    // Dim 배경
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xCC4D4D4D))   // 전체 DIM
//            .clickable { onDismiss() }       // 💥 화면 아무 곳이나 눌러도 닫힘
//    ) {
//
//        // 닫기 버튼
//        Image(
//            painter = painterResource(id = R.drawable.ic_guide_exit),
//            contentDescription = "닫기",
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(top = 40.dp, end = 24.dp)
//                .size(40.dp)
//                .clickable { onDismiss() }
//        )
//
//        Column(
//            modifier = Modifier
//                .align(Alignment.Center)
//                .offset(x = (-64).dp, y = (240).dp)  // ⭐ 디자이너 배치 그대로 유지
//                .padding(horizontal = 20.dp),
//            horizontalAlignment = Alignment.Start
//        ) {
//
//            // 펜 1: 처음 진입
//            GuideRowItem(
//                icon = R.drawable.ic_study_black_pen_guide,
//                text = "처음 진입 상태"
//            )
//
//            Spacer(Modifier.height(20.dp))
//
//            // 펜 2
//            GuideRowItem(
//                icon = R.drawable.ic_study_yellow_pen_guide,
//                text = "단어 수집 중"
//            )
//
//            Spacer(Modifier.height(20.dp))
//
//            // 펜 3
//            GuideRowItem(
//                icon = R.drawable.ic_study_blue_pen_guide,
//                text = "단어 수집 후"
//            )
//
//            Spacer(Modifier.height(40.dp))
//
//            Row(verticalAlignment = Alignment.Top) {
//
//                Image(
//                    painter = painterResource(id = R.drawable.ic_study_black_pen_highlight_guide),
//                    contentDescription = "가이드 펜",
//                    modifier = Modifier.size(44.dp)
//                )
//
//                Spacer(Modifier.width(12.dp))
//
//                Text(
//                    text = "형광펜을 활성화시켜\n모르는 단어를 수집해보세요.",
//                    fontFamily = Pretendard,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color(0xFFCCFF00),
//                    lineHeight = 24.sp
//                )
//            }
//        }
//    }
//}
@Composable
private fun GuideRowItem(
    icon: Int,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = text,
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCCFF00)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewStudyReadingGuideScreen() {
    StudyReadingGuideScreen(onDismiss = {})
}