package com.keremsen.e_commerce.api

import com.keremsen.e_commerce.models.entityModel.User
import com.keremsen.e_commerce.models.requestModel.UpdateUserRequest
import com.keremsen.e_commerce.models.responseModel.UserUpdateResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApiService {

    @GET("api/users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("api/users/me")
    suspend fun getCurrentUser(): Response<User>

    @GET("api/users/{user_id}")
    suspend fun getUserById(@Path("user_id") userId: Int): Response<User>

    @PUT("api/users/{user_id}")
    suspend fun updateUser(@Path("user_id") userId: Int, @Body request: UpdateUserRequest): Response<UserUpdateResponse>

    @DELETE("api/users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: Int): Response<Map<String, String>>
}