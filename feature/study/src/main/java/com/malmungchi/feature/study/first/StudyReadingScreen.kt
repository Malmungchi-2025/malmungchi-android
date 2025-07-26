package com.malmungchi.feature.study.first

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import com.malmungchi.feature.study.StudyReadingViewModel

@Composable
fun StudyReadingScreen(
    viewModel: StudyReadingViewModel,
    totalSteps: Int = 3,
    currentStep: Int = 1,
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    val content by viewModel.content
    val saveResult by viewModel.saveResult

    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedWord by remember { mutableStateOf("") }
    var selectedDefinition by remember { mutableStateOf("") }
    var selectedExample by remember { mutableStateOf("") }

    val penStates = listOf(
        R.drawable.img_pen_black,
        R.drawable.img_pen_yellow,
        R.drawable.img_pen_blue
    )
    var currentPenIndex by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // 🔙 뒤로가기 + 제목
            Box(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.btn_img_back),
                    contentDescription = "뒤로가기",
                    modifier = Modifier.align(Alignment.CenterStart).size(40.dp).clickable { onBackClick() }
                )
                Text(
                    text = "오늘의 학습",
                    fontSize = 20.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF333333),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text("학습 진행률", fontSize = 16.sp, fontFamily = Pretendard, fontWeight = FontWeight.Medium, color = Color(0xFF333333))
            Spacer(Modifier.height(12.dp))

            StepProgressBar(totalSteps, currentStep)
            Spacer(Modifier.height(20.dp))

            // 🧾 본문
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF9F9F9), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = content,
                        fontSize = 14.sp,
                        fontFamily = Pretendard,
                        color = Color(0xFF333333),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ✅ 하단 버튼
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = penStates[currentPenIndex]),
                    contentDescription = "펜",
                    modifier = Modifier.size(64.dp).clickable {
                        currentPenIndex = (currentPenIndex + 1) % penStates.size
                        when (currentPenIndex) {
                            1 -> {
                                // 노란펜 → 단어 선택 시 BottomSheet 오픈
                                selectedWord = "작성"
                                selectedDefinition = "문서나 글 따위를 씀"
                                selectedExample = "보고서를 작성하여 제출하세요."
                                showBottomSheet = true
                            }
                            2 -> println("🔵 파란펜 → 서버 단어 하이라이트")
                            else -> println("⚫ 검정펜 → 강조 제거")
                        }
                    }
                )

                Button(
                    onClick = { onNextClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 10.dp),
                    modifier = Modifier.height(42.dp).width(160.dp)
                ) {
                    Text("다음 단계", fontSize = 16.sp, fontFamily = Pretendard, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // ✅ BottomSheet 표시
    if (showBottomSheet) {
        WordCollectBottomSheet(
            word = selectedWord,
            definition = selectedDefinition,
            example = selectedExample,
            onDismiss = { showBottomSheet = false },
            onSaveClick = {
                viewModel.saveWord(selectedWord, selectedDefinition, selectedExample)
                showBottomSheet = false
            }
        )
    }
}

@Composable
fun StepProgressBar(totalSteps: Int = 3, currentStep: Int = 1) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier.weight(1f).height(12.dp).background(
                    color = if (index == currentStep - 1) Color(0xFF195FCF) else Color(0xFFF2F2F2),
                    shape = RoundedCornerShape(50)
                )
            )
        }
    }
}

/** ✅ Preview 전용 Wrapper (ViewModel 없이 contentText 미리보기) */
@Composable
fun StudyReadingScreenPreviewWrapper(contentText: String) {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Text(contentText, Modifier.align(Alignment.Center), fontSize = 16.sp, fontFamily = Pretendard)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewStudyReadingScreen() {
    StudyReadingScreenPreviewWrapper(
        contentText = "“빛을 보기 위해 눈이 있고, 소리를 듣기 위해 귀가 있으며, 너희들은 시간을 느끼기 위해 가슴을 갖고 있다...”"
    )
}