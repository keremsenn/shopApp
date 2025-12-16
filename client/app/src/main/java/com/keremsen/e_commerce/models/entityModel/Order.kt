package com.keremsen.e_commerce.models.entityModel

data class Order(
    val id: Int,
    val user_id: Int,
    val total_price: Double,
    val status: String,
    val created_at: String,
    val shipping_title: String,
    val shipping_city: String,
    val shipping_district: String,
    val shipping_detail: String,
    val items: List<OrderItem>? = null
)

