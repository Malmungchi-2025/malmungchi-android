package com.malmungchi.feature.quiz

// ===== Imports =====
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.malmungchi.core.designsystem.Pretendard

/* ---------- 팔레트 ---------- */
private val BrandBlue = Color(0xFF195FCF)
private val BgBlue    = Color(0xFFEFF4FB)
private val CorrectFill = Color(0xFFD1DFF5) // ✅ 정답 내부색
private val WrongRed    = Color(0xFFFF0000) // ✅ 오답 테두리
private val WrongFill   = Color(0xFFFFCCCC) // ✅ 오답 내부색
private val ChipGray  = Color(0xFFF7F7F7)
private val ChipSel   = Color(0xFFE0E0E0)
private val LabelGray = Color(0xFF616161)

/* ---------- 결과 타입 ---------- */
enum class RetryResultType { MCQ, OX, SHORT }

/* ---------- 결과 아이템 ---------- */
data class RetryResultItem(
    val id: String,
    val type: RetryResultType,
    val order: Int,           // 1..n
    val total: Int,           // 전체 개수
    val question: String,
    val options: List<String> = emptyList(), // MCQ/OX
    val userAnswer: String?,  // 사용자가 고른 값
    val correctAnswer: String,
    val explanation: String
) { val isCorrect: Boolean get() = userAnswer == correctAnswer }

/* =========================================================
 * 재도전 결과 리스트 화면 (TopBar 포함)
 * ========================================================= */
@Composable
fun QuizRetryAllResultScreen(
    categoryTitle: String,
    results: List<RetryResultItem>,
    onBack: () -> Unit = {},
    onFinishClick: () -> Unit = {},
    correctIconRes: Int? = null,
    wrongIconRes: Int? = null,
    backIconRes: Int? = null
) {
    val inPreview = LocalInspectionMode.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ----- TopBar : ← + 타이틀 중앙 -----
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                if (LocalInspectionMode.current) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Color.Black
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back), // ← 고정
                        contentDescription = "뒤로",
                        tint = Color.Unspecified
                    )
                }
            }
            Text(
                text = categoryTitle,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            // 오른쪽 더미 영역을 왼쪽 IconButton(48dp)과 동일하게
            Spacer(modifier = Modifier.size(56.dp))
        }

        Spacer(Modifier.height(16.dp))

        // ----- 결과 리스트 -----
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp)
        ) {
            itemsIndexed(results) { _, item ->
                RetryResultCard(
                    item = item,
                    correctIconRes = correctIconRes,
                    wrongIconRes = wrongIconRes
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        // ----- 하단 완료 버튼 -----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 48.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = onFinishClick,
                modifier = Modifier
                    .fillMaxWidth(0.5f)      // 가로 절반
                    .height(48.dp),          // 높이는 기존 유지(원하면 44~48dp 권장)
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "학습 마치기",
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/* ---------- 단일 카드(배경 EFF4FB + 흰 카드 + 정답/해설) ---------- */
@Composable
private fun RetryResultCard(
    item: RetryResultItem,
    correctIconRes: Int? = null,
    wrongIconRes: Int? = null
) {
    val inPreview = LocalInspectionMode.current

    Box(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {

            // 1) 흰 카드(질문/선택지)만 별도로
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "${item.order}/${item.total}",
                        fontFamily = Pretendard, fontSize = 12.sp,
                        fontWeight = FontWeight.Medium, color = Color(0xFF616161)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = item.question,
                        fontFamily = Pretendard, fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold, color = Color.Black, lineHeight = 26.sp
                    )
                    Spacer(Modifier.height(16.dp))

                    when (item.type) {
                        RetryResultType.MCQ -> McqResult(item.options, item.userAnswer, item.correctAnswer)
                        RetryResultType.OX   -> OxResult(item.userAnswer, item.correctAnswer)
                        RetryResultType.SHORT-> ShortResult(item.userAnswer, item.correctAnswer, item.isCorrect)
                    }
                }
            }

            // 2) 정답/해설 전용 BgBlue 박스 (상단 모서리 0dp → 카드와 딱 맞닿게)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(
                        topStart = 0.dp, topEnd = 0.dp,
                        bottomStart = 12.dp, bottomEnd = 12.dp
                    ))
                    .background(Color(0xFFEFF4FB)) // BgBlue
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 12.dp) // 세로 여백
                        .padding(start = 6.dp, end = 6.dp)   // ▶ 왼쪽 치우침 방지: 양쪽 6dp 들여쓰기
                ) {
                    // 라벨: 616161, 12, Medium
                    Text(
                        text = "정답",
                        fontFamily = Pretendard, fontSize = 12.sp,
                        fontWeight = FontWeight.Medium, color = Color(0xFF616161)
                    )
                    Spacer(Modifier.height(4.dp))
                    // 값: Black, 14, Medium
                    Text(
                        text = item.correctAnswer,
                        fontFamily = Pretendard, fontSize = 14.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "해설",
                        fontFamily = Pretendard, fontSize = 12.sp,
                        fontWeight = FontWeight.Medium, color = Color(0xFF616161)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.explanation,
                        fontFamily = Pretendard, fontSize = 14.sp,
                        fontWeight = FontWeight.Medium, color = Color.Black
                    )
                }
            }
        }

        // 좌상단 결과 아이콘
        val resId = if (item.isCorrect)
            (correctIconRes ?: R.drawable.ic_correct)
        else
            (wrongIconRes   ?: R.drawable.ic_wrong)

        if (!inPreview) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-12).dp, y = (-12).dp)
                    .size(120.dp)           // ← 72~88.dp 권장
                    .zIndex(1f)
            )
