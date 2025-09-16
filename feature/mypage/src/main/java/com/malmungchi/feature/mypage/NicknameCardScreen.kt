package com.malmungchi.feature.mypage.nickname

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.mypage.R as MyPageR
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

/**
 * 마이페이지 위에 떠 있는 커스텀 모달(투명 스크림)
 * - 카드 이미지 600dp + 저장 버튼만 노출
 * - 우상단 닫기(ic_card_end) = onExit
 */
@Composable
fun NicknameCardDialog(
    nickname: String?,
    onExit: () -> Unit = {},
    onSaveImage: (String) -> Unit = {}
) {
    Dialog(
        onDismissRequest = onExit,
        properties = DialogProperties(usePlatformDefaultWidth = false) // 전체 뷰를 덮는 오버레이
    ) {
        // 스크림(반투명) → 마이페이지가 살짝 비쳐 보임
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66000000)) // 40% 블랙
        ) {
            // 다이얼로그 본문(가운데 정렬)
            NicknameCardDialogBody(
                nickname = nickname,
                onExit = onExit,
                onSaveImage = onSaveImage,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp) // 피그마 여백
            )
        }
    }
}
@Composable
private fun NicknameCardDialogBody(
    nickname: String?,
    onExit: () -> Unit,
    onSaveImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val imgRes = getNicknameCardImageResOrNull(nickname)
    val context = LocalContext.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // 버튼영역(48dp) + 간격(16dp) = 64dp 만큼은 카드 아래 공간으로 비워둠
        val reservedForButton = 64.dp
        // 사용 가능한 최대 카드 높이(= 다이얼로그 영역 - 버튼영역)
        val availableCardHeight = (maxHeight - reservedForButton)
        // 카드 높이: 가용 높이를 넘지 않되, 최대 600dp 유지
        val cardHeight = availableCardHeight.coerceAtMost(450.dp)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // ── 카드 이미지 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp) // ✅ 고정 높이 450dp
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (imgRes != null) {
                    Image(
                        painter = painterResource(id = imgRes),
                        contentDescription = "별명 카드 이미지",
                        contentScale = ContentScale.Fit,   // ⬅️ Crop → Fit
                        modifier = Modifier
                            .fillMaxSize()                 // Box 크기에 맞추되
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xFFF5F6F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = MyPageR.drawable.img_nickname_loading),
                            contentDescription = "닉네임 카드 로딩",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .aspectRatio(1f)
                        )
                    }
                }

                // 닫기 버튼
                IconButton(
                    onClick = onExit,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 40.dp)
                ) {
                    Icon(
                        painter = painterResource(id = MyPageR.drawable.ic_card_end),
                        contentDescription = "닫기",
                        modifier = Modifier.size(24.dp)

                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 저장 버튼(가운데 56~60%) ──
            OutlinedButton(
                onClick = {
                    val resId = imgRes
                    if (resId != null) {
                        // 갤러리에 저장
                        val name =
                            "nickname_${nickname ?: "card"}_${System.currentTimeMillis()}.png"
                        val ok = saveDrawableToPictures(context, resId, name)
                        if (ok) {
                            // 필요 시 콜백도 실행
                            nickname?.let(onSaveImage)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.56f)
                    .height(48.dp),
                shape = MaterialTheme.shapes.extraLarge,
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF195FCF))
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF195FCF)
                )
            ) {
                Text(
                    "카드 이미지 저장",
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
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

        Toast.makeText(context, "갤러리에 저장했어요.\n(Pictures/NicknameCards)", Toast.LENGTH_SHORT).show()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "이미지 저장에 실패했어요.", Toast.LENGTH_SHORT).show()
        false
    }
}

//@Composable
//private fun NicknameCardDialogBody(
//    nickname: String?,
//    onExit: () -> Unit,
//    onSaveImage: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val imgRes = getNicknameCardImageResOrNull(nickname)
//
//    Column(
//        modifier = modifier,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        // 카드 이미지(600dp)
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(480.dp) // ✅ 고정: 600dp
//                .clip(RoundedCornerShape(16.dp))
//        ) {
//            if (imgRes != null) {
//                Image(
//                    painter = painterResource(id = imgRes),
//                    contentDescription = "별명 카드 이미지",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.matchParentSize()
//                )
//            } else {
//                // 로딩/대체
//                Box(
//                    modifier = Modifier
//                        .matchParentSize()
//                        .background(Color(0xFFF5F6F9)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Image(
//                        painter = painterResource(id = MyPageR.drawable.img_nickname_loading),
//                        contentDescription = "닉네임 카드 로딩",
//                        contentScale = ContentScale.Fit,
//                        modifier = Modifier
//                            .fillMaxWidth(0.6f)
//                            .aspectRatio(1f)
//                    )
//                }
//            }
//
//            // 우상단 닫기 버튼
//            IconButton(
//                onClick = onExit,
//                modifier = Modifier
//                    .align(Alignment.TopEnd)
//                    .padding(top = 8.dp, end = 36.dp) // ← end 값 늘리면 왼쪽으로 이동
//            ) {
//                Icon(
//                    painter = painterResource(id = MyPageR.drawable.ic_card_end),
//                    contentDescription = "닫기"
//                )
//            }
//        }
//
//        Spacer(Modifier.height(8.dp))
//
//        // 저장 버튼(가운데 60%)
//        // 저장 버튼(가운데 60%)
//        OutlinedButton(
//            onClick = { nickname?.let(onSaveImage) },
//            modifier = Modifier
//                .fillMaxWidth(0.56f)
//                .height(48.dp),
//            shape = MaterialTheme.shapes.extraLarge,
//            border = ButtonDefaults.outlinedButtonBorder.copy(
//                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF195FCF)) // 기존 파란색을 테두리로
//            ),
//            colors = ButtonDefaults.outlinedButtonColors(
//                containerColor = Color.White,          // 배경 → 글자색(원래 흰색)
//                contentColor = Color(0xFF195FCF)       // 글자 → 원래 버튼 배경색(파란색)
//            )
//        ) {
//            Text(
//                "카드 이미지 저장",
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.SemiBold,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
//}

/** nickname → 이미지 리소스 (기본값 반환 없음) */
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
    else -> null
}

/* ----------------------------- Previews ----------------------------- */
/* Dialog는 미리보기 제약이 있어, 페이크 호스트로 스크림+본문을 그려줍니다. */
@Preview(showBackground = true, backgroundColor = 0xFFEFEFEF, showSystemUi = true)
@Composable
private fun NicknameCardDialogPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFEFEF))
        ) {
            // 스크림
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x66000000))
            )
            NicknameCardDialogBody(
                nickname = "언어연금술사",
                onExit = {},
                onSaveImage = {},
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEFEFEF, showSystemUi = true)
@Composable
private fun NicknameCardDialogLoadingPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFEFEF))
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x66000000))
            )
            NicknameCardDialogBody(
                nickname = null, // 로딩 예시
                onExit = {},
                onSaveImage = {},
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}
