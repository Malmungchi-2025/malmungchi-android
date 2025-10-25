package com.malmungchi.feature.study

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
import com.malmungchi.feature.study.Pretendard

class EndStudyAlert {
    companion object {
        @Composable
        fun Show(
            onConfirm: () -> Unit,
            onDismiss: () -> Unit
        ) {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = {
                    Text(
                        "오늘의 학습이 끝나지 않았어요.",
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        "정말 종료하시겠습니까?",
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // ✅ 네 버튼
                        OutlinedButton(
                            onClick = { onConfirm() },
                            shape = RoundedCornerShape(50),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                            modifier = Modifier
                                .height(42.dp)
                                .weight(1f)
                        ) {
                            Text("네", fontSize = 16.sp, fontFamily = Pretendard)
                        }
                        Spacer(Modifier.width(8.dp))
                        // ✅ 아니요 버튼
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(42.dp)
                                .weight(1f)
                        ) {
                            Text("아니요", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
                        }
                    }
                },
                dismissButton = {},
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEndStudyAlert() {
    EndStudyAlert.Show(
        onConfirm = {},
        onDismiss = {}
    )
}