package com.malmungchi.data.implementation.repository

import com.malmungchi.core.model.*
import com.malmungchi.core.repository.FriendRepository
import com.malmungchi.data.api.FriendService
import javax.inject.Inject

class FriendRepositoryImpl @Inject constructor(
    private val api: FriendService
) : FriendRepository {
    override suspend fun addFriendByCode(code: String): AddFriendResult {
        val resp = api.addFriendByCode(AddFriendByCodeReq(code))
        if (!resp.success) error(resp.message ?: "친구 추가 실패")
        return resp.result ?: error("빈 응답")
    }

    override suspend fun getFriendsRanking(limit: Int): Pair<Int, List<FriendSummary>> {
        val resp = api.getFriendsRanking(limit)
        if (!resp.success) error(resp.message ?: "친구 랭킹 실패")
        val p = resp.result ?: error("빈 응답")
        return p.meId to p.items
    }

    override suspend fun getGlobalRanking(limit: Int): Pair<Int, List<FriendSummary>> {
        val resp = api.getGlobalRanking(limit)
        if (!resp.success) error(resp.message ?: "전체 랭킹 실패")
        val p = resp.result ?: error("빈 응답")
        return p.meId to p.items
    }
}