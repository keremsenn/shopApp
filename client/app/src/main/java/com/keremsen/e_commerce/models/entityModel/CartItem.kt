package com.keremsen.e_commerce.models.entityModel

data class CartItem(
    val id: Int,
    val cart_id: Int,
    val product_id: Int,
    val quantity: Int,
    val product: Product? = null
)

