package com.malmungchi.feature.study.first

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.model.WordItem
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.StudyReadingViewModel

@Composable
fun StudyAppendixListScreen(
    studyId: Int,
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateNext: () -> Unit = {}
) {
    val words by viewModel.savedWords.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVocabularyList(studyId)
    }

    StudyAppendixListContent(
        words = words,
        onBackClick = onBackClick,
        onNavigateNext = onNavigateNext
    )
}

@Composable
fun StudyAppendixListContent(
    words: List<WordItem>,
    onBackClick: () -> Unit,
    onNavigateNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 120.dp) // ✅ 하단 버튼과 겹치지 않게 여백
        ) {
            // ✅ 헤더 (아이콘 + 가운데 타이틀)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.btn_img_back),
                        contentDescription = "뒤로가기",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "오늘의 학습",
                    fontSize = 20.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f), // ✅ Row 안에서 weight
                    color = Color.Black
                )

                // 왼쪽 24dp 아이콘과 균형 맞추기
                Spacer(Modifier.width(24.dp))
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "학습 진행률",
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(16.dp))

            StepProgressBarAppendix()

            Spacer(Modifier.height(16.dp))

            // 🔹 단어 카드 리스트(남은 높이 채우기)
//            Surface(
//                shape = RoundedCornerShape(12.dp),
//                color = Color(0xFFF9F9F9),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f) // ✅ Column 안에서 남은 공간
//            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(words) { WordCard(it) }
                }

        }

        // 🔹 하단 버튼 (Box 스코프 안, align 사용 가능)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    //.padding(horizontal = 20.dp)
                    .offset(y = (-64).dp) // ✅ 바텀시트 위로 64dp 띄움
                    .fillMaxWidth(),      // ✅ Row 전체 폭 사용
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF195FCF)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF195FCF)),
                    modifier = Modifier
                        .height(42.dp)
                        .weight(1f) // ✅ 좌우 길이 자동 맞춤
                ) {
                    Text(
                        "이전 단계",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard
                    )
                }

                Button(
                    onClick = onNavigateNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF195FCF)
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(42.dp)
                        .weight(1f) // ✅ 좌우 길이 자동 맞춤
                ) {
                    Text(
                        "다음 단계",
                        fontSize = 16.sp,
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun StepProgressBarAppendix(totalSteps: Int = 3) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(14.dp)
                    .background(
                        color = if (index == 0) Color(0xFF195FCF) else Color(0xFFF2F2F2),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
fun WordCard(item: WordItem) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        color = Color(0xFFF7F7F7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                item.word,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
                color = Color(0xFF333333)
            )
            Text(
                ": ${item.meaning}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                color = Color(0xFF333333),
                modifier = Modifier.padding(top = 8.dp)
            )
            if (!item.example.isNullOrEmpty()) {
                Text(
                    "예문) ${item.example}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 800)
@Composable
fun PreviewStudyAppendixListScreen() {
    val dummyWords = listOf(
        WordItem("지정하다", "가리키어 확실하게 정하다.", "모임 장소를 지정하다."),
        WordItem("부여하다", "어떤 자격을 주다.", "추석 전날을 공휴일로 지정하다.")
    )
    StudyAppendixListContent(words = dummyWords, onBackClick = {}, onNavigateNext = {})
}
