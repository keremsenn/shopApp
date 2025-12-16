package com.keremsen.e_commerce.models.entityModel

data class Cart(
    val id: Int,
    val user_id: Int,
    val created_at: String?,
    val items: List<CartItem> = emptyList(),
    val total: Double
)

