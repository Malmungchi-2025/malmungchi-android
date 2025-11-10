package com.malmungchi.feature.quiz

// ===== Imports =====
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
import com.malmungchi.core.model.quiz.OxStep

/* ---------------------------------------------------------
 * 팔레트
 * --------------------------------------------------------- */
private val BrandBlue   = Color(0xFF195FCF)
private val TrackGray   = Color(0xFFFAFAFA)
private val TextGray    = Color(0xFF989898)
private val TileBgBlue  = Color(0xFFEFF4FB)
private val WrongBg     = Color(0xFFFFCCCC)
private val WrongBorder = Color(0xFFFF0D0D)

/* ---------------------------------------------------------
 * OX 화면 (표시 + 콜백만, 로컬 상태 없음)
 * --------------------------------------------------------- */
@Composable
fun QuizOxScreen(
    categoryTitle: String,
    step: Int,                    // VM: ui.displayStep
    total: Int,                   // VM: ui.total
    progress: Float,              // VM: ui.progress
    question: OxStep?,            // ✅ VM: ui.current as OxStep?
    selectedIsO: Boolean?,        // VM: 현재 문항 선택값(null/O/X)
    submitted: Boolean,           // VM: 현재 문항 제출 여부
    onSelect: (isO: Boolean) -> Unit, // VM: QuizEvent.SelectOx
    onSubmit: () -> Unit,         // VM: QuizEvent.Submit
    onBack: () -> Unit,            // VM: QuizEvent.Back
    showPrimaryButton: Boolean = true
) {
    val inPreview = LocalInspectionMode.current

    val isCorrect = submitted &&
            selectedIsO != null &&
            question?.answerIsO != null &&
            selectedIsO == question.answerIsO

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(start = 20.dp, top = 48.dp, end = 20.dp, bottom = 48.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // 상단바
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                if (inPreview) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
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
            Spacer(
                modifier = Modifier.size(48.dp) // IconButton과 동일 크기(40~48dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        ProgressBarLarge(
            progress = progress.coerceIn(0f, 1f),
            trackColor = TrackGray,
            progressColor = BrandBlue,
            height = 10.dp
        )

        Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "$step/$total",
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "OX 퀴즈",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = question?.statement.orEmpty(),
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                lineHeight = 30.sp
            )

            Spacer(Modifier.height(20.dp))

            // O / X
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val leftVisual = visualizeOxTile(
                    labelIsO = true,
                    selected = selectedIsO == true,
                    isSubmitted = submitted,
                    isCorrect = isCorrect
                )
                val rightVisual = visualizeOxTile(
                    labelIsO = false,
                    selected = selectedIsO == false,
                    isSubmitted = submitted,
                    isCorrect = isCorrect
                )

                OxTile(
                    label = "O",
                    visual = leftVisual,
                    modifier = Modifier.weight(1f),
                    onClick = { if (!submitted) onSelect(true) }
                )
                OxTile(
                    label = "X",
                    visual = rightVisual,
                    modifier = Modifier.weight(1f),
                    onClick = { if (!submitted) onSelect(false) }
                )
            }

            // 제출 후 결과 아이콘 표시(선택된 쪽 아래)
            if (submitted && selectedIsO != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(24.dp),                    // ← 결과 영역 고정 높이
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconSize = 20.dp

                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (submitted && selectedIsO == true) {
                            ResultIcon(isCorrect = isCorrect, inPreview = inPreview, iconSize = iconSize)
                        }
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (submitted && selectedIsO == false) {
                            ResultIcon(isCorrect = isCorrect, inPreview = inPreview, iconSize = iconSize)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))
        if (showPrimaryButton) {            // ✅ 제출 후 숨김 가능
            val enabled = if (!submitted) (selectedIsO != null) else true
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
            Spacer(Modifier.height(8.dp))
        }
    }
}

//        val enabled = if (!submitted) (selectedIsO != null) else true
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

/* --------------------------------------------------------- */
private data class OxVisual(val bg: Color, val border: Color, val text: Color)

