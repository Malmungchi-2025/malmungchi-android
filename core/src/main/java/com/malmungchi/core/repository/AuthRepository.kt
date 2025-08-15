package com.malmungchi.core.repository
import com.malmungchi.core.model.*

interface AuthRepository {
    suspend fun devRequestOtp(email: String): Boolean
    suspend fun devVerifyOtp(email: String, code: String): Boolean

    suspend fun register(email: String, password: String, name: String, nickname: String? = null): RegisterResponse
    suspend fun resendVerification(email: String): Boolean
    suspend fun login(email: String, password: String): LoginResponse
    suspend fun me(): UserDto?
}