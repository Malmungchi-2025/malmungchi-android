package com.malmungchi.feature.friend

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.friend.R

private val BrandBlue   = Color(0xFF195FCF)
private val GrayBgChip  = Color(0xFFF7F7F7)
private val GrayTextSub = Color(0xFF989898)
private val RankNumGray = Color(0xFF616161)
private val Gold        = Color(0xFFFFD91C)

private val HorizontalPad = 20.dp
private val VerticalPad   = 48.dp

@Composable
fun FriendScreen(
    onAddFriend: () -> Unit = {},
    tab: RankTab,
    onSelectFriendTab: () -> Unit,
    onSelectAllTab: () -> Unit,
    ranks: List<FriendRank>,
    loading: Boolean
) {
    // ─────────────────────────────────────────
    // Top3: 실제 데이터가 있으면 보여주고, 부족한 칸만 플레이스홀더로 채움
    // ─────────────────────────────────────────
    val top3ForUi: List<FriendRank> = buildTop3Padded(ranks)

    // 4위부터: 항상 4, 5등은 채워주기
    val listRows: List<FriendRank> = ranks.drop(3)
//    val base = ranks.drop(3) // 실제 4위 이후
//    val needPlaceholders = (4..5).map { targetRank ->
//        val hasThisIndex = (targetRank - 4) < base.size
//        if (hasThisIndex) null else placeholderRank(targetRank)
//    }.filterNotNull()
//
//    val listRows: List<FriendRank> = buildList {
//        addAll(base)
//        addAll(needPlaceholders)
//    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(
            start = HorizontalPad,
            end = HorizontalPad,
            top = VerticalPad,
            bottom = VerticalPad
        )
    ) {
        // 헤더
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onAddFriend,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_friend_add),
                        contentDescription = "친구 추가",
                        tint = Color.Unspecified
                    )
                }
                Text(
                    text = "친구목록",
                    fontFamily = Pretendard,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }

        // 탭
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RankTabChip(
                    label = "친구 순위",
                    selected = tab == RankTab.FRIEND,
                    onClick = onSelectFriendTab
                )
                Spacer(Modifier.width(8.dp))
                RankTabChip(
                    label = "전체 순위",
                    selected = tab == RankTab.ALL,
                    onClick = onSelectAllTab
                )
            }
        }

        item { Spacer(Modifier.height(32.dp)) }

        if (loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            // Top3 (은·금·동)
            item {
                Top3RowSilverGoldBronze(items = top3ForUi)
            }

            item { Spacer(Modifier.height(20.dp)) }

            // 4위 이후
            items(
                items = listRows,
                key = { item -> item.rank }   // 👈 추가: rank나 id 등 고유값
            ) { item ->
                RankRow(item = item)
                Spacer(Modifier.height(10.dp))
            }
//            items(listRows) { item ->
//                RankRow(item = item)
//                Spacer(Modifier.height(10.dp))
//            }
        }
    }
}

// ────────────────────────────────
// 유틸: Top3 채우기
// ────────────────────────────────
private fun buildTop3Padded(ranks: List<FriendRank>): List<FriendRank> {
    val top = ranks.take(3).toMutableList()
    while (top.size < 3) {
        top += placeholderRank(top.size + 1)
    }
    return top
}

// ────────────────────────────────
// 컴포저블들
// ────────────────────────────────
@Composable
private fun RankTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) BrandBlue else GrayBgChip,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(30.dp)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() }
        ) {
            Text(
                text = label,
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else GrayTextSub
            )
        }
    }
}

@Composable
private fun Top3RowSilverGoldBronze(items: List<FriendRank>) {
    // 은, 금, 동 순서로 재배치
    val safe = listOf(
        items.getOrElse(1) { placeholderRank(2) },
        items.getOrElse(0) { placeholderRank(1) },
        items.getOrElse(2) { placeholderRank(3) }
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Box(Modifier.weight(1f).offset(y = 10.dp), contentAlignment = Alignment.TopCenter) {
            Top3Item(item = safe[0], raise = false) // 은
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
            Top3Item(item = safe[1], raise = true)  // 금
        }
        Box(Modifier.weight(1f).offset(y = 10.dp), contentAlignment = Alignment.TopCenter) {
            Top3Item(item = safe[2], raise = false) // 동
        }
    }
}

