package com.keremsen.e_commerce.repository

import com.keremsen.e_commerce.api.UserApiService
import com.keremsen.e_commerce.models.requestModel.UpdateUserRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: UserApiService
) {
    suspend fun getAllUsers() = apiService.getAllUsers()

    suspend fun getCurrentUser() = apiService.getCurrentUser()

    suspend fun getUserById(userId: Int) = apiService.getUserById(userId)

    suspend fun updateUser(userId: Int, request: UpdateUserRequest) =
        apiService.updateUser(userId, request)

    suspend fun deleteUser(userId: Int) = apiService.deleteUser(userId)
}