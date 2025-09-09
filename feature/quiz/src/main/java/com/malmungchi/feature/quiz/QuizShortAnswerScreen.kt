package com.malmungchi.feature.quiz

// ===== Imports =====
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.DrawableRes
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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

/* ---------- 모델 ---------- */
data class QuizShortQuestion(
    val id: Int,
    val guide: String,
    val sentence: String,
    val answer: String,
    val underlineText: String? = null
)

/* ---------- 색 ---------- */
private val BrandBlue = Color(0xFF195FCF)
private val TrackGray = Color(0xFFFAFAFA)
private val TextGray  = Color(0xFF989898)
private val Gray6161  = Color(0xFF616161)
private val ErrorRed  = Color(0xFFFF0D0D)

/* ---------- 이름으로 drawable 찾기(없으면 null) ---------- */
@Composable
@DrawableRes
private fun drawableIdOrNull(name: String): Int? {
    val context = LocalContext.current
    val id = remember(name) {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    }
    return id.takeIf { it != 0 }
}

/* ---------- 안전 아이콘 헬퍼 (프리뷰에서도 리소스 로드 시도, 실패 시 폴백) ---------- */
@Composable
private fun SafeIcon(
    painterRes: Int?,                // null 또는 리소스 ID
    fallback: ImageVector,           // 프리뷰/에러 시 사용할 머티리얼 아이콘
    contentDescription: String? = null,
    tint: Color? = null,
    allowPreviewResources: Boolean = false
) {
    val inPreview = LocalInspectionMode.current
    val tryPainter = painterRes != null && (!inPreview || allowPreviewResources)

    if (tryPainter) {
        val painter = runCatching { painterResource(id = painterRes!!) }.getOrNull()
        if (painter != null) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                tint = tint ?: Color.Unspecified
            )
            return
        }
    }
    Icon(
        imageVector = fallback,
        contentDescription = contentDescription,
        tint = tint ?: LocalContentColor.current
    )
}

