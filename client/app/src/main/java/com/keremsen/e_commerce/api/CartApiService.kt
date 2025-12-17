package com.keremsen.e_commerce.api

import com.keremsen.e_commerce.models.entityModel.Cart
import com.keremsen.e_commerce.models.requestModel.AddToCartRequest
import com.keremsen.e_commerce.models.requestModel.UpdateCartItemRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApiService {

    @GET("api/cart")
    suspend fun getCart(): Response<Cart>

    @POST("api/cart/items")
    suspend fun addItemToCart(@Body request: AddToCartRequest): Response<Map<String, Any>>

    @PUT("api/cart/items/{cart_item_id}")
    suspend fun updateCartItem(@Path("cart_item_id") cartItemId: Int, @Body request: UpdateCartItemRequest): Response<Map<String, Any>>

    @DELETE("api/cart/items/{cart_item_id}")
    suspend fun removeItemFromCart(@Path("cart_item_id") cartItemId: Int): Response<Map<String, String>>

    @DELETE("api/cart")
    suspend fun clearCart(): Response<Map<String, String>>
}