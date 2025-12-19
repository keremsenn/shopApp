package com.keremsen.e_commerce.repository

import com.keremsen.e_commerce.api.AuthApiService
import com.keremsen.e_commerce.models.requestModel.LoginRequest
import com.keremsen.e_commerce.models.requestModel.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: AuthApiService
) {
    suspend fun register(request: RegisterRequest) = apiService.register(request)

    suspend fun login(request: LoginRequest) = apiService.login(request)

    suspend fun refreshToken(token: String) = apiService.refreshToken("Bearer $token")
}

