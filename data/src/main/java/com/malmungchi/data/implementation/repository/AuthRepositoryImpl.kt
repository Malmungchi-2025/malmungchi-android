package com.malmungchi.data.implementation.repository

import com.malmungchi.core.model.*
import com.malmungchi.core.repository.AuthRepository
import com.malmungchi.data.api.AuthService
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(   // 👈 @Inject 추가
    private val api: AuthService
) : AuthRepository {

    override suspend fun devRequestOtp(email: String): Boolean {
        val r = api.requestOtp(ResendRequest(email))
        return r.success
    }

    override suspend fun devVerifyOtp(email: String, code: String): Boolean {
        val r = api.verifyOtp(mapOf("email" to email, "code" to code))
        return r.success
    }

    override suspend fun register(email: String, password: String, name: String, nickname: String?): RegisterResponse {
        return api.register(RegisterRequest(email, password, name, nickname))
    }

    override suspend fun resendVerification(email: String): Boolean {
        val r = api.resendVerification(ResendRequest(email))
        return r.success
    }

    override suspend fun login(email: String, password: String): LoginResponse {
        return api.login(LoginRequest(email, password))
    }

    override suspend fun me(): UserDto? {
        val r = api.me()
        return if (r.success) r.result else null
    }
}