package com.malmungchi.core.model

import androidx.annotation.DrawableRes
import com.google.gson.annotations.SerializedName

data class AddFriendByCodeReq(
    val code: String
)

/** 서버가 내려주는 친구 요약 */
data class FriendSummary(
    val id: Int,
    val name: String?,
    @SerializedName("avatarName")
    val avatar_name: String?,
    val point: Int?,
    @SerializedName("friendCode")
    val friend_code: String
)

/** 서버 friend_edges 행 */
data class FriendEdge(
    val id: Long,
    @SerializedName("requester_id")
    val requesterId: Long,
    @SerializedName("addressee_id")
    val addresseeId: Long,
    val status: String,                 // "ACCEPTED" 등
    @SerializedName("accepted_at")
    val acceptedAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?
)

/** result 루트 */
data class AddFriendResult(
    val friend: FriendSummary,
    val edge: FriendEdge
)

