package com.malmungchi.data.api


import com.malmungchi.core.model.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

interface FriendService {
    @POST("/api/friends/by-code")
    suspend fun addFriendByCode(
        @Body req: AddFriendByCodeReq
    ): BaseResponse<AddFriendResult>

    @GET("/api/friends/ranking")
    suspend fun getFriendsRanking(@Query("limit") limit: Int = 50): BaseResponse<RankingPayload>

    @GET("/api/friends/ranking/all")
    suspend fun getGlobalRanking(@Query("limit") limit: Int = 50): BaseResponse<RankingPayload>
}

data class RankingPayload(
    val meId: Int,
    val items: List<FriendSummary>
)