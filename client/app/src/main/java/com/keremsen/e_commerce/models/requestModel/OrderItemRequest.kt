package com.keremsen.e_commerce.models.requestModel

data class OrderItemRequest(
    val product_id: Int,
    val quantity: Int
)