/* ---------- 화면 ---------- */
@Composable
fun QuizShortAnswerScreen(
    categoryTitle: String,
    questions: List<QuizShortQuestion>,
    startIndex: Int = 0,
    onBack: () -> Unit = {},
    onFinish: (correctCount: Int) -> Unit = {},
    // 런타임/프리뷰에서 넘겨도 됨 (프리뷰는 allowPreviewResources=true로 로딩 시도)
    backIconRes: Int? = null,
    penIconRes: Int? = null,
    correctIconRes: Int? = null,
    incorrectIconRes: Int? = null,
    // 프리뷰 초기 상태
    previewInitialAnswer: String? = null,
    previewInitialJudge: Int? = null   // -1/0/1
) {
    var index by rememberSaveable { mutableStateOf(startIndex) }

    val inPreview = LocalInspectionMode.current

    val answers: SnapshotStateList<String> = rememberSaveable(
        questions, startIndex, inPreview, previewInitialAnswer
    ) {
        MutableList(questions.size) { i ->
            if (inPreview && i == startIndex) previewInitialAnswer.orEmpty() else ""
        }.toMutableStateList()
    }

    val judges: SnapshotStateList<Int> = rememberSaveable(
        questions, startIndex, inPreview, previewInitialJudge
    ) {
        MutableList(questions.size) { i ->
            if (inPreview && i == startIndex) (previewInitialJudge ?: -1).coerceIn(-1, 1) else -1
        }.toMutableStateList()
    }


    val hasDispatcher = LocalOnBackPressedDispatcherOwner.current != null
    if (!inPreview && hasDispatcher) BackHandler { if (index > 0) index-- else onBack() }

//    LaunchedEffect(Unit) {
//        if (inPreview) {
//            previewInitialAnswer?.let { answers[index] = it }
//            previewInitialJudge?.let { judges[index] = it.coerceIn(-1, 1) }
//        }
//    }

    val q = questions.getOrNull(index)
    val step = index + 1
    val total = questions.size

    val judge = judges.getOrNull(index) ?: -1
    val submitted = judge != -1
    val isCorrect = judge == 1
    val fieldColor = when {
        !submitted -> Color.Black
        isCorrect  -> BrandBlue
        else       -> ErrorRed
    }

    Column(
        Modifier.fillMaxSize().padding(start = 20.dp, end = 20.dp, bottom = 48.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (index > 0) index-- else onBack() }) {
                SafeIcon(
                    painterRes = backIconRes,
                    fallback   = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = Color.Black,
                    allowPreviewResources = true
                )
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
            progress = step.toFloat() / total.toFloat(),
            trackColor = TrackGray,
            progressColor = BrandBlue,
            height = 10.dp
        )

        Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "$step/$total", //
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Gray6161
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = q?.guide.orEmpty(),
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextGray,
                lineHeight = 24.sp
            )

            Spacer(Modifier.height(10.dp))

            // 밑줄 색상도 판정 색상으로
            Text(
                text = underlinedText(
                    sentence = q?.sentence.orEmpty(),
                    target = q?.underlineText
                ),
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,       // 문장 전체 색 = 검정
                    lineHeight = 30.sp
                )
            )
            Spacer(Modifier.height(20.dp))

            val input = answers.getOrNull(index) ?: ""
            TextField(
                value = input,
                onValueChange = { answers[index] = it },
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
                        !submitted -> SafeIcon(
                            penIconRes, Icons.Filled.Edit, tint = TextGray, allowPreviewResources = true
                        )
                        isCorrect  -> SafeIcon(
                            correctIconRes, Icons.Filled.CheckCircle, tint = BrandBlue, allowPreviewResources = true
                        )
                        else       -> SafeIcon(
                            incorrectIconRes, Icons.Filled.Close, tint = ErrorRed, allowPreviewResources = true
                        )
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

        val hasInput = (answers.getOrNull(index)?.isNotBlank() == true)
        Button(
            onClick = {
                val last = index >= questions.lastIndex
                if (judges[index] == -1) {
                    val ok = answers[index].trim() == (q?.answer ?: "").trim()
                    judges[index] = if (ok) 1 else 0
                } else {
                    if (!last) index += 1
                    else {
                        val correctCount = questions.indices.count { i -> judges[i] == 1 }
                        onFinish(correctCount)
                    }
                }
            },
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
                text = when {
                    judges[index] == -1 -> "정답 제출"
                    index < questions.lastIndex -> "다음 문제"
                    else -> "결과 보기"
                },
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/* ---------- 밑줄 도우미 ---------- */
private fun underlinedText(
    sentence: String,
    target: String?
): androidx.compose.ui.text.AnnotatedString {
    if (target.isNullOrBlank()) return buildAnnotatedString { append(sentence) }
    val start = sentence.indexOf(target)
    if (start < 0) return buildAnnotatedString { append(sentence) }
    val end = start + target.length
    return buildAnnotatedString {
        append(sentence.substring(0, start))
        // 색은 지정하지 않고 밑줄만 적용 → 부모 Text의 color(=검정) 유지
        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
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

/* ---------- 프리뷰 ---------- */
private val previewShortQuestions = listOf(
    QuizShortQuestion(
        id = 1,
        guide = "밑줄 친 단어를 상황에 맞게 다시 써보세요.",
        sentence = "오늘 안에 보내줄게.",
        answer = "금일",
        underlineText = "오늘"
    )
)

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewShort_Default() {
    MaterialTheme {
        Surface {
            QuizShortAnswerScreen(
                categoryTitle = "취업 준비",
                questions = previewShortQuestions,
                onBack = {},
                onFinish = {},
                // 이름으로 찾아보고 없으면 null → 머티리얼 폴백
                penIconRes = drawableIdOrNull("img_quiz_pen"),
                correctIconRes = drawableIdOrNull("img_quiz_correct"),
                incorrectIconRes = drawableIdOrNull("img_quiz_incorrect")
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewShort_Correct() {
    MaterialTheme {
        Surface {
            QuizShortAnswerScreen(
                categoryTitle = "취업 준비",
                questions = previewShortQuestions,
                previewInitialAnswer = "금일",
                previewInitialJudge = 1,
                correctIconRes = drawableIdOrNull("img_quiz_correct"),
                incorrectIconRes = drawableIdOrNull("img_quiz_incorrect")
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewShort_Incorrect() {
    MaterialTheme {
        Surface {
            QuizShortAnswerScreen(
                categoryTitle = "취업 준비",
                questions = previewShortQuestions,
                previewInitialAnswer = "익일",
                previewInitialJudge = 0,
                correctIconRes = drawableIdOrNull("img_quiz_correct"),
                incorrectIconRes = drawableIdOrNull("img_quiz_incorrect")
            )
        }
    }
}
