package com.keremsen.e_commerce.models.entityModel

data class Favorite(
    val id: Int,
    val user_id: Int,
    val created_at: String,
    val product: Product
){
    val product_id: Int
        get() = product.id
}
