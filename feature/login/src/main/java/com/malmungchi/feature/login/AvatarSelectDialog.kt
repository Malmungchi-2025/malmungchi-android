// file: feature/login/src/main/java/com/malmungchi/feature/login/AvatarSelectDialog.kt
package com.malmungchi.feature.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.login.R

private val BrandBlue = Color(0xFF195FCF)
private val NeonLime = Color(0xFFD9FF00)
private val Scrim = Color(0xCC000000)
private val DisabledBg = Color(0xFFEFF4FB)

private data class AvatarOption(
    val name: String,
    val resId: Int
)

/**
 * (풀스크린) 아바타 선택 스크린
 * - name: API로 받은 사용자 이름
 * - onConfirm: 선택 완료 시 avatarName 콜백
 * - onDismiss: 취소(필요 시)
 */
@Composable
fun AvatarSelectDialog(
    name: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val avatars = remember {
        listOf(
            AvatarOption("img_glass_mungchi", R.drawable.img_glass_mungchi), // ↖
            AvatarOption("img_glass_malchi",  R.drawable.img_glass_malchi),  // ↗
            AvatarOption("img_mungchi",       R.drawable.img_mungchi),       // ↙
            AvatarOption("img_malchi",        R.drawable.img_malchi)         // ↘
        )
    }
    var selected by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Scrim)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { /* 바깥 터치 무시 */ },
        contentAlignment = Alignment.Center
    ) {
        Surface(color = Color.Transparent) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "환영해요 ${name}님",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "말뭉치에서 사용할 프로필을 선택해주세요",
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // 2 x 2 그리드 (좌↔우 스왑)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
                ) {
                    // 원래 [0, 1] 순서를 [1, 0]로
                    AvatarCircle(
                        option = avatars[1],
                        selected = selected == avatars[1].name,
                        onClick = { selected = avatars[1].name }
                    )
                    AvatarCircle(
                        option = avatars[0],
                        selected = selected == avatars[0].name,
                        onClick = { selected = avatars[0].name }
                    )
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
                ) {
                    // 원래 [2, 3] 순서를 [3, 2]로
                    AvatarCircle(
                        option = avatars[3],
                        selected = selected == avatars[3].name,
                        onClick = { selected = avatars[3].name }
                    )
                    AvatarCircle(
                        option = avatars[2],
                        selected = selected == avatars[2].name,
                        onClick = { selected = avatars[2].name }
                    )
                }

                Spacer(Modifier.height(24.dp))

                // 하단 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        enabled = selected != null,
                        onClick = { selected?.let(onConfirm) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue,
                            contentColor = Color.White,
                            disabledContainerColor = DisabledBg, // #EFF4FB
                            disabledContentColor = BrandBlue     // 파란 글자 유지
                        ),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .height(44.dp)
                            .width(200.dp)
                    ) {
                        Text(
                            text = "선택 완료",
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 필요 시 취소 노출
                // Text(
                //     text = "취소",
                //     fontFamily = Pretendard,
                //     fontWeight = FontWeight.Medium,
                //     fontSize = 14.sp,
                //     color = Color(0xFFE0E0E0),
                //     modifier = Modifier
                //         .clip(RoundedCornerShape(8.dp))
                //         .clickable { onDismiss() }
                //         .padding(horizontal = 8.dp, vertical = 4.dp)
                // )
            }
        }
    }
}

@Composable
private fun AvatarCircle(
    option: AvatarOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val border = if (selected) BorderStroke(3.dp, BrandBlue) else null
    Box(
        modifier = Modifier
            .size(108.dp)
            .clip(CircleShape)
            .background(NeonLime)
            .then(if (border != null) Modifier.border(border, CircleShape) else Modifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = option.resId),
            contentDescription = option.name,
            modifier = Modifier
                .size(84.dp)
                .offset(y = (-3).dp), // ✅ 살짝 위로 보정
            contentScale = ContentScale.Fit, // ✅ 비율 유지 + 중앙
            alignment = Alignment.Center
        )
    }
}

/* PREVIEW */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 720)
@Composable
private fun AvatarSelectDialogPreview() {
    AvatarSelectDialog(
        name = "하진",
        onConfirm = {},
        onDismiss = {}
    )
}
