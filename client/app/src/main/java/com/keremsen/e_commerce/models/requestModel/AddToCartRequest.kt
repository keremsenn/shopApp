package com.keremsen.e_commerce.models.requestModel

data class AddToCartRequest(
    val product_id: Int,
    val quantity: Int = 1
)
