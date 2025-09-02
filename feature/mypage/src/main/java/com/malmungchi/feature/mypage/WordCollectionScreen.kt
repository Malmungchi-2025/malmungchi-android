package com.malmungchi.feature.mypage

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.mypage.R as MyPageR

// ===== Colors =====
private val Gray_616161 = Color(0xFF616161)
private val PillBg_F7F7F7 = Color(0xFFF7F7F7)
private val CardBg_F7F7F7 = Color(0xFFF7F7F7)
private val DisabledText_C9CAD4 = Color(0xFFC9CAD4)
private val Yellow_FFCB2B = Color(0xFFFFCB2B) // 별 채움 시 참고(아이콘 틴트용)

// ===== Dimens =====
private val ScreenPadding = 20.dp
private val CardCorner = 16.dp
private val ItemGap = 12.dp

// ===== Model (추후 API 연동 시 이 데이터로 바인딩) =====
data class WordItem(
    val id: String,
    val title: String,
    val description: String,
    val example: String,
    val isFavorite: Boolean
)

// ===== Public Screen =====
@Composable
fun WordCollectionScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onToggleFilterFavorite: (Boolean) -> Unit = {},   // "즐겨찾기" 필터 토글 (나중에 목록 필터용)
    filterFavoriteOnly: Boolean = false,              // 즐겨찾기 필터 상태
    items: List<WordItem>                              // 목록 데이터(API 바인딩 대상)
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(12.dp))
        WordCollectionTopBar(
            title = "단어 수집함",
            onBack = onBack,
            favoriteFilterOn = filterFavoriteOnly,
            onToggleFavoriteFilter = { onToggleFilterFavorite(!filterFavoriteOnly) }
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp) // 카드 사이 간격
        ) {
            items(items, key = { it.id }) { item ->
                WordCard(
                    item = item,
                    onClick = { /* TODO: 상세로 이동이 필요하면 여기서 처리 */ }
                )
            }
        }
    }
}

// ===== TopBar =====
@Composable
private fun WordCollectionTopBar(
    title: String,
    onBack: () -> Unit,
    favoriteFilterOn: Boolean,
    onToggleFavoriteFilter: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Back
        Row(
            modifier = Modifier.align(Alignment.CenterStart)
                .size(40.dp) // 터치 영역 확보
                .clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = MyPageR.drawable.ic_back),
                contentDescription = "뒤로",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        // Title (Pretendard 20, SemiBold)
        Text(
            text = title,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // 즐겨찾기 Pill (우측)
        FavoritePill(
            modifier = Modifier.align(Alignment.CenterEnd),
            enabled = favoriteFilterOn,              // on = 활성 색(별 채움), off = 비활성
            onClick = onToggleFavoriteFilter
        )
    }
}

@Composable
private fun FavoritePill(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(PillBg_F7F7F7)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (enabled) MyPageR.drawable.ic_favor else MyPageR.drawable.ic_favor_null
            ),
            contentDescription = "즐겨찾기 필터",
            tint = if (enabled) Yellow_FFCB2B else DisabledText_C9CAD4,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "즐겨찾기",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = if (enabled) MaterialTheme.colorScheme.onBackground else DisabledText_C9CAD4
            )
        )
    }
}

// ===== Item Card =====
@Composable
private fun WordCard(
    item: WordItem,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg_F7F7F7),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(CardCorner),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 상단: 좌측 별 + 제목
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 좌측 즐겨찾기 표시(서버 값 기반)
                Icon(
                    painter = painterResource(
                        id = if (item.isFavorite) MyPageR.drawable.ic_favor else MyPageR.drawable.ic_favor_null
                    ),
                    contentDescription = "즐겨찾기",
                    tint = if (item.isFavorite) Yellow_FFCB2B else DisabledText_C9CAD4,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.title,
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(ItemGap))

            // 설명(14, 미디엄)
            Text(
                text = item.description,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )

            Spacer(Modifier.height(8.dp))

            // 예문(12, 미디엄, #616161)
            Text(
                text = item.example,
                style = TextStyle(
                    fontFamily = Pretendard,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Gray_616161
                )
            )
        }
    }
}

// ===== Preview =====
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun WordCollectionScreenPreview() {
    val demo = listOf(
        WordItem(
            id = "1",
            title = "말뭉치",
            description = "언어 연구를 위해 텍스트를 컴퓨터가 읽을 수 있는 형태로 모아 놓은 언어 자료.",
            example = "예문) 어휘력, 문해력을 키우기 위해서는 ‘말뭉치’ 사용이 필수적이다.",
            isFavorite = true
        ),
        WordItem(
            id = "2",
            title = "고찰",
            description = "깊이 생각하고 살펴봄.",
            example = "예문) 그는 문제의 본질을 고찰한 끝에 새로운 해결책을 제시했다.",
            isFavorite = false
        )
    )

    MaterialTheme {
        WordCollectionScreen(
            onBack = {},
            filterFavoriteOnly = false,
            onToggleFilterFavorite = {},
            items = demo
        )
    }
}