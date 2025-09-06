package com.malmungchi.feature.mypage.nickname

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard

class SkipNickNameTestAlert {
    companion object {
        /**
         * onConfirm: 사용자가 "그만하기"를 눌렀을 때 (→ 마이페이지로 이동)
         * onDismiss: 사용자가 "이어하기"를 눌렀거나 바깥을 눌러 닫을 때 (→ 계속 진행)
         */
        @Composable
        fun Show(
            onConfirm: () -> Unit,
            onDismiss: () -> Unit
        ) {
            AlertDialog(
                onDismissRequest = { onDismiss() }, // 바깥 클릭/뒤로가기 = 이어하기
                title = {
                    Text(
                        "아직 문제 풀이가 완료되지 않았어요.",
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        "별명 테스트를 그만할까요?",
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                // 버튼은 기존처럼 커스텀 Row로 두 개 구성
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 보조 액션: 그만하기 (아웃라인) -> onConfirm()
                        OutlinedButton(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(50),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                            modifier = Modifier
                                .height(42.dp)
                                .weight(1f)
                        ) {
                            Text("그만하기", fontSize = 16.sp, fontFamily = Pretendard)
                        }
                        // 기본 액션: 이어하기 (파란색) -> onDismiss()
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(42.dp)
                                .weight(1f)
                        ) {
                            Text("이어하기", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
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