//            Icon(
//                painter = painterResource(id = resId),
//                contentDescription = null,
//                tint = Color.Unspecified,
//                modifier = Modifier
//                    .align(Alignment.TopStart)
//                    .offset(x = (-8).dp, y = (-8).dp)
//                    .size(48.dp)
//                    .zIndex(1f)
//            )
        } else {
            Icon(
                imageVector = if (item.isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Close,
                contentDescription = null,
                tint = if (item.isCorrect) BrandBlue else Color(0xFFFF0D0D),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-8).dp, y = (-8).dp)
                    .size(48.dp)
                    .zIndex(1f)
            )
        }
    }
}

/* ---------- 정답/해설 한 줄 컴포넌트 (라벨+값) ---------- */
@Composable
private fun InfoRow(label: String, value: String, startPadding: Dp = 0.dp, endPadding: Dp = 0.dp) {
    Column(Modifier.padding(start = startPadding, end = endPadding)) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = LabelGray
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = Pretendard,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

/* ---------- MCQ 렌더 ---------- */
@Composable
private fun McqResult(options: List<String>, user: String?, correct: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { opt ->
            val isCorrectOpt = opt == correct
            val isUserWrong  = (user == opt) && !isCorrectOpt

            val bgColor = when {
                isCorrectOpt -> CorrectFill
                isUserWrong  -> WrongFill
                else         -> ChipGray
            }
            val borderColor = when {
                isCorrectOpt -> BrandBlue
                isUserWrong  -> WrongRed
                else         -> Color(0xFFE0E0E0)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp)), // ✅ 테두리 적용
                shape = RoundedCornerShape(12.dp),
                color = bgColor
            ) {
                Text(
                    text = opt,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black // ✅ 내부색이 연하니 가독성 유지
                )
            }
        }
    }
}
//@Composable
//private fun McqResult(options: List<String>, user: String?, correct: String) {
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        options.forEach { opt ->
//            val isCorrect = opt == correct
//            val isUserSel = opt == user && !isCorrect
//            Surface(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(12.dp),
//                color = when {
//                    isCorrect -> BrandBlue
//                    isUserSel -> ChipSel
//                    else      -> ChipGray
//                }
//            ) {
//                Text(
//                    text = opt,
//                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
//                    fontFamily = Pretendard,
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium,
//                    color = if (isCorrect) Color.White else Color.Black
//                )
//            }
//        }
//    }
//}

/* ---------- OX 렌더 ---------- */
@Composable
private fun OxResult(user: String?, correct: String) {
    val tiles = listOf("O", "X")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        tiles.forEach { t ->
            val isCorrectTile = t == correct
            val isUserWrong   = (t == user) && !isCorrectTile

            val bgColor = when {
                isCorrectTile -> CorrectFill
                isUserWrong   -> WrongFill
                else          -> ChipGray
            }
            val borderColor = when {
                isCorrectTile -> BrandBlue
                isUserWrong   -> WrongRed
                else          -> Color(0xFFE0E0E0)
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp)), // ✅ 테두리
                shape = RoundedCornerShape(12.dp),
                color = bgColor
            ) {
                Text(
                    text = t,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}
//@Composable
//private fun OxResult(user: String?, correct: String) {
//    val tiles = listOf("O", "X")
//    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//        tiles.forEach { t ->
//            val isCorrect = t == correct
//            val isUserSel = t == user && !isCorrect
//            Surface(
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(12.dp),
//                color = when {
//                    isCorrect -> BrandBlue
//                    isUserSel -> ChipSel
//                    else      -> ChipGray
//                }
//            ) {
//                Text(
//                    text = t,
//                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
//                    fontFamily = Pretendard,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = if (isCorrect) Color.White else Color.Black
//                )
//            }
//        }
//    }
//}

/* ---------- 단답형 렌더 ---------- */
@Composable
private fun ShortResult(user: String?, correct: String, isCorrect: Boolean) {
    val bgColor = if (isCorrect) CorrectFill else WrongFill
    val border  = if (isCorrect) BrandBlue   else WrongRed
    val textCol = if (isCorrect) BrandBlue   else WrongRed

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, border, RoundedCornerShape(12.dp)), // ✅ 테두리
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.orEmpty(),
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textCol
            )
        }
    }
}
//@Composable
//private fun ShortResult(user: String?, correct: String, isCorrect: Boolean) {
//    val color = if (isCorrect) BrandBlue else Color(0xFFFF0D0D)
//    Surface(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(12.dp),
//        color = ChipGray
//    ) {
//        Row(
//            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = user.orEmpty(),
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = color
//            )
//        }
//    }
//}

