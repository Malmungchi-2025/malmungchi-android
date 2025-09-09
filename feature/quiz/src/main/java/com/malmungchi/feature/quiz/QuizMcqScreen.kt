package com.malmungchi.feature.quiz

// ===== Imports =====
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.malmungchi.core.designsystem.Pretendard

/* ---------------------------------------------------------
 * 1) 모델
 * --------------------------------------------------------- */
data class QuizOption(val id: Int, val label: String)

data class QuizQuestion(
    val id: Int,
    val text: String,               // 문제 본문(설명)
    val options: List<QuizOption>,  // 4지선다
    val answerOptionId: Int         // 정답 id
)

/* ---------------------------------------------------------
 * 2) 4지선다 화면 (상태 시각화 포함)
 * --------------------------------------------------------- */

private val BrandBlue = Color(0xFF195FCF)
private val TrackGray = Color(0xFFFAFAFA)
private val TextGray  = Color(0xFF989898)
private val WrongRed  = Color(0xFFFF0000)

@Composable
fun QuizMcqScreen(
    categoryTitle: String,                 // 예) "취업 준비" / "심화" …
    questions: List<QuizQuestion>,
    startIndex: Int = 0,
    onBack: () -> Unit = {},
    onFinish: (correctCount: Int) -> Unit = {},
    // 👇 프리뷰/테스트를 위한 주입(실사용 시 null 유지)
    presetSelections: List<Int>? = null,     // 각 문항의 선택값(id) or -1
    presetSubmitted: List<Boolean>? = null   // 각 문항 "제출됨" 여부
) {
    val inPreview = LocalInspectionMode.current

    var index by rememberSaveable { mutableStateOf(startIndex) }

    // 각 문항의 선택 상태(-1 = 미선택)
    val selections = rememberSaveable(
        questions,
        saver = listSaver<SnapshotStateList<Int>, Int>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) {
        val seed = presetSelections ?: List(questions.size) { -1 }
        seed.toMutableStateList()
    }

    // 각 문항이 "제출" 되었는지
    val submitted = rememberSaveable(
        questions,
        saver = listSaver<SnapshotStateList<Int>, Int>(
            save = { it.map { b -> if (b == 1) 1 else 0 } },
            restore = { it.map { v -> if (v == 1) 1 else 0 }.toMutableStateList() }
        )
    ) {
        val seed = presetSubmitted ?: List(questions.size) { false }
        seed.map { if (it) 1 else 0 }.toMutableStateList()
    }

    // 프리뷰/디스패처 없을 땐 BackHandler 등록 X
    val hasDispatcher = LocalOnBackPressedDispatcherOwner.current != null
    if (!inPreview && hasDispatcher) {
        BackHandler {
            if (index > 0) index -= 1 else onBack()
        }
    }

    val q = questions.getOrNull(index)
    val step = index + 1
    val total = questions.size
    val isSubmitted = submitted.getOrNull(index) == 1
    val selectedId = selections.getOrNull(index) ?: -1
    val isCorrectSelection = (selectedId != -1 && selectedId == (q?.answerOptionId ?: -2))

    Column(
        Modifier
            .fillMaxSize()
            .padding(start = 20.dp, end = 20.dp, bottom = 48.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // 상단: 뒤로 + 카테고리 제목(가운데)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (index > 0) index -= 1 else onBack() }) {
                if (inPreview) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로",
                        tint = Color.Black
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
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
            // 오른쪽엔 비워서 중앙 정렬 유지
            Spacer(Modifier.width(24.dp))
        }

        Spacer(Modifier.height(16.dp))

        // 진행 바
        ProgressBarLarge(
            progress = step.toFloat() / total.toFloat(),
            trackColor = TrackGray,
            progressColor = BrandBlue,
            height = 10.dp
        )

        // 진행 바 아래 본문 영역
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // "4지선다 1/7" (디자인: #616161 / 12 / Medium)
            Text(
                text = "$step/$total", //4지선다
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )

            Spacer(Modifier.height(6.dp))

            // 안내 문구 (16 / Medium / #989898)
            Text(
                text = "주어진 설명에 해당하는 단어를 선택하세요!",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray
            )

            Spacer(Modifier.height(10.dp))

            // 문제 본문
            Text(
                text = q?.text.orEmpty(),
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                lineHeight = 30.sp
            )

            Spacer(Modifier.height(20.dp))

            // 4지선다
            q?.options?.forEach { opt ->
                // 현재 옵션의 시각 상태 계산
                val state = when {
                    isSubmitted && selectedId == opt.id && isCorrectSelection -> OptionVisualState.CORRECT
                    isSubmitted && selectedId == opt.id && !isCorrectSelection -> OptionVisualState.WRONG
                    !isSubmitted && selectedId == opt.id -> OptionVisualState.SELECTED
                    else -> OptionVisualState.DEFAULT
                }

                OptionItem(
                    label = opt.label,
                    state = state,
                    showResultIcon = (state == OptionVisualState.CORRECT || state == OptionVisualState.WRONG),
                    inPreview = inPreview,
                    onClick = {
                        if (!isSubmitted) selections[index] = opt.id
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.weight(1f))

        // 버튼: 제출 전엔 선택해야 활성화, 제출 후엔 항상 활성화(다음/결과)
        val enabled = if (!isSubmitted) selectedId != -1 else true
        Button(
            onClick = {
                if (!isSubmitted) {
                    // 첫 클릭: 제출 상태로 전환 → 정/오답 시각화
                    submitted[index] = 1
                } else {
                    // 두 번째 클릭: 다음 문항 or 종료
                    if (index < questions.lastIndex) {
                        index += 1
                    } else {
                        // 전체 채점(서버 붙기 전 로컬 계산)
                        val finalCorrect = questions.indices.count { i ->
                            val sel = selections[i]
                            sel != -1 && sel == questions[i].answerOptionId
                        }
                        onFinish(finalCorrect)
                    }
                }
            },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (enabled) BrandBlue else TrackGray,
                contentColor = if (enabled) Color.White else TextGray
            ),
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 80.dp)
                .height(48.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            val label = when {
                !isSubmitted -> "정답 제출"
                index < questions.lastIndex -> "다음 문제"
                else -> "결과 보기"
            }
            Text(label, fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/* ---------------------------------------------------------
 * 시각 상태별 옵션 컴포넌트
 * --------------------------------------------------------- */

private enum class OptionVisualState { DEFAULT, SELECTED, CORRECT, WRONG }

@Composable
private fun OptionItem(
    label: String,
    state: OptionVisualState,
    showResultIcon: Boolean,
    inPreview: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    val bg = when (state) {
        OptionVisualState.DEFAULT  -> Color.White
        OptionVisualState.SELECTED -> BrandBlue.copy(alpha = 0.2f)   // 선택(제출 전)
        OptionVisualState.CORRECT  -> BrandBlue.copy(alpha = 0.2f)   // 정답(제출 후)
        OptionVisualState.WRONG    -> WrongRed.copy(alpha = 0.2f)    // 오답(제출 후)
    }

    val border = when (state) {
        OptionVisualState.DEFAULT  -> Color(0xFFE0E0E0)
        OptionVisualState.SELECTED -> BrandBlue
        OptionVisualState.CORRECT  -> BrandBlue
        OptionVisualState.WRONG    -> WrongRed
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(width = 2.dp, color = border, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )

        if (showResultIcon) {
            if (inPreview) {
                // 프리뷰에서는 시스템 아이콘으로 대체
                val icon = if (state == OptionVisualState.CORRECT) Icons.Filled.CheckCircle else Icons.Filled.Close
                Icon(icon, contentDescription = null, tint = if (state == OptionVisualState.CORRECT) BrandBlue else WrongRed)
            } else {
                val resId = if (state == OptionVisualState.CORRECT)
                    R.drawable.img_quiz_correct
                else
                    R.drawable.img_quiz_incorrect

                Image(
                    painter = painterResource(id = resId),
                    contentDescription = null
                )
            }
        }
    }
}

/* ---------------------------------------------------------
 * 공용: 진행 바
 * --------------------------------------------------------- */

@Composable
private fun ProgressBarLarge(
    progress: Float,
    trackColor: Color,
    progressColor: Color,
    height: Dp
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(shape)
                .background(progressColor)
        )
    }
}

/* ---------------------------------------------------------
 * 3) 프리뷰 / 더미 데이터 (정답/오답 상태 확인)
 * --------------------------------------------------------- */

private val previewQuestions: List<QuizQuestion> = List(7) { i ->
    QuizQuestion(
        id = i + 1,
        text = "다른 사람의 감정을 이해 및 공감하는 능력을 뜻하는 단어는?",
        options = listOf(
            QuizOption(1,"공감"),
            QuizOption(2,"직관"),
            QuizOption(3,"분석"),
            QuizOption(4,"판단")
        ),
        answerOptionId = 1
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "정답 선택 후 (파랑 체크)")
@Composable
private fun PreviewQuizCorrect() {
    MaterialTheme {
        Surface {
            // 1번 문항에서 정답(1) 선택 + 제출됨
            QuizMcqScreen(
                categoryTitle = "취업 준비",
                questions = previewQuestions,
                startIndex = 0,
                presetSelections = listOf(1) + List(6) { -1 },
                presetSubmitted = listOf(true) + List(6) { false }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "오답 선택 후 (빨강 X)")
@Composable
private fun PreviewQuizWrong() {
    MaterialTheme {
        Surface {
            // 1번 문항에서 오답(3) 선택 + 제출됨
            QuizMcqScreen(
                categoryTitle = "취업 준비",
                questions = previewQuestions,
                startIndex = 0,
                presetSelections = listOf(3) + List(6) { -1 },
                presetSubmitted = listOf(true) + List(6) { false }
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    name = "선택만 · 1/7 (제출 전)"
)
@Composable
private fun PreviewQuizSelected_PreSubmit_1of7() {
    MaterialTheme {
        Surface {
            // Q1에서 정답(1) '공감'을 선택했지만 아직 제출하지 않은 상태
            QuizMcqScreen(
                categoryTitle = "취업 준비",
                questions = previewQuestions,
                startIndex = 0, // 1/7
                presetSelections = listOf(1) + List(6) { -1 }, // [1, -1, -1, -1, -1, -1, -1]
                presetSubmitted = List(7) { false }            // 전부 미제출
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    name = "선택만 · 2/7 (제출 전)"
)
@Composable
private fun PreviewQuizSelected_PreSubmit_2of7() {
    MaterialTheme {
        Surface {
            // Q2에서 오답(3) '분석'을 선택했지만 아직 제출하지 않은 상태
            QuizMcqScreen(
                categoryTitle = "취업 준비",
                questions = previewQuestions,
                startIndex = 1,                       // 2/7
                presetSelections = listOf(-1, 3) + List(5) { -1 }, // [-1, 3, -1, -1, -1, -1, -1]
                presetSubmitted = List(7) { false }   // 전부 미제출
            )
        }
    }
}
