package com.malmungchi.feature.friend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FriendRankRoute(
    onAddFriend: () -> Unit = {}
) {
    val vm: FriendAddViewModel = hiltViewModel()
    val state by vm.ui.collectAsState()

    val list = when (state.rankTab) {
        RankTab.FRIEND -> state.friends
        RankTab.ALL    -> state.all
    }

    FriendScreen(
        onAddFriend = onAddFriend,
        tab = state.rankTab,
        onSelectFriendTab = { vm.switchTab(RankTab.FRIEND) },
        onSelectAllTab    = { vm.switchTab(RankTab.ALL) },
        ranks = list,
        loading = state.rankLoading
    )
}