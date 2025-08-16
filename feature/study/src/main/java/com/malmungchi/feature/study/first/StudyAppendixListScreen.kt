package com.malmungchi.feature.study.first


import androidx.compose.foundation.background
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
    //token: String,
    studyId: Int,
    viewModel: StudyReadingViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateNext: () -> Unit = {}
) {
    val words by viewModel.savedWords.collectAsState()

    // API 연동
    LaunchedEffect(Unit) {
        viewModel.loadVocabularyList(studyId)
    }

    StudyAppendixListContent(
        words = words,
        onBackClick = onBackClick,
        onNavigateNext = onNavigateNext
    )
}

/**
 * ✅ UI 렌더링 로직 분리 (Preview와 공용)
 */
@Composable
fun StudyAppendixListContent(
    words: List<WordItem>,
    onBackClick: () -> Unit,
    onNavigateNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
                top = 32.dp      // ✅ 위는 32, 나머지는 16
            )
    ) {
        // 🔹 상단 UI (오늘의 학습)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.btn_img_back),
                    contentDescription = "뒤로가기",
                    tint = Color.Unspecified
                )
            }
            Text(
                text = "오늘의 학습",
                fontSize = 20.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
                color = Color.Black
            )
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(24.dp))

        Text("학습 진행률", fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Normal, modifier = Modifier.padding(start = 8.dp))
        Spacer(Modifier.height(16.dp))
        StepProgressBarAppendix()
        Spacer(Modifier.height(24.dp))

        // 🔹 단어 카드 리스트
        Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9F9F9), modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(words) { WordCard(it) }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 하단 버튼
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(
                onClick = onBackClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                modifier = Modifier.height(42.dp).width(160.dp)
            ) {
                Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard)
            }

            Button(
                onClick = onNavigateNext,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(42.dp).width(160.dp)
            ) {
                Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
            }
        }
    }
}

/**
 * ✅ ProgressBar (첫 번째만 파란색)
 */
@Composable
fun StepProgressBarAppendix(totalSteps: Int = 3) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier.weight(1f).height(14.dp).background(
                    color = if (index == 0) Color(0xFF195FCF) else Color(0xFFF2F2F2),
                    shape = RoundedCornerShape(50)
                )
            )
        }
    }
}

/**
 * ✅ 단어 카드
 */
@Composable
fun WordCard(item: WordItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(item.word, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, fontFamily = Pretendard, color = Color(0xFF333333))
            Text(": ${item.meaning}", fontSize = 14.sp, fontWeight = FontWeight.Medium, fontFamily = Pretendard, color = Color(0xFF333333), modifier = Modifier.padding(top = 4.dp))
            if (!item.example.isNullOrEmpty()) {
                Text("예문) ${item.example}", fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = Pretendard, color = Color(0xFF616161), modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

/**
 * ✅ Preview (실제 UI와 동일)
 */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewStudyAppendixListScreen() {
    val dummyWords = listOf(
        WordItem("지정하다", "가리키어 확실하게 정하다.", "모임 장소를 지정하다."),
        WordItem("부여하다", "어떤 자격을 주다.", "추석 전날을 공휴일로 지정하다.")
    )
    StudyAppendixListContent(words = dummyWords, onBackClick = {}, onNavigateNext = {})
}

//@Composable
//fun StudyAppendixListScreenPreview(
//    words: List<WordItem>,
//    onBackClick: () -> Unit = {},
//    onNavigateNext: () -> Unit = {}
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(16.dp)
//    ) {
//        /** 🔹 오늘의 학습 (가운데 정렬) */
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            IconButton(onClick = onBackClick) {
//                Icon(
//                    painter = painterResource(id = R.drawable.btn_img_back),
//                    contentDescription = "뒤로가기",
//                    tint = Color.Unspecified
//                )
//            }
//            Text(
//                text = "오늘의 학습",
//                fontSize = 20.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.SemiBold,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.weight(1f),
//                color = Color.Black
//            )
//            Spacer(Modifier.width(48.dp))
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        /** 🔹 학습 진행률 (살짝 오른쪽) */
//        Text(
//            "학습 진행률",
//            fontSize = 16.sp,
//            color = Color.Black,
//            fontWeight = FontWeight.Normal,
//            modifier = Modifier.padding(start = 8.dp)
//        )
//
//        Spacer(Modifier.height(16.dp))
//        StepProgressBarAppendix() // ✅ 수정된 ProgressBar 사용
//        Spacer(Modifier.height(24.dp))
//
//        /** 🔹 단어 카드 리스트 */
//        Surface(
//            shape = RoundedCornerShape(12.dp),
//            color = Color(0xFFF9F9F9),
//            modifier = Modifier.weight(1f)
//        ) {
//            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//                items(words) { WordCard(it) }
//            }
//        }
//
//        Spacer(Modifier.height(16.dp))
//
//        /** 🔹 하단 버튼 */
//        Row(
//            Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            OutlinedButton(
//                onClick = onBackClick,
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("이전 단계", fontSize = 16.sp, fontFamily = Pretendard)
//            }
//
//            Button(
//                onClick = onNavigateNext,
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//                shape = RoundedCornerShape(50),
//                modifier = Modifier.height(42.dp).width(160.dp)
//            ) {
//                Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
//            }
//        }
//    }
//}
//
///** ✅ ProgressBar (첫 번째만 파란색) */
//@Composable
//fun StepProgressBarAppendix(totalSteps: Int = 3) {
//    Row(
//        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        repeat(totalSteps) { index ->
//            Box(
//                modifier = Modifier.weight(1f).height(14.dp).background(
//                    color = if (index == 0) Color(0xFF195FCF) else Color(0xFFF2F2F2),
//                    shape = RoundedCornerShape(50)
//                )
//            )
//        }
//    }
//}
//
///** ✅ 단어 카드 UI (그림자 + 분리감 추가) */
//@Composable
//fun WordCard(item: WordItem) {
//    Surface(
//        shape = RoundedCornerShape(12.dp),
//        shadowElevation = 4.dp, // ✅ 그림자 효과
//        color = Color.White,
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 6.dp)
//    ) {
//        Column(Modifier.padding(12.dp)) {
//            // 🔹 단어 (Pretendard SemiBold, 18sp)
//            Text(
//                text = item.word,
//                fontSize = 18.sp,
//                fontWeight = FontWeight.SemiBold,
//                fontFamily = Pretendard,
//                color = Color(0xFF333333)
//            )
//
//            // 🔹 뜻 (Pretendard Medium, 14sp)
//            Text(
//                text = ": ${item.meaning}",
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Medium,
//                fontFamily = Pretendard,
//                color = Color(0xFF333333),
//                modifier = Modifier.padding(top = 4.dp)
//            )
//
//            // 🔹 예문 (Pretendard Medium, 12sp, color=616161)
//            if (!item.example.isNullOrEmpty()) {
//                Text(
//                    text = "예문) ${item.example}",
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium,
//                    fontFamily = Pretendard,
//                    color = Color(0xFF616161),
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//            }
//        }
//    }
//}
//
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//fun PreviewStudyAppendixListScreen() {
//    val dummyWords = listOf(
//        WordItem("지정하다", "가리키어 확실하게 정하다.", "모임 장소를 지정하다."),
//        WordItem("부여하다", "어떤 자격을 주다.", "추석 전날을 공휴일로 지정하다.")
//    )
//    StudyAppendixListScreenPreview(words = dummyWords)
//}
