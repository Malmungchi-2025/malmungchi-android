// feature/ai/EndChatConfirmDialog.kt (같은 파일에 넣어도 OK)
package com.malmungchi.feature.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

@Composable
fun EndChatConfirmDialog(
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onNo,
        title = {
            Text(
                "오늘은 대화를 마칠까요?",
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        // ✅ 본문을 아예 없애서 간격 제거
        text = null,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onYes,
                    shape = RoundedCornerShape(50),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                    modifier = Modifier.height(42.dp).weight(1f)
                ) {
                    Text("네", fontFamily = Pretendard, fontSize = 16.sp)
                }

                Button(
                    onClick = onNo,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(42.dp).weight(1f)
                ) {
                    Text("아니요", fontFamily = Pretendard, fontSize = 16.sp, color = Color.White)
                }
            }
        },
        dismissButton = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview_EndChatConfirmDialog() {
    EndChatConfirmDialog(
        onYes = {},
        onNo = {}
    )
}