@Composable
private fun Top3Item(item: FriendRank, raise: Boolean) {
    val borderColor = if (item.rank == 1) Gold else RankNumGray

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (raise) Modifier.offset(y = (-10).dp) else Modifier
    ) {
        Box(
            modifier = Modifier.size(75.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            AvatarThumb(
                size = 75.dp,
                border = BorderStroke(2.dp, borderColor),
                painterRes = item.avatarRes
            )
            if (item.rank == 1) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_crown),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(26.dp)
                        .offset(y = (-12).dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.name,
            fontFamily = Pretendard,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "${item.points}P",
            fontFamily = Pretendard,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = GrayTextSub
        )
    }
}

@Composable
private fun RankRow(item: FriendRank) {
    val outline =
        if (item.isMe) BorderStroke(2.dp, BrandBlue) else BorderStroke(1.dp, Color(0x14000000))

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        border = outline,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.rank.toString(),
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = RankNumGray
            )
            Spacer(Modifier.width(12.dp))
            AvatarThumb(size = 44.dp, painterRes = item.avatarRes)
            Spacer(Modifier.width(12.dp))
            Text(
                text = item.name,
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${item.points}P",
                fontFamily = Pretendard,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GrayTextSub
            )
        }
    }
}

@Composable
private fun AvatarThumb(
    size: Dp,
    border: BorderStroke? = null,
    @DrawableRes painterRes: Int? = null
) {
    val isPreview = LocalInspectionMode.current
    val shape = CircleShape
    // ✅ 추가: painterResource 캐싱
    // ✅ painterResource 호출은 remember 밖으로 분리
    val painter = if (painterRes != null) painterResource(painterRes) else null

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

// ────────────────────────────────
// 플레이스홀더 유틸
// ────────────────────────────────
private fun placeholderRank(rank: Int) = FriendRank(
    rank = rank,
    name = "말뭉치",
    points = 0,
    isMe = false,
    avatarRes = null
)

private fun placeholderMe(rank: Int) = FriendRank(
    rank = rank,
    name = "말뭉치",
    points = 0,
    isMe = true,
    avatarRes = null
)

// ── 프리뷰
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "친구 탭(친구 없음)")
@Composable
private fun PreviewFriendScreenEmpty() {
    MaterialTheme {
        Surface {
            FriendScreen(
                onAddFriend = {},
                tab = RankTab.FRIEND,
                onSelectFriendTab = {},
                onSelectAllTab = {},
                ranks = emptyList(), // 친구 없음
                loading = false
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "친구 탭(친구 5명)")
@Composable
private fun PreviewFriendScreenFriend() {
    val dummy = listOf(
        FriendRank(1, "금동동", 2149, false, null),
        FriendRank(2, "은동동", 2019, false, null),
        FriendRank(3, "동동동", 2012, false, null),
        FriendRank(4, "박뭉치", 1800, false, null),
        FriendRank(5, "김뭉치", 1700, true, null)
    )
    MaterialTheme {
        Surface {
            FriendScreen(
                onAddFriend = {},
                tab = RankTab.FRIEND,
                onSelectFriendTab = {},
                onSelectAllTab = {},
                ranks = dummy,
                loading = false
            )
        }
    }
}

//package com.malmungchi.feature.friend
//
//
//import androidx.annotation.DrawableRes
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalInspectionMode
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.malmungchi.core.designsystem.Pretendard
//import com.malmungchi.feature.friend.R
//
//private val BrandBlue   = Color(0xFF195FCF)
//private val GrayBgChip  = Color(0xFFF7F7F7)
//private val GrayTextSub = Color(0xFF989898)
//private val RankNumGray = Color(0xFF616161)
//private val Gold        = Color(0xFFFFD91C)
//
//private val HorizontalPad = 20.dp
//private val VerticalPad   = 48.dp
//
//@Composable
//fun FriendScreen(
//    onAddFriend: () -> Unit = {},
//    tab: RankTab,
//    onSelectFriendTab: () -> Unit,
//    onSelectAllTab: () -> Unit,
//    ranks: List<FriendRank>,
//    loading: Boolean
//) {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White),
//        contentPadding = PaddingValues(
//            start = HorizontalPad,
//            end = HorizontalPad,
//            top = VerticalPad,
//            bottom = VerticalPad
//        )
//    ) {
//        // 헤더
//        item {
//            Box(
//                modifier = Modifier.fillMaxWidth(),
//                contentAlignment = Alignment.Center
//            ) {
//                IconButton(
//                    onClick = onAddFriend,
//                    modifier = Modifier.align(Alignment.CenterStart)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.ic_friend_add),
//                        contentDescription = "친구 추가",
//                        tint = Color.Unspecified
//                    )
//                }
//                Text(
//                    text = "친구목록",
//                    fontFamily = Pretendard,
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    color = Color.Black
//                )
//            }
//        }
//
//        item { Spacer(Modifier.height(24.dp)) }
//
//        // 탭
//        item {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                RankTabChip(
//                    label = "친구 순위",
//                    selected = tab == RankTab.FRIEND,
//                    onClick = onSelectFriendTab
//                )
//                Spacer(Modifier.width(8.dp))
//                RankTabChip(
//                    label = "전체 순위",
//                    selected = tab == RankTab.ALL,
//                    onClick = onSelectAllTab
//                )
//            }
//        }
//
//        item { Spacer(Modifier.height(32.dp)) }
//
//        if (loading) {
//            item {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 40.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            }
//        } else {
//            // Top3
//            item {
//                val top3 = ranks.take(3)
//                Top3RowSilverGoldBronze(items = top3)
//            }
//
//            item { Spacer(Modifier.height(20.dp)) }
//
//            // 4위 이후
//            items(ranks.drop(3)) { item ->
//                RankRow(item = item)
//                Spacer(Modifier.height(10.dp))
//            }
//        }
//    }
//}
//
//@Composable
//private fun RankTabChip(
//    label: String,
//    selected: Boolean,
//    onClick: () -> Unit
//) {
//    Surface(
//        shape = RoundedCornerShape(16.dp),
//        color = if (selected) BrandBlue else GrayBgChip,
//        shadowElevation = 0.dp,
//        tonalElevation = 0.dp
//    ) {
//        Box(
//            contentAlignment = Alignment.Center,
//            modifier = Modifier
//                .height(30.dp)
//                .padding(horizontal = 12.dp)
//                .clip(RoundedCornerShape(16.dp))
//                .clickable { onClick() }
//        ) {
//            Text(
//                text = label,
//                fontFamily = Pretendard,
//                fontSize = 12.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = if (selected) Color.White else GrayTextSub
//            )
//        }
//    }
//}
//
//@Composable
//private fun Top3RowSilverGoldBronze(items: List<FriendRank>) {
//    val safe = if (items.size >= 3) listOf(items[1], items[0], items[2]) else items
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.Top
//    ) {
//        Box(Modifier.weight(1f).offset(y = 10.dp), contentAlignment = Alignment.TopCenter) {
//            Top3Item(item = safe.getOrNull(0), raise = false)
//        }
//        Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
//            Top3Item(item = safe.getOrNull(1), raise = true)
//        }
//        Box(Modifier.weight(1f).offset(y = 10.dp), contentAlignment = Alignment.TopCenter) {
//            Top3Item(item = safe.getOrNull(2), raise = false)
//        }
//    }
//}
//
//@Composable
//private fun Top3Item(item: FriendRank?, raise: Boolean) {
//    if (item == null) return
//    val borderColor = if (item.rank == 1) Gold else RankNumGray
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = if (raise) Modifier.offset(y = (-10).dp) else Modifier
//    ) {
//        Box(
//            modifier = Modifier.size(75.dp),
//            contentAlignment = Alignment.TopCenter
//        ) {
//            AvatarThumb(
//                size = 75.dp,
//                border = BorderStroke(2.dp, borderColor),
//                painterRes = item.avatarRes
//            )
//            if (item.rank == 1) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_crown),
//                    contentDescription = null,
//                    tint = Color.Unspecified,
//                    modifier = Modifier
//                        .size(26.dp)
//                        .offset(y = (-12).dp)
//                )
//            }
//        }
//        Spacer(Modifier.height(8.dp))
//        Text(
//            text = item.name,
//            fontFamily = Pretendard,
//            fontSize = 16.sp,
//            fontWeight = FontWeight.Medium,
//            color = Color.Black,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//        Spacer(Modifier.height(2.dp))
//        Text(
//            text = "${item.points}P",
//            fontFamily = Pretendard,
//            fontSize = 12.sp,
//            fontWeight = FontWeight.Medium,
//            color = GrayTextSub
//        )
//    }
//}
//
//@Composable
//private fun RankRow(item: FriendRank) {
//    val outline =
//        if (item.isMe) BorderStroke(2.dp, BrandBlue) else BorderStroke(1.dp, Color(0x14000000))
//
//    Surface(
//        shape = RoundedCornerShape(12.dp),
//        color = Color.White,
//        tonalElevation = 0.dp,
//        shadowElevation = 2.dp,
//        border = outline,
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 12.dp, vertical = 10.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = item.rank.toString(),
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = RankNumGray
//            )
//            Spacer(Modifier.width(12.dp))
//            AvatarThumb(size = 44.dp, painterRes = item.avatarRes)
//            Spacer(Modifier.width(12.dp))
//            Text(
//                text = item.name,
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                modifier = Modifier.weight(1f)
//            )
//            Text(
//                text = "${item.points}P",
//                fontFamily = Pretendard,
//                fontSize = 12.sp,
//                fontWeight = FontWeight.Medium,
//                color = GrayTextSub
//            )
//        }
//    }
//}
//
//@Composable
//private fun AvatarThumb(
//    size: Dp,
//    border: BorderStroke? = null,
//    @DrawableRes painterRes: Int? = null
//) {
//    val isPreview = LocalInspectionMode.current
//    val shape = CircleShape
//
//    Box(
//        modifier = Modifier
//            .size(size)
//            .clip(shape)
//            .then(if (border != null) Modifier.border(border, shape) else Modifier),
//        contentAlignment = Alignment.Center
//    ) {
//        if (painterRes != null) {
//            Image(
//                painter = painterResource(id = painterRes),
//                contentDescription = null,
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.fillMaxSize()
//            )
//        } else {
//            Box(
//                Modifier
//                    .fillMaxSize()
//                    .background(Color(0xFFEFF4FB)),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    painter = painterResource(
//                        id = if (isPreview) android.R.drawable.ic_menu_help else android.R.drawable.ic_menu_help
//                    ),
//                    contentDescription = null,
//                    tint = BrandBlue
//                )
//            }
//        }
//    }
//}
//
//// ── 프리뷰(더미 데이터로 렌더)
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, name = "친구 탭")
//@Composable
//private fun PreviewFriendScreenFriend() {
//    val dummy = listOf(
//        FriendRank(1, "금동동", 2149, false, null),
//        FriendRank(2, "은동동", 2019, false, null),
//        FriendRank(3, "동동동", 2012, false, null),
//        FriendRank(4, "박뭉치", 1800, false, null),
//        FriendRank(5, "김뭉치", 1700, true, null)
//    )
//    MaterialTheme {
//        Surface {
//            FriendScreen(
//                onAddFriend = {},
//                tab = RankTab.FRIEND,
//                onSelectFriendTab = {},
//                onSelectAllTab = {},
//                ranks = dummy,
//                loading = false
//            )
//        }
//    }
//}
//
////import androidx.annotation.DrawableRes
////import androidx.compose.foundation.BorderStroke
////import androidx.compose.foundation.Image
////import androidx.compose.foundation.background
////import androidx.compose.foundation.border
////import androidx.compose.foundation.clickable
////import androidx.compose.foundation.interaction.MutableInteractionSource
////import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.lazy.LazyColumn
////import androidx.compose.foundation.lazy.items
////import androidx.compose.foundation.shape.CircleShape
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material3.Icon
////import androidx.compose.material3.IconButton
////import androidx.compose.material3.MaterialTheme
////import androidx.compose.material3.Surface
////import androidx.compose.material3.Text
////import androidx.compose.runtime.*
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.composed
////import androidx.compose.ui.draw.clip
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.layout.ContentScale
////import androidx.compose.ui.platform.LocalInspectionMode
////import androidx.compose.ui.res.painterResource
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.text.style.TextOverflow
////import androidx.compose.ui.tooling.preview.Preview
////import androidx.compose.ui.unit.Dp
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import androidx.hilt.navigation.compose.hiltViewModel
////import com.malmungchi.core.designsystem.Pretendard
////import com.malmungchi.feature.friend.R
////
////// ───────────────── Color Tokens ─────────────────
////private val BrandBlue   = Color(0xFF195FCF)
////private val GrayBgChip  = Color(0xFFF7F7F7)
////private val GrayTextSub = Color(0xFF989898)
////private val RankNumGray = Color(0xFF616161)
////private val Gold        = Color(0xFFFFD91C)
////
////// 외곽 패딩
////private val HorizontalPad = 20.dp
////private val VerticalPad   = 48.dp
////
////// ───────────────── Data ─────────────────
////
////
////// 탭
////private enum class FriendTab { FRIEND, ALL }
////
////// ───────────────── Screen ─────────────────
////@Composable
////fun FriendScreen(
////    onAddFriend: () -> Unit = {},
////    tab: RankTab,
////    onSelectFriendTab: () -> Unit,
////    onSelectAllTab: () -> Unit,
////    ranks: List<FriendRank>,
////    loading: Boolean
////) {
////    var tab by remember { mutableStateOf(FriendTab.FRIEND) }
////
////    val all = remember {
////        listOf(
////            FriendRank(1, "금동동", 2149),
////            FriendRank(2, "은동동", 2019),
////            FriendRank(3, "동동동", 2012),
////            FriendRank(4, "박뭉치", 2149),
////            FriendRank(5, "김뭉치", 2019, isMe = true), // 5위: 나
////            FriendRank(6, "허뭉치", 1329),
////            FriendRank(7, "송뭉치", 839),
////        )
////    }
////    val list = all // 탭 분기 필요시 교체
////
////    LazyColumn(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(Color.White),
////        contentPadding = PaddingValues(
////            start = HorizontalPad,
////            end = HorizontalPad,
////            top = VerticalPad,
////            bottom = VerticalPad
////        )
////    ) {
////        // ───── 헤더: 왼쪽 아이콘 + 가운데 타이틀
////        item {
////            Box(
////                modifier = Modifier.fillMaxWidth(),
////                contentAlignment = Alignment.Center
////            ) {
////                IconButton(
////                    onClick = onAddFriend,
////                    modifier = Modifier.align(Alignment.CenterStart)
////                ) {
////                    Icon(
////                        painter = painterResource(id = R.drawable.ic_friend_add),
////                        contentDescription = "친구 추가",
////                        tint = Color.Unspecified
////                    )
////                }
////                Text(
////                    text = "친구목록",
////                    fontFamily = Pretendard,
////                    fontSize = 24.sp,
////                    fontWeight = FontWeight.SemiBold,
////                    color = Color.Black
////                )
////            }
////        }
////
////        // 헤더 → 탭 간격 (조금 올림: 24dp)
////        item { Spacer(Modifier.height(24.dp)) }
////
////        // ───── 탭(중앙 정렬)
////        item {
////            Row(
////                modifier = Modifier.fillMaxWidth(),
////                horizontalArrangement = Arrangement.Center,
////                verticalAlignment = Alignment.CenterVertically
////            ) {
////                RankTabChip(
////                    label = "친구 순위",
////                    selected = tab == FriendTab.FRIEND,
////                    onClick = { tab = FriendTab.FRIEND }
////                )
////                Spacer(Modifier.width(8.dp))
////                RankTabChip(
////                    label = "전체 순위",
////                    selected = tab == FriendTab.ALL,
////                    onClick = { tab = FriendTab.ALL }
////                )
////            }
////        }
////
////        // 탭 → Top3 간격 (더 벌림: 32dp)
////        item { Spacer(Modifier.height(32.dp)) }
////
////        // ───── Top 3: 은-금-동 (금은 살짝 위)
////        item {
////            val top3 = list.take(3)
////            Top3RowSilverGoldBronze(items = top3)
////        }
////
////        // Top3 → 리스트 간격 (약간 벌림)
////        item { Spacer(Modifier.height(20.dp)) }
////
////        // ───── 4위 이후 리스트
////        items(list.drop(3)) { item ->
////            RankRow(item = item)
////            Spacer(Modifier.height(10.dp))
////        }
////    }
////}
////
////// ───────────────── Tabs ─────────────────
////@Composable
////private fun RankTabChip(
////    label: String,
////    selected: Boolean,
////    onClick: () -> Unit
////) {
////    Surface(
////        shape = RoundedCornerShape(16.dp),
////        color = if (selected) BrandBlue else GrayBgChip,
////        shadowElevation = 0.dp,
////        tonalElevation = 0.dp,
////        modifier = Modifier
////            .height(30.dp)
////            .wrapContentWidth()
////            .clip(RoundedCornerShape(16.dp))
////            .noRippleClickable { onClick() }   // ripple 제거
////    ) {
////        Box(
////            contentAlignment = Alignment.Center,
////            modifier = Modifier.padding(horizontal = 12.dp)
////        ) {
////            Text(
////                text = label,
////                fontFamily = Pretendard,
////                fontSize = 12.sp,
////                fontWeight = FontWeight.SemiBold,
////                color = if (selected) Color.White else GrayTextSub
////            )
////        }
////    }
////}
////
////// 클릭 리플 제거 버전(Modifier 확장)
////private fun Modifier.noRippleClickable(
////    enabled: Boolean = true,
////    onClick: () -> Unit
////): Modifier = composed {
////    val interaction = remember { MutableInteractionSource() }
////    this.clickable(
////        enabled = enabled,
////        interactionSource = interaction,
////        indication = null,
////        onClick = onClick
////    )
////}
////
////// ───────────────── Top 3(은-금-동) ─────────────────
////@Composable
////private fun Top3RowSilverGoldBronze(items: List<FriendRank>) {
////    // [1(금), 2(은), 3(동)] → 은, 금, 동 순서로 재배치
////    val safe = when {
////        items.size >= 3 -> listOf(items[1], items[0], items[2])
////        else -> items
////    }
////
////    Row(
////        modifier = Modifier.fillMaxWidth(),
////        horizontalArrangement = Arrangement.SpaceBetween,
////        verticalAlignment = Alignment.Top
////    ) {
////        Box(Modifier.weight(1f).offset(y = 10.dp), contentAlignment = Alignment.TopCenter) {
////            Top3Item(item = safe.getOrNull(0), raise = false) // 은
////        }
////        Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
////            Top3Item(item = safe.getOrNull(1), raise = true)  // 금(위로)
////        }
////        Box(Modifier.weight(1f).offset(y = 10.dp), contentAlignment = Alignment.TopCenter) {
////            Top3Item(item = safe.getOrNull(2), raise = false) // 동
////        }
////    }
////}
////
////@Composable
////private fun Top3Item(item: FriendRank?, raise: Boolean) {
////    if (item == null) return
////    val borderColor = if (item.rank == 1) Gold else RankNumGray
////
////    Column(
////        horizontalAlignment = Alignment.CenterHorizontally,
////        modifier = if (raise) Modifier.offset(y = (-10).dp) else Modifier
////    ) {
////        Box(
////            modifier = Modifier.size(75.dp),
////            contentAlignment = Alignment.TopCenter
////        ) {
////            AvatarThumb(
////                size = 75.dp,
////                border = BorderStroke(2.dp, borderColor),
////                painterRes = item.avatarRes
////            )
////            if (item.rank == 1) {
////                Icon(
////                    painter = painterResource(id = R.drawable.ic_crown),
////                    contentDescription = null,
////                    tint = Color.Unspecified,
////                    modifier = Modifier
////                        .size(26.dp)
////                        .offset(y = (-12).dp)
////                )
////            }
////        }
////        Spacer(Modifier.height(8.dp))
////        Text(
////            text = item.name,
////            fontFamily = Pretendard,
////            fontSize = 16.sp,
////            fontWeight = FontWeight.Medium,
////            color = Color.Black,
////            maxLines = 1,
////            overflow = TextOverflow.Ellipsis
////        )
////        Spacer(Modifier.height(2.dp))
////        Text(
////            text = "${item.points}P",
////            fontFamily = Pretendard,
////            fontSize = 12.sp,
////            fontWeight = FontWeight.Medium,
////            color = GrayTextSub
////        )
////    }
////}
////
////// ───────────────── Rank rows (4위~) ─────────────────
////@Composable
////private fun RankRow(item: FriendRank) {
////    val outline =
////        if (item.isMe) BorderStroke(2.dp, BrandBlue) else BorderStroke(1.dp, Color(0x14000000))
////
////    Surface(
////        shape = RoundedCornerShape(12.dp),
////        color = Color.White,
////        tonalElevation = 0.dp,
////        shadowElevation = 2.dp,
////        border = outline,
////        modifier = Modifier.fillMaxWidth()
////    ) {
////        Row(
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(horizontal = 12.dp, vertical = 10.dp),
////            verticalAlignment = Alignment.CenterVertically
////        ) {
////            // 순위 번호
////            Text(
////                text = item.rank.toString(),
////                fontFamily = Pretendard,
////                fontSize = 16.sp,
////                fontWeight = FontWeight.Medium,
////                color = RankNumGray
////            )
////            Spacer(Modifier.width(12.dp))
////            // 아바타
////            AvatarThumb(size = 44.dp, painterRes = item.avatarRes)
////            Spacer(Modifier.width(12.dp))
////            // 이름 (왼쪽, 가변폭)
////            Text(
////                text = item.name,
////                fontFamily = Pretendard,
////                fontSize = 16.sp,
////                fontWeight = FontWeight.Medium,
////                color = Color.Black,
////                maxLines = 1,
////                overflow = TextOverflow.Ellipsis,
////                modifier = Modifier.weight(1f)
////            )
////            // 포인트 (오른쪽 끝)
////            Text(
////                text = "${item.points}P",
////                fontFamily = Pretendard,
////                fontSize = 12.sp,
////                fontWeight = FontWeight.Medium,
////                color = GrayTextSub
////            )
////        }
////    }
////}
////
////// ───────────────── Avatar(안전 플레이스홀더) ─────────────────
////@Composable
////private fun AvatarThumb(
////    size: Dp,
////    border: BorderStroke? = null,
////    @DrawableRes painterRes: Int? = null
////) {
////    val isPreview = LocalInspectionMode.current
////    val shape = CircleShape
////
////    Box(
////        modifier = Modifier
////            .size(size)
////            .clip(shape)
////            .then(if (border != null) Modifier.border(border, shape) else Modifier),
////        contentAlignment = Alignment.Center
////    ) {
////        if (painterRes != null) {
////            Image(
////                painter = painterResource(id = painterRes),
////                contentDescription = null,
////                contentScale = ContentScale.Crop,
////                modifier = Modifier.fillMaxSize()
////            )
////        } else {
////            Box(
////                Modifier
////                    .fillMaxSize()
////                    .background(Color(0xFFEFF4FB)),
////                contentAlignment = Alignment.Center
////            ) {
////                Icon(
////                    painter = painterResource(
////                        id = if (isPreview) android.R.drawable.ic_menu_help else android.R.drawable.ic_menu_help
////                    ),
////                    contentDescription = null,
////                    tint = BrandBlue
////                )
////            }
////        }
////    }
////}
////
////
//////// ───────────────── Preview ─────────────────
//////@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//////@Composable
//////private fun PreviewFriendScreen() {
//////    MaterialTheme { Surface { FriendScreen() } }
//////}
