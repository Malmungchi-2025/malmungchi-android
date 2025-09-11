package com.malmungchi.feature.quiz

// ===== Imports =====
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

/* ---------- 색 ---------- */
private val BrandBlue = Color(0xFF195FCF)
private val TrackGray = Color(0xFFFAFAFA)
private val TextGray  = Color(0xFF989898)
private val Gray6161  = Color(0xFF616161)
private val ErrorRed  = Color(0xFFFF0D0D)

/* ---------- 안전 아이콘 헬퍼 ---------- */
@Composable
private fun SafeIcon(
    painterRes: Int?,
    fallback: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String? = null,
    tint: Color? = null
) {
    val inPreview = LocalInspectionMode.current
    val tryPainter = painterRes != null && !inPreview
    if (tryPainter) {
        val painter = runCatching { painterResource(id = painterRes!!) }.getOrNull()
        if (painter != null) {
            Icon(painter = painter, contentDescription = contentDescription, tint = tint ?: Color.Unspecified)
            return
        }
    }
    Icon(imageVector = fallback, contentDescription = contentDescription, tint = tint ?: LocalContentColor.current)
}

/* ---------- 화면: 표시 + 콜백만 ---------- */
@Composable
fun QuizShortAnswerScreen(
    categoryTitle: String,
    step: Int,                        // VM: ui.displayStep
    total: Int,                       // VM: ui.total
    progress: Float,                  // VM: ui.progress
    question: ShortStep?,             // ✅ VM: ui.current as ShortStep?
    // 입력/판정은 VM에서 내려주는 단일 소스
    inputText: String,                // VM: 현재 입력값
    submitted: Boolean,               // VM: 판정 완료 여부
    isCorrect: Boolean,               // VM: 판정 결과
    onInputChange: (String) -> Unit,  // VM: QuizEvent.FillShort
    onSubmit: () -> Unit,             // VM: QuizEvent.Submit
    onBack: () -> Unit,               // VM: QuizEvent.Back
    // (선택) 리소스 주입
    // ✅ retry 전용 옵션: 제출 후 내부 '다음 문제' 버튼 감추기
    hideNextAfterSubmit: Boolean = false,
    backIconRes: Int? = null,
    penIconRes: Int? = null,
    correctIconRes: Int? = null,
    incorrectIconRes: Int? = null,
    showPrimaryButton: Boolean = true
) {
    val fieldColor = when {
        !submitted -> Color.Black
        isCorrect  -> BrandBlue
        else       -> ErrorRed
    }

    Column(
        Modifier.fillMaxSize().background(Color.White).padding(start = 20.dp, end = 20.dp, bottom = 48.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                if (LocalInspectionMode.current) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = Color.Black)
                } else {
                    Icon(
                        painter = painterResource(id = backIconRes ?: R.drawable.ic_back),
                        contentDescription = "뒤로",
                        tint = if (backIconRes == null) Color.Unspecified else Color.Unspecified
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
            Spacer(Modifier.width(24.dp))
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
                color = Gray6161
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = question?.guide.orEmpty(),
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(10.dp))

            // 밑줄 색도 판정 색상으로
            Text(
                text = underlinedTextColored(
                    sentence = question?.sentence.orEmpty(),
                    target = question?.underlineText,
                    underlineColor = fieldColor
                ),
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    lineHeight = 30.sp
                )
            )

            Spacer(Modifier.height(20.dp))

            TextField(
                value = inputText,
                onValueChange = onInputChange,
                singleLine = true,
                placeholder = {
                    Text(
                        "해당 단어만 입력해주세요.",
                        fontFamily = Pretendard,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray
                    )
                },
                trailingIcon = {
                    when {
                        !submitted -> SafeIcon(penIconRes, Icons.Filled.Edit, tint = TextGray)
                        isCorrect  -> SafeIcon(correctIconRes, Icons.Filled.CheckCircle, tint = BrandBlue)
                        else       -> SafeIcon(incorrectIconRes, Icons.Filled.Close, tint = ErrorRed)
                    }
                },
                textStyle = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = fieldColor
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = fieldColor,
                    unfocusedIndicatorColor = fieldColor,
                    disabledIndicatorColor = fieldColor,
                    cursorColor = Color.Black,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.weight(1f))

        // ✅ 버튼 노출 여부 계산: showPrimaryButton AND (제출 전 OR 제출 후에도 보이게 허용)
        val shouldShowPrimary =
            showPrimaryButton && (!submitted || !hideNextAfterSubmit)

        if (shouldShowPrimary) {
            val hasInput = inputText.isNotBlank()
            Button(
                onClick = onSubmit,
                enabled = hasInput,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasInput) BrandBlue else TrackGray,
                    contentColor = if (hasInput) Color.White else TextGray
                ),
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 80.dp)
                    .height(48.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (!submitted) "정답 제출" else "다음 문제",
                    fontFamily = Pretendard, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            // 높이 튐 방지용 최소 여백
            Spacer(Modifier.height(8.dp))
        }
    }
}