/* ---------- 프리뷰 (OX 1, 4지, 단답형 포함, 총 7문항) ---------- */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "재도전 결과 리스트")
@Composable
private fun PreviewQuizRetryAllResultScreen() {
    val list = listOf(
        RetryResultItem(
            id = "1",
            type = RetryResultType.MCQ,
            order = 1, total = 7,
            question = "다른 사람의 감정을 이해 및 공감하는 능력을 뜻하는 단어는?",
            options = listOf("공감", "직관", "분석", "판단"),
            userAnswer = "직관",
            correctAnswer = "공감",
            explanation = "공감: 타인의 감정을 이해하고 함께 느끼는 능력."
        ),
        RetryResultItem(
            id = "2",
            type = RetryResultType.OX,
            order = 2, total = 7,
            question = "‘말뭉치는 어휘/문해력 향상에 도움을 준다’는 진술은 참이다.",
            options = listOf("O","X"),
            userAnswer = "O",
            correctAnswer = "O",
            explanation = "다양한 문장 패턴을 통해 어휘/독해력이 향상됨."
        ),
        RetryResultItem(
            id = "3",
            type = RetryResultType.SHORT,
            order = 3, total = 7,
            question = "밑줄 친 단어를 격식 있게 바꾸세요. ‘오늘 안에 보내줄게.’",
            userAnswer = "금일",
            correctAnswer = "금일",
            explanation = "‘오늘’의 격식체 표현은 ‘금일’."
        ),
        RetryResultItem(
            id = "4",
            type = RetryResultType.MCQ,
            order = 4, total = 7,
            question = "프로젝트의 위험을 사전에 줄이는 활동은?",
            options = listOf("테스팅", "리팩터링", "리스크 관리", "디버깅"),
            userAnswer = "리팩터링",
            correctAnswer = "리스크 관리",
            explanation = "리스크 관리는 사전 위험 식별/대응 계획 수립."
        ),
        RetryResultItem(
            id = "5",
            type = RetryResultType.OX,
            order = 5, total = 7,
            question = "HTTP는 상태를 보존한다.",
            options = listOf("O", "X"),
            userAnswer = "O",
            correctAnswer = "X",
            explanation = "HTTP는 Stateless 프로토콜."
        ),
        RetryResultItem(
            id = "6",
            type = RetryResultType.SHORT,
            order = 6, total = 7,
            question = "‘내일’을 격식 있게 쓰면?",
            userAnswer = "익일",
            correctAnswer = "익일",
            explanation = "‘내일’의 격식체는 ‘익일’."
        ),
        RetryResultItem(
            id = "7",
            type = RetryResultType.MCQ,
            order = 7, total = 7,
            question = "가장 빠른 정렬 알고리즘은 상황에 따라 다르다.",
            options = listOf("항상 퀵정렬", "항상 병합정렬", "상황에 따라 다름", "항상 버블정렬"),
            userAnswer = "상황에 따라 다름",
            correctAnswer = "상황에 따라 다름",
            explanation = "데이터 특성/메모리/안정성 요구에 따라 달라짐."
        )
    )

    MaterialTheme {
        Surface(color = Color.White) {
            QuizRetryAllResultScreen(
                categoryTitle = "취업 준비",
                results = list,
                onBack = {}
            )
        }
    }
}
