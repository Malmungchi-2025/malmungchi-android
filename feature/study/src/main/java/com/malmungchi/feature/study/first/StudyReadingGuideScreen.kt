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
    // Dim 배경
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC4D4D4D))   // 80% 어둡게 처리
            .clickable(enabled = false) {}   // 뒤 클릭 막기
    ) {

        // 닫기 버튼
        Image(
            painter = painterResource(id = R.drawable.ic_guide_exit),
            contentDescription = "닫기",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 24.dp)
                .size(40.dp)
                .clickable { onDismiss() }
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-64).dp, y = (240).dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {

            // 펜 1: 처음 진입
            GuideRowItem(
                icon = R.drawable.ic_study_black_pen_guide,
                text = "처음 진입 상태"
            )

            Spacer(Modifier.height(20.dp))

            // 펜 2: 단어 수집 중
            GuideRowItem(
                icon = R.drawable.ic_study_yellow_pen_guide,
                text = "단어 수집 중"
            )

            Spacer(Modifier.height(20.dp))

            // 펜 3: 단어 수집 후
            GuideRowItem(
                icon = R.drawable.ic_study_blue_pen_guide,
                text = "단어 수집 후"
            )

            Spacer(Modifier.height(40.dp))

            // 형광펜 가이드 문구
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
                    lineHeight = 24.sp   // 150%
                )
            }
        }
    }
}

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