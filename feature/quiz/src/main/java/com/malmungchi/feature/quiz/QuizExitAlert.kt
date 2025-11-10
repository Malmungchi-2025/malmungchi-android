package com.malmungchi.feature.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

private val BrandBlue = Color(0xFF195FCF)
private val Gray6161 = Color(0xFF616161)

/**
 * 퀴즈 중단 확인 알럿
 *
 * @param visible  true일 때만 표시
 * @param onConfirmQuit  "네" (그만하기) 클릭
 * @param onContinue     "아니오" (이어하기) 또는 바깥 클릭/뒤로가기
 */
@Composable
fun QuizExitAlert(
    visible: Boolean,
    onConfirmQuit: () -> Unit,
    onContinue: () -> Unit
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onContinue,
        title = {
            Text(
                text = "아직 문제풀이가 완료되지 않았어요.",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,              // ← 150%
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "퀴즈를 그만 할까요?",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Gray6161,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,              // ← 150%
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ① 보조(아웃라인): "네" = 그만하기
                OutlinedButton(
                    onClick = onConfirmQuit,
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBlue),
                    modifier = Modifier
                        .height(42.dp)
                        .weight(1f)
                ) {
                    Text("네", fontSize = 16.sp, fontFamily = Pretendard)
                }
                // ② 기본(채움): "아니오" = 이어하기
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .height(42.dp)
                        .weight(1f)
                ) {
                    Text("아니오", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
                }
            }
        },
        dismissButton = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}
@Preview(
    name = "QuizExitAlert – Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Composable
private fun PreviewQuizExitAlert() {
    MaterialTheme {
        Surface {
            // 미리보기용: 항상 보이게
            QuizExitAlert(
                visible = true,
                onConfirmQuit = { /* preview */ },
                onContinue = { /* preview */ }
            )
        }
    }
}
