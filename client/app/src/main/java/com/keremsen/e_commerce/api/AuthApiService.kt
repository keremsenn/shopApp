package com.keremsen.e_commerce.api

import com.keremsen.e_commerce.models.requestModel.LoginRequest
import com.keremsen.e_commerce.models.requestModel.RegisterRequest
import com.keremsen.e_commerce.models.responseModel.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): Response<AuthResponse>
}