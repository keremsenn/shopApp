package com.keremsen.e_commerce.repository

import com.keremsen.e_commerce.api.FavoriteApiService
import com.keremsen.e_commerce.models.requestModel.AddFavoriteRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val apiService: FavoriteApiService
) {
    suspend fun getFavorites() = apiService.getFavorites()

    suspend fun addFavorite(productId: Int) =
        apiService.addFavorite(AddFavoriteRequest(productId))

    suspend fun removeFavorite(productId: Int) =
        apiService.removeFavorite(productId)
}