package com.malmungchi.data.session

object SessionManager {
    @Volatile var userId: Int? = null
        private set
    @Volatile var token: String? = null
        private set

    fun set(userId: Int, token: String) {
        this.userId = userId
        this.token = token
    }

    fun clear() {
        userId = null
        token = null
    }
}