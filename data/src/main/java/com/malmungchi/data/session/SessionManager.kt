package com.malmungchi.data.session

object SessionManager {
    @Volatile
    private var nicknameTitle: String? = null
    @Volatile var userId: Int? = null
        private set

    @Volatile var token: String? = null
        private set

    @Volatile var level: Int? = null   // 1~4 (선택)
        private set

    // level을 옵션으로 받는 set 오버로드
    fun set(userId: Int, token: String, level: Int? = null) {
        this.userId = userId
        this.token = token
        this.level = level
    }

    // nickname_title 업데이트 메서드
    fun updateNicknameTitle(nicknameTitle: String?) {
        this.nicknameTitle = nicknameTitle
    }

    // level만 갱신할 때
    fun updateLevel(level: Int?) {
        this.level = level
    }

    fun clear() {
        userId = null
        token = null
        level = null
    }
}