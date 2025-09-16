package com.malmungchi.feature.mypage.nickname


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.malmungchi.feature.mypage.R as MyPageR
import androidx.compose.ui.platform.LocalContext
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import android.widget.Toast
import androidx.activity.compose.BackHandler

@Composable
fun NicknameTestResultScreen(
    userName: String? = null,   // ✅ 기본값 추가
    nickname: String?,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {

    BackHandler { onExit() }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFF4FB))
            //.background(Color.White)
    ) {

        // ✅ Top Bar (ic_back + "별명 카드")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp) // 좌우 20, 위 8
                .height(48.dp)
                .align(Alignment.TopCenter)
        ) {
            Icon(
                painter = painterResource(id = MyPageR.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterStart)
                    .clickable { onExit() }     // ← 뒤로가기 동작
            )
            Text(
                text = "별명 카드",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Pretendard,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center) // 가운데 정렬
            )
        }

        // 별명(=이미지 리소스)이 준비될 때만 카드 렌더링
        val imgRes = getNicknameCardImageResOrNull(nickname)
        if (imgRes != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 100.dp)
                    .height(600.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.TopCenter)
            ) {
                Image(
                    painter = painterResource(id = imgRes),
                    contentDescription = "별명 카드 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
                if (!userName.isNullOrBlank()) {
                    Text(
                        text = "$userName 님의 별명은",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 24.dp)
                    )
                }
            }
        } else {
            // ← 로딩/스켈레톤 (원하면 다른 UI로 교체 가능)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp)
                    .padding(horizontal = 20.dp)
                    .padding(top = 80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .align(Alignment.TopCenter)
                    .background(Color(0xFFF5F6F9)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // 버튼 바: 하단에서 48dp 띄우고, 시스템 내비게이션 인셋도 확보
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 48.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val context = LocalContext.current
            val blue = Color(0xFF195FCF)
            val resIdForSave = getNicknameCardImageResOrNull(nickname)

            // ✅ "카드 이미지 저장" (흰색 Outlined 버튼)
            OutlinedButton(
                onClick = {
                    val resId = resIdForSave
                    if (resId == null) {
                        Toast.makeText(context, "이미지가 아직 준비되지 않았어요.", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    val name = "nickname_${nickname ?: "card"}_${System.currentTimeMillis()}.png"
                    val ok = saveDrawableToPictures(context, resId, name)
                    if (ok) {
                        // 필요하면 콜백을 나중에 쓸 수 있도록 남겨둠
                        // onSaveImage 같은 게 없다면 토스트만으로 충분
                    }
                },
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f),
                shape = RoundedCornerShape(50),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(blue)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,   // ⬅️ 흰색 배경
                    contentColor = blue             // ⬅️ 파란색 텍스트/아이콘
                ),
                enabled = resIdForSave != null
            ) {
                Text(
                    "카드 이미지 저장",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // "나가기" (기존 유지)
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = blue),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .height(42.dp)
                    .weight(1f)
            ) {
                Text(
                    "나가기",
                    fontSize = 16.sp,
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}


/** 별명 → 이미지 리소스 매핑 (NicknameCardScreen과 동일) */
private fun getNicknameCardImageResOrNull(nickname: String?): Int? = when (nickname) {
    "언어연금술사" -> MyPageR.drawable.img_word_magician
    "눈치번역가"  -> MyPageR.drawable.img_sense
    "감각해석가"  -> MyPageR.drawable.img_sense2
    "맥락추리자"  -> MyPageR.drawable.img_context
    "언어균형술사"-> MyPageR.drawable.img_language
    "낱말여행자"  -> MyPageR.drawable.img_word2
    "단어수집가"  -> MyPageR.drawable.img_word3
    "의미해석가"  -> MyPageR.drawable.img_context2
    "언어모험가"  -> MyPageR.drawable.img_language2
    else -> MyPageR.drawable.img_nickname_loading        // ← ★ 기본(프리뷰용) 이미지 리턴 금지
}
private fun saveDrawableToPictures(
    context: Context,
    @DrawableRes resId: Int,
    displayName: String = "nickname_card_${System.currentTimeMillis()}.png"
): Boolean {
    return try {
        val resolver = context.contentResolver
        val bitmap = BitmapFactory.decodeResource(context.resources, resId)

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NicknameCards")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(collection, values) ?: return false
        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        }

        Toast.makeText(
            context,
            "갤러리에 저장했어요.\n(Pictures/NicknameCards)",
            Toast.LENGTH_SHORT
        ).show()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "이미지 저장에 실패했어요.", Toast.LENGTH_SHORT).show()
        false
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewNicknameTestResultScreen_Magician() {
    MaterialTheme {
        Surface {
            NicknameTestResultScreen(
                userName = "김뭉치",     // ← 추가
                nickname = "언어연금술사",
                onRetry = {},
                onExit = {}
            )
        }
    }
}