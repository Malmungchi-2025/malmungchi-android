package com.malmungchi.feature.friend

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.friend.R
import kotlinx.coroutines.delay


// ───────────────── Color Tokens ─────────────────
private val BrandBlue   = Color(0xFF195FCF)
private val GrayTextSub = Color(0xFF989898)
private val RankNumGray = Color(0xFF616161)
private val CopyBg      = Color(0xFFE0E0E0)

// 외곽 패딩 스펙
private val HorizontalPad = 20.dp
private val VerticalPad   = 48.dp


// ───────────────── Screen ─────────────────
@Composable
fun FriendAddScreen(
    myCode: String = "D21MMC1",
    foundFriend: FriendUi? = null,
    isAdded: Boolean = false,
    loading: Boolean = false,                     // ⬅️ 추가
    onBack: () -> Unit = {},
    onSearch: (String) -> Unit = { _ -> },
    onAddFriend: (FriendUi) -> Unit = {},
    onViewRank: () -> Unit = {},
    onCopyMyCode: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp) // ⬅️ 선택
) {
    var codeInput by remember { mutableStateOf("") }
    var showCopied by remember { mutableStateOf(false) }
    var showAddedToast by remember { mutableStateOf(false) }
    // yw- 검색 누르고 인지를 위한 변수 생성
    var searchPressed by remember { mutableStateOf(false) }

    // isAdded 변경 시 하단 토스트 노출
    LaunchedEffect(isAdded) {
        if (isAdded) {
            showAddedToast = true
            delay(1500)
            showAddedToast = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HorizontalPad, vertical = VerticalPad)
                .fillMaxWidth()
        ) {
            // ───── 헤더: 좌측 back, 중앙 타이틀 ─────
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "뒤로가기",
                        tint = Color.Unspecified
                    )
                }
                Text(
                    text = "친구 추가",
                    fontFamily = Pretendard,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // ───── 친구 코드 입력 (밑줄만 있는 스타일) ─────
            TextField(
                value = codeInput,
                onValueChange = { codeInput = it },
                placeholder = {
                    Text(
                        "친구 코드 입력",
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF989898)
                    )
                },
                singleLine = true,
                enabled = !loading,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (codeInput.isNotBlank()) {
                            onSearch(codeInput)
                            searchPressed = true   // 검색 후 X로 전환
                        }
                    }
                ),

                // yw- 돋보기 아이콘 추가 및 기능 구현 -> 사랑해요...
                // 기본 상태 돋보기였다가 검색을 누르면 x로 전환 x로 누르면 다시 돋보기 아이콘으로
                trailingIcon = {
                    if (!searchPressed) {
                        IconButton(
                            onClick = {
                                if (codeInput.isNotBlank()) {
                                    onSearch(codeInput)
                                    searchPressed = true   //  검색 후 X로 전환
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_friendsearch_new01),
                                contentDescription = "검색",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                codeInput = ""
                                searchPressed = false   // 다시 돋보기로 복귀
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_frienddelet_new01), // X 아이콘
                                contentDescription = "삭제",
                                modifier = Modifier.size(22.dp),
                                tint = Color.Black
                            )
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = BrandBlue,
                    unfocusedIndicatorColor = Color.Black,
                    disabledIndicatorColor = Color.Black,
                    cursorColor = BrandBlue,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // ───── 내 코드 박스 ─────
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF2F5FA),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "내 코드",
                        fontFamily = Pretendard,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = myCode,
                        fontFamily = Pretendard,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            onCopyMyCode(myCode)
                            showCopied = true
                        },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_copy),
                            contentDescription = "복사",
                            tint = Color.Unspecified
                        )
                    }
                }
            }

            // ───── 친구 검색 결과 ─────
            if (foundFriend != null) {
                Spacer(Modifier.height(32.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Avatar(
                        size = 86.dp,
                        border = BorderStroke(0.dp, Color.Transparent),
                        painterRes = foundFriend.avatarRes
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = foundFriend.name,
                        fontFamily = Pretendard,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(16.dp))

                    if (!isAdded) {
                        Button(
                            onClick = { onAddFriend(foundFriend) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier
                                .height(48.dp)
                                .width(160.dp)
                        ) {
                            Text(
                                text = "친구 추가",
                                fontFamily = Pretendard,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = onViewRank,
                            shape = MaterialTheme.shapes.extraLarge,
                            border = BorderStroke(1.dp, BrandBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = BrandBlue
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .width(180.dp)
                        ) {
                            Text(
                                text = "친구순위보기",
                                fontFamily = Pretendard,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp)) // 마지막 여백
        }

        // ───── 하단 토스트: 복사되었습니다 ─────
        if (showCopied) {
            LaunchedEffect(Unit) {
                delay(1500)
                showCopied = false
            }
            BottomToast(
                iconRes = R.drawable.ic_copy,
                text = "복사되었습니다"
            )
        }

        // ───── 하단 토스트: 추가되었습니다 ─────
        if (showAddedToast) {
            BottomToast(
                iconRes = R.drawable.ic_people,
                text = "추가되었습니다",
                textColor = RankNumGray
            )
        }
    }
}

// ───────────────── Bottom toast ─────────────────
@Composable
fun BoxScope.BottomToast(
    @DrawableRes iconRes: Int,
    text: String,
    textColor: Color = Color.Black
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CopyBg,
        shadowElevation = 0.dp,
        modifier = Modifier
            .align(Alignment.BottomCenter)  // ⬅️ 이제 정상 작동
            .padding(bottom = VerticalPad)  // 하단에서 48dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = Color.Unspecified)
            Spacer(Modifier.width(8.dp))
            Text(text = text, color = textColor)
        }
    }
}

// ───────────────── Avatar ─────────────────
@Composable
private fun Avatar(
    size: Dp,
    border: BorderStroke? = null,
    @DrawableRes painterRes: Int? = null
) {
    val isPreview = LocalInspectionMode.current
    val shape = CircleShape

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .then(if (border != null) Modifier.border(border, shape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (painterRes != null) {
            Image(
                painter = painterResource(id = painterRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEFF4FB)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isPreview) android.R.drawable.ic_menu_help else android.R.drawable.ic_menu_help
                    ),
                    contentDescription = null,
                    tint = BrandBlue
                )
            }
        }
    }
}

// ───────────────── Previews ─────────────────
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PreviewFriendAdd_Empty() {
    MaterialTheme { Surface { FriendAddScreen() } }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "검색 결과")
@Composable
private fun PreviewFriendAdd_Found() {
    MaterialTheme {
        Surface {
            FriendAddScreen(
                foundFriend = FriendUi(code = "M3M8CHI2", name = "송뭉치")
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "추가 완료")
@Composable
private fun PreviewFriendAdd_Added() {
    MaterialTheme {
        Surface {
            FriendAddScreen(
                foundFriend = FriendUi(code = "M3M8CHI2", name = "송뭉치"),
                isAdded = true
            )
        }
    }
}
