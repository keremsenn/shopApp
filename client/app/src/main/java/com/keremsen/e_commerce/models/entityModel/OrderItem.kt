package com.keremsen.e_commerce.models.entityModel

data class OrderItem(
    val id: Int,
    val order_id: Int,
    val product_id: Int,
    val quantity: Int,
    val unit_price: Double,
    val product: Product?,
    val subtotal: Double
)

