package com.malmungchi.core.repository

import com.malmungchi.core.model.AddFriendResult
import com.malmungchi.core.model.FriendSummary

interface FriendRepository {
    suspend fun addFriendByCode(code: String): AddFriendResult
    suspend fun getFriendsRanking(limit: Int = 50): Pair<Int, List<FriendSummary>>
    suspend fun getGlobalRanking(limit: Int = 50): Pair<Int, List<FriendSummary>>

}