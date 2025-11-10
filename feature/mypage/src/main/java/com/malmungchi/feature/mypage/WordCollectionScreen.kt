package com.malmungchi.feature.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.core.model.VocabularyDto
import com.malmungchi.feature.mypage.R as MyPageR
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.snapshotFlow

// ===== Colors =====
private val Gray_616161 = Color(0xFF616161)
private val PillBg_F7F7F7 = Color(0xFFF7F7F7)
private val CardBg_F7F7F7 = Color(0xFFF7F7F7)
private val DisabledText_C9CAD4 = Color(0xFFC9CAD4)
private val Yellow_FFCB2B = Color(0xFFFFCB2B)
private val Text_262626 = Color(0xFF262626)

// ===== Dimens =====
private val ScreenPadding = 20.dp
private val CardCorner = 16.dp
private val ItemGap = 12.dp

// ===== Model (UI 바인딩용) =====
data class WordItem(
    val id: String,
    val title: String,
    val description: String,
    val example: String,
    val isFavorite: Boolean
)

/* ========================================================
 * Route: VM과 연결되는 진입 컴포저블
 *  - 네비게이션에선 이 컴포저블을 사용하세요.
 * ======================================================*/
@Composable
fun WordCollectionRoute(
    viewModel: MyPageViewModel,
    //viewModel: MyPageViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val ui by viewModel.ui.collectAsState()

    // 즐겨찾기 필터 상태
    var favoriteOnly by remember { mutableStateOf(false) }

    // 보여줄 리스트 소스 (필터에 따라 전환)
    val items: List<WordItem> = remember(ui.allVocab, ui.likedVocab, favoriteOnly) {
        val source = if (favoriteOnly) ui.likedVocab else ui.allVocab
        source.map { it.toWordItem() }
    }

    // 무한 스크롤
    val listState = rememberLazyListState()
    val canLoadMore = if (favoriteOnly) ui.likedCursor != null else ui.allCursor != null

    // 리스트의 마지막 가시 아이템 인덱스를 관찰 → 끝 근처면 페이징
    LaunchedEffect(favoriteOnly, canLoadMore, items.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .distinctUntilChanged()
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                if (canLoadMore && total > 0 && lastVisible >= total - 3) {
                    if (favoriteOnly) viewModel.loadMoreLiked() else viewModel.loadMoreAll()
                }
            }
    }

    WordCollectionScreen(
        onBack = onBack,
        filterFavoriteOnly = favoriteOnly,
        onToggleFilterFavorite = { favoriteOnly = it },
        items = items,
        listState = listState,
        onToggleLike = { idStr, wantLike ->
            val id = idStr.toIntOrNull() ?: return@WordCollectionScreen
            viewModel.toggleLike(id, wantLike)
        }
    )
}

// ===== Public Screen (UI) =====
@Composable
fun WordCollectionScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onToggleFilterFavorite: (Boolean) -> Unit = {},
    filterFavoriteOnly: Boolean = false,
    items: List<WordItem>,
    listState: LazyListState = rememberLazyListState(),
    onToggleLike: (id: String, wantLike: Boolean) -> Unit = { _, _ -> }
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(48.dp))
        WordCollectionTopBar(
            title = "단어 수집함",
            onBack = onBack
        )

        Spacer(Modifier.height(16.dp))

        // ⭐ 즐겨찾기 Pill을 오른쪽 끝에 배치
        Box(modifier = Modifier.fillMaxWidth()) {
            FavoritePill(
                enabled = filterFavoriteOnly,
                onClick = { onToggleFilterFavorite(!filterFavoriteOnly) },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items, key = { it.id }) { item ->
                WordCard(
                    item = item,
                    onClick = { /* TODO: 상세 이동 */ },
                    onToggleLike = onToggleLike
                )
            }
        }
    }
}

// ===== TopBar =====
@Composable
private fun WordCollectionTopBar(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
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
                color = if (enabled) Text_262626 else DisabledText_C9CAD4 // 활성 시 262626
            )
        )
    }
}

