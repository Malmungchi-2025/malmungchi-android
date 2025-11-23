package com.malmungchi.feature.quiz

// ===== Imports =====
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.core.model.quiz.McqStep
import com.malmungchi.core.model.quiz.QuizOption

// ✅ 도메인 모델은 ViewModel 쪽(같은 패키지)에 이미 있음: McqStep, QuizOption
// sealed interface QuizStep { ... }
// data class McqStep(...)
// data class QuizOption(...)

/* ---------------------------------------------------------
 * 4지선다 화면 (표시 + 콜백만, 로컬 상태 없음)
 * --------------------------------------------------------- */
private val BrandBlue = Color(0xFF195FCF)
private val TrackGray = Color(0xFFFAFAFA)
private val TextGray  = Color(0xFF989898)
private val WrongRed  = Color(0xFFFF0000)

@Composable
fun QuizMcqScreen(
    categoryTitle: String,                 // 예) "취업 준비" / "심화" …
    step: Int,                             // VM: ui.displayStep
    total: Int,                            // VM: ui.total
    progress: Float,                       // VM: ui.progress (0f..1f)
    question: McqStep?,                    // ✅ VM: ui.current as McqStep?
    selectedOptionId: Int?,                // VM: 현재 문항 선택값 (없으면 null)
    submitted: Boolean,                    // VM: 현재 문항 제출 여부
    onSelect: (optionId: Int) -> Unit,     // VM: QuizEvent.SelectMcq
    onSubmit: () -> Unit,                  // VM: QuizEvent.Submit
    onBack: () -> Unit,                     // VM: QuizEvent.Back
    showPrimaryButton: Boolean = true

) {
    val inPreview = LocalInspectionMode.current
    val selectedId = selectedOptionId ?: -1
    val isCorrectSelection =
        submitted &&
                selectedId != -1 &&
                question?.correctOptionId != null &&
                selectedId == question.correctOptionId

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, top = 48.dp, end = 20.dp, bottom = 48.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // 상단: 뒤로 + 카테고리 제목(가운데)
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                if (inPreview) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
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
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(
                modifier = Modifier.size(48.dp) // IconButton과 동일 크기(40~48dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // 진행 바 (외부 progress 사용)
        ProgressBarLarge(
            progress = progress.coerceIn(0f, 1f),
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

            // "n/total"
            Text(
                text = "$step/$total",
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )

            Spacer(Modifier.height(6.dp))

            // 안내 문구
            Text(
                text = "주어진 설명에 해당하는 단어를 선택하세요!",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray
            )

            Spacer(Modifier.height(10.dp))

            // 문제 본문
            UnderlinedText(
                full = question?.text.orEmpty(),
                target = question?.underline
            )

            Spacer(Modifier.height(20.dp))

            // 4지선다
            question?.options?.forEach { opt ->
                val state = when {
                    submitted && selectedId == opt.id && isCorrectSelection -> OptionVisualState.CORRECT
                    submitted && selectedId == opt.id && !isCorrectSelection -> OptionVisualState.WRONG
                    !submitted && selectedId == opt.id -> OptionVisualState.SELECTED
                    else -> OptionVisualState.DEFAULT
                }

                OptionItem(
                    label = opt.label,
                    state = state,
                    showResultIcon = (state == OptionVisualState.CORRECT || state == OptionVisualState.WRONG),
                    inPreview = inPreview,
                    onClick = { if (!submitted) onSelect(opt.id) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }

        Spacer(Modifier.weight(1f))
        if (showPrimaryButton) {            // ✅ 제출 후 숨길 수 있음
            val enabled = if (!submitted) selectedId != -1 else true
            Button(
                onClick = onSubmit,
                enabled = enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled) BrandBlue else TrackGray,
                    contentColor = if (enabled) Color.White else TextGray
                ),
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 80.dp, end = 80.dp, bottom = 48.dp)
                    .height(48.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                val label = if (!submitted) "정답 제출" else "다음 문제"
                Text(
                    label,
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            // 레이아웃 튀지 않도록 살짝 여백만
            Spacer(Modifier.height(8.dp))
        }
    }
}

//밑줄 ui 추가
@Composable
fun UnderlinedText(
    full: String,
    target: String?
) {
    if (target.isNullOrBlank()) {
        Text(
            text = full,
            fontFamily = Pretendard,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
        return
    }

    val annotated = buildAnnotatedString {
        val start = full.indexOf(target)
        if (start >= 0) {
            append(full.substring(0, start))
            withStyle(
                style = SpanStyle(
                    color = BrandBlue,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(target)
            }
            append(full.substring(start + target.length))
        } else {
            append(full)
        }
    }

    Text(
        text = annotated,
        fontFamily = Pretendard,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Black
    )
}

//        // 버튼: 제출 전엔 선택해야 활성화, 제출 후엔 항상 활성화(다음/결과)
//        val enabled = if (!submitted) selectedId != -1 else true
//        Button(
//            onClick = onSubmit,
//            enabled = enabled,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = if (enabled) BrandBlue else TrackGray,
//                contentColor = if (enabled) Color.White else TextGray
//            ),
//            shape = MaterialTheme.shapes.extraLarge,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 80.dp)
//                .height(48.dp)
//                .align(Alignment.CenterHorizontally)
//        ) {
//            val label = if (!submitted) "정답 제출" else "다음 문제"
//            Text(label, fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
//        }
//    }
//}

/* ---------------------------------------------------------
 * 시각 상태별 옵션 컴포넌트
 * --------------------------------------------------------- */
private enum class OptionVisualState { DEFAULT, SELECTED, CORRECT, WRONG }

//@Composable
//private fun OptionItem(
//    label: String,
//    state: OptionVisualState,
//    showResultIcon: Boolean,
//    inPreview: Boolean,
//    onClick: () -> Unit
//) {
//    val shape = RoundedCornerShape(12.dp)
//    val iconSize = 20.dp                    // ← 아이콘 고정 크기
//    val iconSlotWidth = 28.dp               // ← 슬롯 폭(여유 약간)
//
//    val bg = when (state) {
//        OptionVisualState.DEFAULT  -> Color.White
//        OptionVisualState.SELECTED -> BrandBlue.copy(alpha = 0.2f)   // 선택(제출 전)
//        OptionVisualState.CORRECT  -> BrandBlue.copy(alpha = 0.2f)   // 정답(제출 후)
//        OptionVisualState.WRONG    -> WrongRed.copy(alpha = 0.2f)    // 오답(제출 후)
//    }
//
//    val border = when (state) {
//        OptionVisualState.DEFAULT  -> Color(0xFFE0E0E0)
//        OptionVisualState.SELECTED -> BrandBlue
//        OptionVisualState.CORRECT  -> BrandBlue
//        OptionVisualState.WRONG    -> WrongRed
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clip(shape)
//            .background(bg)
//            .border(width = 2.dp, color = border, shape = shape)
//            .clickable(onClick = onClick)
//            .padding(horizontal = 16.dp, vertical = 14.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = label,
//            fontFamily = Pretendard,
//            fontSize = 16.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color.Black,
//            modifier = Modifier.weight(1f)
//        )
//
//        // ▼ 항상 동일 폭의 아이콘 슬롯 유지 (보여줄 때만 아이콘 배치)
//        Box(
//            modifier = Modifier
//                .width(iconSlotWidth)
//                .height(iconSize),
//            contentAlignment = Alignment.Center
//        ) {
//            if (showResultIcon) {
//                if (inPreview) {
//                    val icon = if (state == OptionVisualState.CORRECT)
//                        Icons.Filled.CheckCircle else Icons.Filled.Close
//                    Icon(icon, contentDescription = null, tint =
//                        if (state == OptionVisualState.CORRECT) BrandBlue else WrongRed,
//                        modifier = Modifier.size(iconSize)
//                    )
//                } else {
//                    val resId = if (state == OptionVisualState.CORRECT)
//                        R.drawable.img_quiz_correct else R.drawable.img_quiz_incorrect
//                    Icon(
//                        painter = painterResource(id = resId),
//                        contentDescription = null,
//                        tint = Color.Unspecified,
//                        modifier = Modifier.size(iconSize)
//                    )
//                }
//            }
//        }
//    }
//}
@Composable
private fun OptionItem(
    label: String,
    state: OptionVisualState,
    showResultIcon: Boolean,
    inPreview: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val iconSize = 20.dp
    val iconSlotWidth = 28.dp

    // 🔵 요구사항 반영: 상태별 배경색
    val bg = when (state) {
        OptionVisualState.DEFAULT  -> Color(0xFFEFF4FB)            // 선택 전
        OptionVisualState.SELECTED -> BrandBlue.copy(alpha = 0.2f)  // 선택 후 (제출 전)
        OptionVisualState.CORRECT  -> BrandBlue.copy(alpha = 0.2f)  // 정답
        OptionVisualState.WRONG    -> WrongRed.copy(alpha = 0.2f)   // 오답
    }

    // 🔵 상태별 경계선
    val border = when (state) {
        OptionVisualState.DEFAULT  -> Color(0xFFE0E0E0)
        OptionVisualState.SELECTED -> BrandBlue
        OptionVisualState.CORRECT  -> BrandBlue
        OptionVisualState.WRONG    -> WrongRed
    }

    // 🔵 상태별 글자색
    val textColor = when (state) {
        OptionVisualState.DEFAULT  -> Color.Black
        OptionVisualState.SELECTED -> BrandBlue
        OptionVisualState.CORRECT  -> BrandBlue      // 정답 → 파랑
        OptionVisualState.WRONG    -> WrongRed       // 오답 → 빨강
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)                 // ← 고정 높이
            .clip(shape)
            .background(bg)
            .border(width = 1.dp, color = border, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically  // ← 텍스트 수직 가운데 정렬
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        // 아이콘 영역
        Box(
            modifier = Modifier
                .width(iconSlotWidth)
                .height(iconSize),
            contentAlignment = Alignment.Center
        ) {
            if (showResultIcon) {
                if (inPreview) {
                    val icon = if (state == OptionVisualState.CORRECT)
                        Icons.Filled.CheckCircle else Icons.Filled.Close
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (state == OptionVisualState.CORRECT) BrandBlue else WrongRed,
                        modifier = Modifier.size(iconSize)
                    )
                } else {
                    val resId = if (state == OptionVisualState.CORRECT)
                        R.drawable.img_quiz_correct else R.drawable.img_quiz_incorrect
                    Icon(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(iconSize)
                    )
                }
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
 * 3) 프리뷰 / 더미 (VM 모델: McqStep 사용)
 *   - 옛 프리뷰와 동일 비주얼을 위해 하네스에서 로컬 상태 시뮬
 * --------------------------------------------------------- */
private val previewMcqList: List<McqStep> = List(7) { i ->
    McqStep(
        id = (i + 1).toString(),
        text = "다른 사람의 감정을 이해 및 공감하는 능력을 뜻하는 단어는?",
        options = listOf(
            QuizOption(1,"공감"),
            QuizOption(2,"직관"),
            QuizOption(3,"분석"),
            QuizOption(4,"판단")
        ),
        correctOptionId = 1 // 프리뷰용
    )
}

/** 프리뷰 전용 하네스 */
@Composable
private fun PreviewHarnessMcq(
    questions: List<McqStep>,
    startIndex: Int,
    presetSelections: List<Int>,   // -1은 미선택
    presetSubmitted: List<Boolean> // 제출 여부
) {
    var index by remember { mutableIntStateOf(startIndex) }
    val selections = remember { mutableStateListOf(*presetSelections.toTypedArray()) }
    val submitted  = remember { mutableStateListOf(*presetSubmitted.toTypedArray()) }

    val step = index + 1
    val total = questions.size
    val progress = (submitted.count { it }.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    val q = questions.getOrNull(index)
    val selectedId = selections.getOrNull(index)?.takeIf { it != -1 }
    val isSubmitted = submitted.getOrNull(index) == true

    QuizMcqScreen(
        categoryTitle = "취업 준비",
        step = step,
        total = total,
        progress = progress,
        question = q,
        selectedOptionId = selectedId,
        submitted = isSubmitted,
        onSelect = { optId -> if (!isSubmitted) selections[index] = optId },
        onSubmit = {
            if (!isSubmitted) submitted[index] = true
            else if (index < questions.lastIndex) index += 1
        },
        onBack = { if (index > 0) index -= 1 }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "정답 선택 후 (파랑 체크)")
@Composable private fun PreviewQuizCorrect() {
    MaterialTheme {
        Surface(color = Color.White){
            PreviewHarnessMcq(
                questions = previewMcqList,
                startIndex = 0,
                presetSelections = listOf(1) + List(6) { -1 },
                presetSubmitted  = listOf(true) + List(6) { false }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "오답 선택 후 (빨강 X)")
@Composable private fun PreviewQuizWrong() {
    MaterialTheme {
        Surface {
            PreviewHarnessMcq(
                questions = previewMcqList,
                startIndex = 0,
                presetSelections = listOf(3) + List(6) { -1 },
                presetSubmitted  = listOf(true) + List(6) { false }
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    name = "선택만 · 1/7 (제출 전)"
)
@Composable private fun PreviewQuizSelected_PreSubmit_1of7() {
    MaterialTheme {
        Surface {
            PreviewHarnessMcq(
                questions = previewMcqList,
                startIndex = 0,
                presetSelections = listOf(1) + List(6) { -1 },
                presetSubmitted  = List(7) { false }
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    name = "선택만 · 2/7 (제출 전)"
)
@Composable private fun PreviewQuizSelected_PreSubmit_2of7() {
    MaterialTheme {
        Surface {
            PreviewHarnessMcq(
                questions = previewMcqList,
                startIndex = 1,
                presetSelections = listOf(-1, 3) + List(5) { -1 },
                presetSubmitted  = List(7) { false }
            )
        }
    }
}