//        val hasInput = inputText.isNotBlank()
//        Button(
//            onClick = onSubmit,
//            enabled = hasInput,
//            colors = ButtonDefaults.buttonColors(
//                containerColor = if (hasInput) BrandBlue else TrackGray,
//                contentColor = if (hasInput) Color.White else TextGray
//            ),
//            shape = MaterialTheme.shapes.extraLarge,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 80.dp)
//                .height(48.dp)
//                .align(Alignment.CenterHorizontally)
//        ) {
//            Text(
//                text = if (!submitted) "정답 제출" else "다음 문제",
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//    }
//}

/* ---------- 밑줄 도우미 ---------- */
private fun underlinedTextColored(
    sentence: String,
    target: String?,
    underlineColor: Color
): androidx.compose.ui.text.AnnotatedString {
    if (target.isNullOrBlank()) return buildAnnotatedString { append(sentence) }
    val start = sentence.indexOf(target)
    if (start < 0) return buildAnnotatedString { append(sentence) }
    val end = start + target.length
    return buildAnnotatedString {
        append(sentence.substring(0, start))
        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = underlineColor))
        append(sentence.substring(start, end))
        pop()
        append(sentence.substring(end))
    }
}

/* ---------- 진행바 ---------- */
@Composable
private fun ProgressBarLarge(progress: Float, trackColor: Color, progressColor: Color, height: Dp) {
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
 * 프리뷰 하네스 (VM 모델: ShortStep 사용)
 * --------------------------------------------------------- */
private val previewShort = listOf(
    ShortStep(
        id = "1",
        guide = "밑줄 친 단어를 상황에 맞게 다시 써보세요.",
        sentence = "오늘 안에 보내줄게.",
        underlineText = "오늘",
        answerText = "금일" // 프리뷰용
    )
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable private fun PreviewShort_Default() {
    MaterialTheme {
        Surface(color = Color.White) {
            var index by remember { mutableIntStateOf(0) }
            var text by remember { mutableStateOf("") }
            var sub by remember { mutableStateOf(false) }
            var correct by remember { mutableStateOf(false) }

            QuizShortAnswerScreen(
                categoryTitle = "취업 준비",
                step = index + 1,
                total = previewShort.size,
                progress = (if (sub) 1 else 0) / previewShort.size.toFloat(),
                question = previewShort[index],
                inputText = text,
                submitted = sub,
                isCorrect = correct,
                onInputChange = { text = it },
                onSubmit = {
                    if (!sub) {
                        sub = true
                        correct = text.trim() == (previewShort[index].answerText ?: "")
                    }
                },
                onBack = { },
                penIconRes = R.drawable.img_quiz_pen,
                correctIconRes = R.drawable.img_quiz_correct,
                incorrectIconRes = R.drawable.img_quiz_incorrect
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable private fun PreviewShort_Correct() {
    MaterialTheme {
        Surface {
            QuizShortAnswerScreen(
                categoryTitle = "취업 준비",
                step = 1, total = 1, progress = 1f,
                question = previewShort.first(),
                inputText = "금일",
                submitted = true,
                isCorrect = true,
                onInputChange = {},
                onSubmit = {},
                onBack = {},
                correctIconRes = R.drawable.img_quiz_correct,
                incorrectIconRes = R.drawable.img_quiz_incorrect
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable private fun PreviewShort_Incorrect() {
    MaterialTheme {
        Surface {
            QuizShortAnswerScreen(
                categoryTitle = "취업 준비",
                step = 1, total = 1, progress = 1f,
                question = previewShort.first(),
                inputText = "익일",
                submitted = true,
                isCorrect = false,
                onInputChange = {},
                onSubmit = {},
                onBack = {},
                correctIconRes = R.drawable.img_quiz_correct,
                incorrectIconRes = R.drawable.img_quiz_incorrect
            )
        }
    }
}
