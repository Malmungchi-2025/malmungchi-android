package com.malmungchi.feature.study.first

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.malmungchi.feature.study.Pretendard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordCollectBottomSheet(
    word: String,
    definition: String,
    example: String,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = Color.Transparent // ✅ 기존 디자인 유지 위해 투명 배경
    ) {
        // ✅ 기존 Surface 디자인 그대로 사용
        Surface(
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
            ) {
                Text("단어 수집", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard, color = Color(0xFF195FCF))

                Spacer(Modifier.height(12.dp))

                Text(word, fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    fontFamily = Pretendard, color = Color(0xFF333333))

                Spacer(Modifier.height(4.dp))
                Text(": $definition", fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard, color = Color(0xFF333333))

                Spacer(Modifier.height(8.dp))
                Text(example, fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    fontFamily = Pretendard, color = Color(0xFF666666))

                // ✅ 버튼 위 흰색 여백
                Spacer(Modifier.height(48.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 🔹 취소 버튼 — 흰색 배경 + 파란 테두리 (#195FCF, 1dp)
                    Button(
                        onClick = { onDismiss() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF195FCF)), // ✅ 테두리 추가
                        modifier = Modifier
                            .height(36.dp)
                            .weight(1f)
                    ) {
                        Text(
                            "취소",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = Pretendard,
                            color = Color(0xFF195FCF)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSaveClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                        modifier = Modifier.weight(1f).height(36.dp)
                    ) {
                        Text("저장", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            fontFamily = Pretendard, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun WordCollectBottomSheetContent(
    word: String,
    definition: String,
    example: String,
    onDismiss: () -> Unit = {},
    onSaveClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(
                text = "단어 수집",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                color = Color(0xFF195FCF)
            )

            Spacer(Modifier.height(12.dp)) // 24 → 12로 수정됨 (✔️ 요청 반영)

            Text(
                text = word,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
                color = Color(0xFF333333)
            )

            Spacer(Modifier.height(4.dp)) // 단어 → 뜻 (✔️)
            Text(
                text = ": $definition",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                color = Color(0xFF333333)
            )

            Spacer(Modifier.height(8.dp)) // 뜻 → 예문 (✔️)
            Text(
                text = example,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                color = Color(0xFF666666)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF195FCF)),
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f)
                ) {
                    Text(
                        "취소",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        color = Color(0xFF195FCF)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                    modifier = Modifier
                        .height(40.dp)
                        .weight(1f)
                ) {
                    Text(
                        "단어 수집하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        color = Color.White
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewWordCollectBottomSheetContent() {
    WordCollectBottomSheetContent(
        word = "작성",
        definition = "문서나 글 따위를 씀",
        example = "보고서를 작성하여 제출하세요."
    )
}
