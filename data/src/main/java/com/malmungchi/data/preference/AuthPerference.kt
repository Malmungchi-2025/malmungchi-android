package com.malmungchi.data.preference

interface AuthPreference {
    var accessToken: String?
    var refreshToken: String?


    fun clear()              // 로그 아웃 구현!

}

