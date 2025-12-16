package com.keremsen.e_commerce.models.requestModel



data class CreateOrderRequest(
    val address_id: Int,
    val items: List<OrderItemRequest>? = null
)