// ===== Item Card =====
@Composable
private fun WordCard(
    item: WordItem,
    onClick: () -> Unit,
    onToggleLike: (id: String, wantLike: Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg_F7F7F7),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 6.dp
        ),
        shape = RoundedCornerShape(CardCorner),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 상단: 좌측 별 + 제목
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(
                        id = if (item.isFavorite) MyPageR.drawable.ic_favor else MyPageR.drawable.ic_favor_null
                    ),
                    contentDescription = if (item.isFavorite) "즐겨찾기 해제" else "즐겨찾기 등록",
                    tint = if (item.isFavorite) Yellow_FFCB2B else DisabledText_C9CAD4,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable {
                            onToggleLike(item.id, !item.isFavorite)
                        }
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

/* =========================
 * DTO → UI 매핑
 *  - 서버 응답 필드가 Nullable이므로 안전 처리
 * =======================*/
private fun VocabularyDto.toWordItem(): WordItem = WordItem(
    id = this.id?.toString() ?: "",         // id nullable → 빈 문자열 대체
    title = this.word,
    description = this.meaning,
    example = this.example ?: "",
    isFavorite = this.isLiked ?: false      // Boolean? → Boolean
)

// ===== Preview (UI만 검증용) =====
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





//package com.malmungchi.feature.mypage
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.tooling.preview.Preview
//import com.malmungchi.core.designsystem.Pretendard
//import com.malmungchi.feature.mypage.R as MyPageR
//
//// ===== Colors =====
//private val Gray_616161 = Color(0xFF616161)
//private val PillBg_F7F7F7 = Color(0xFFF7F7F7)
//private val CardBg_F7F7F7 = Color(0xFFF7F7F7)
//private val DisabledText_C9CAD4 = Color(0xFFC9CAD4)
//private val Yellow_FFCB2B = Color(0xFFFFCB2B) // 별 채움 시 참고(아이콘 틴트용)
//private val Text_262626 = Color(0xFF262626)
//// ===== Dimens =====
//private val ScreenPadding = 20.dp
//private val CardCorner = 16.dp
//private val ItemGap = 12.dp
//
//// ===== Model (추후 API 연동 시 이 데이터로 바인딩) =====
//data class WordItem(
//    val id: String,
//    val title: String,
//    val description: String,
//    val example: String,
//    val isFavorite: Boolean
//)
//
//// ===== Public Screen =====
//@Composable
//fun WordCollectionScreen(
//    modifier: Modifier = Modifier,
//    onBack: () -> Unit = {},
//    onToggleFilterFavorite: (Boolean) -> Unit = {},
//    filterFavoriteOnly: Boolean = false,
//    items: List<WordItem>
//) {
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(horizontal = ScreenPadding)
//    ) {
//        Spacer(Modifier.height(48.dp))
//        WordCollectionTopBar(
//            title = "단어 수집함",
//            onBack = onBack
//        )
//
//        Spacer(Modifier.height(16.dp))
//
//        // ⭐ 즐겨찾기 Pill을 오른쪽 끝에 배치
//        Box(modifier = Modifier.fillMaxWidth()) {
//            FavoritePill(
//                enabled = filterFavoriteOnly,
//                onClick = { onToggleFilterFavorite(!filterFavoriteOnly) },
//                modifier = Modifier
//                    .align(Alignment.CenterEnd)   // 오른쪽 정렬
//                    .padding(end = 20.dp)         // 오른쪽 여백 20
//            )
//        }
//
//        Spacer(Modifier.height(24.dp))
//
//        LazyColumn(
//            contentPadding = PaddingValues(bottom = 24.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            items(items, key = { it.id }) { item ->
//                WordCard(
//                    item = item,
//                    onClick = { /* TODO: 상세 이동 */ }
//                )
//            }
//        }
//    }
//}
//
//// ===== TopBar =====
//@Composable
//private fun WordCollectionTopBar(
//    title: String,
//    onBack: () -> Unit
//) {
//    Box(
//        modifier = Modifier.fillMaxWidth(),
//        contentAlignment = Alignment.Center
//    ) {
//        Row(
//            modifier = Modifier
//                .align(Alignment.CenterStart)
//                .size(40.dp)
//                .clickable { onBack() },
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Icon(
//                painter = painterResource(id = MyPageR.drawable.ic_back),
//                contentDescription = "뒤로",
//                tint = MaterialTheme.colorScheme.onBackground
//            )
//        }
//
//        Text(
//            text = title,
//            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.colorScheme.onBackground
//            ),
//            modifier = Modifier.fillMaxWidth()
//        )
//    }
//}
//
//@Composable
//private fun FavoritePill(
//    modifier: Modifier = Modifier,
//    enabled: Boolean,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = modifier
//            .clip(RoundedCornerShape(999.dp))
//            .background(PillBg_F7F7F7)
//            .clickable { onClick() }
//            .padding(horizontal = 14.dp, vertical = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Icon(
//            painter = painterResource(
//                id = if (enabled) MyPageR.drawable.ic_favor else MyPageR.drawable.ic_favor_null
//            ),
//            contentDescription = "즐겨찾기 필터",
//            tint = if (enabled) Yellow_FFCB2B else DisabledText_C9CAD4,
//            modifier = Modifier.size(18.dp)
//        )
//        Spacer(Modifier.width(6.dp))
//        Text(
//            text = "즐겨찾기",
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                fontSize = 16.sp,
//                color = if (enabled) MaterialTheme.colorScheme.onBackground else DisabledText_C9CAD4
//            )
//        )
//    }
//}
//
//// ===== Item Card =====
//@Composable
//private fun WordCard(
//    item: WordItem,
//    onClick: () -> Unit
//) {
//    Card(
//        colors = CardDefaults.cardColors(containerColor = CardBg_F7F7F7),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = 4.dp,    // ✅ 살짝 그림자 추가
//            pressedElevation = 6.dp     // 눌렀을 때 더 강조
//        ),
//        shape = RoundedCornerShape(CardCorner),
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//
//            // 상단: 좌측 별 + 제목
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    painter = painterResource(
//                        id = if (item.isFavorite) MyPageR.drawable.ic_favor else MyPageR.drawable.ic_favor_null
//                    ),
//                    contentDescription = "즐겨찾기",
//                    tint = if (item.isFavorite) Yellow_FFCB2B else DisabledText_C9CAD4,
//                    modifier = Modifier.size(18.dp)
//                )
//                Spacer(Modifier.width(8.dp))
//                Text(
//                    text = item.title,
//                    style = TextStyle(
//                        fontFamily = Pretendard,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 16.sp,
//                        color = MaterialTheme.colorScheme.onBackground
//                    ),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(Modifier.height(ItemGap))
//
//            Text(
//                text = item.description,
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 14.sp,
//                    lineHeight = 22.sp,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            )
//
//            Spacer(Modifier.height(8.dp))
//
//            Text(
//                text = item.example,
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 12.sp,
//                    lineHeight = 18.sp,
//                    color = Gray_616161
//                )
//            )
//        }
//    }
//}
//// ===== Preview =====
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun WordCollectionScreenPreview() {
//    val demo = listOf(
//        WordItem(
//            id = "1",
//            title = "말뭉치",
//            description = "언어 연구를 위해 텍스트를 컴퓨터가 읽을 수 있는 형태로 모아 놓은 언어 자료.",
//            example = "예문) 어휘력, 문해력을 키우기 위해서는 ‘말뭉치’ 사용이 필수적이다.",
//            isFavorite = true
//        ),
//        WordItem(
//            id = "2",
//            title = "고찰",
//            description = "깊이 생각하고 살펴봄.",
//            example = "예문) 그는 문제의 본질을 고찰한 끝에 새로운 해결책을 제시했다.",
//            isFavorite = false
//        )
//    )
//
//    MaterialTheme {
//        WordCollectionScreen(
//            onBack = {},
//            filterFavoriteOnly = false,
//            onToggleFilterFavorite = {},
//            items = demo
//        )
//    }
//}