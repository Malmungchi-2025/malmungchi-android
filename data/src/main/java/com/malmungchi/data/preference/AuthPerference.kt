package com.malmungchi.data.preference

interface AuthPreference {
    var accessToken: String?
    var refreshToken: String?


    fun clear()              // 로그 아웃 구현!

    // ✅ 대표 배지 저장/조회 정의만 남기기
    fun saveRepresentativeBadge(badgeKey: String)
    fun getRepresentativeBadge(): String?

}

