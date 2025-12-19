package com.keremsen.e_commerce.api

import com.keremsen.e_commerce.models.entityModel.Favorite
import com.keremsen.e_commerce.models.requestModel.AddFavoriteRequest
import com.keremsen.e_commerce.models.responseModel.FavoriteResponse
import retrofit2.Response
import retrofit2.http.*

interface FavoriteApiService {

    @GET("api/favorites/")
    suspend fun getFavorites(): Response<List<Favorite>>

    @POST("api/favorites/")
    suspend fun addFavorite(@Body request: AddFavoriteRequest): Response<FavoriteResponse>

    @DELETE("api/favorites/{productId}")
    suspend fun removeFavorite(@Path("productId") productId: Int): Response<FavoriteResponse>
}