@Composable
private fun visualizeOxTile(
    labelIsO: Boolean,
    selected: Boolean,
    isSubmitted: Boolean,
    isCorrect: Boolean
): OxVisual {
    var bg = TileBgBlue
    var border = Color(0xFFE0E0E0)
    var text = Color.Black

    if (!isSubmitted) {
        if (selected) {
            border = BrandBlue
            text = BrandBlue
        }
    } else {
        if (selected) {
            if (isCorrect) {
                bg = TileBgBlue
                border = BrandBlue
                text = BrandBlue
            } else {
                bg = WrongBg
                border = WrongBorder
                text = WrongBorder
            }
        }
    }
    return OxVisual(bg, border, text)
}

@Composable
private fun ResultIcon(isCorrect: Boolean, inPreview: Boolean, iconSize: Dp = 20.dp) {
    if (inPreview) {
        Icon(
            imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Close,
            contentDescription = null,
            tint = if (isCorrect) BrandBlue else WrongBorder,
            modifier = Modifier.size(iconSize)
        )
    } else {
        val resId = if (isCorrect) R.drawable.img_quiz_correct else R.drawable.img_quiz_incorrect
        Icon(
            painter = painterResource(id = resId),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun OxTile(
    label: String,
    visual: OxVisual,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .height(120.dp)
            .clip(shape)
            .background(visual.bg)
            .border(2.dp, visual.border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontSize = 44.sp,
            fontWeight = FontWeight.Medium,
            color = visual.text
        )
    }
}

/* 공용 진행 바 */
@Composable
private fun ProgressBarLarge(
    progress: Float,
    trackColor: Color,
    progressColor: Color,
    height: Dp
) {
    val shape = RoundedCornerShape(999.dp)
    Box(Modifier.fillMaxWidth().height(height).clip(shape).background(trackColor)) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(shape)
                .background(progressColor)
        )
    }
}

/* ---------------------------------------------------------
 * 프리뷰 하네스 (VM 모델: OxStep 사용)
 * --------------------------------------------------------- */
private val previewOx: List<OxStep> = List(7) { i ->
    OxStep(
        id = (i + 1).toString(),
        statement = "‘기꺼이 남을 위하여 자신을 희생하는 마음가짐’을 뜻하는 단어는 ‘이기심’이다.",
        answerIsO = false // 프리뷰 정답: X
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "선택만(제출 전) - 버튼 활성화")
@Composable private fun Preview_OX_Selected_PreSubmit() {
    MaterialTheme {
        Surface(color = Color.White) {
            var index by remember { mutableIntStateOf(4) }
            var sel by remember { mutableStateOf<Boolean?>(true) } // O 선택
            var sub by remember { mutableStateOf(false) }

            QuizOxScreen(
                categoryTitle = "취업준비",
                step = index + 1,
                total = previewOx.size,
                progress = (if (sub) 1 else 0) / previewOx.size.toFloat(),
                question = previewOx[index],
                selectedIsO = sel,
                submitted = sub,
                onSelect = { if (!sub) sel = it },
                onSubmit = { if (!sub) sub = true else if (index < previewOx.lastIndex) index++ },
                onBack = { if (index > 0) index-- }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "정답 제출 후")
@Composable private fun Preview_OX_Submitted_Correct() {
    MaterialTheme {
        Surface(color = Color.White) {
            val qs = previewOx.toMutableList().also { it[4] = it[4].copy(answerIsO = true) }
            QuizOxScreen(
                categoryTitle = "취업준비",
                step = 5,
                total = qs.size,
                progress = 5f / qs.size,
                question = qs[4],
                selectedIsO = true,
                submitted = true,
                onSelect = {},
                onSubmit = {},
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "오답 제출 후")
@Composable private fun Preview_OX_Submitted_Wrong() {
    MaterialTheme {
        Surface(color = Color.White) {
            QuizOxScreen(
                categoryTitle = "취업준비",
                step = 5,
                total = previewOx.size,
                progress = 5f / previewOx.size,
                question = previewOx[4],
                selectedIsO = true,   // O를 골랐지만 정답은 X
                submitted = true,
                onSelect = {},
                onSubmit = {},
                onBack = {}
            )
        }
    }
}
