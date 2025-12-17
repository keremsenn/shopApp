package com.keremsen.e_commerce.repository

import com.keremsen.e_commerce.api.CartApiService
import com.keremsen.e_commerce.models.requestModel.AddToCartRequest
import com.keremsen.e_commerce.models.requestModel.UpdateCartItemRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val apiService: CartApiService
) {
    suspend fun getCart() = apiService.getCart()

    suspend fun addItem(productId: Int, quantity: Int) =
        apiService.addItemToCart(AddToCartRequest(productId, quantity))

    suspend fun updateItem(cartItemId: Int, quantity: Int) =
        apiService.updateCartItem(cartItemId, UpdateCartItemRequest(quantity))

    suspend fun removeItem(cartItemId: Int) = apiService.removeItemFromCart(cartItemId)

    suspend fun clearCart() = apiService.clearCart()
}

