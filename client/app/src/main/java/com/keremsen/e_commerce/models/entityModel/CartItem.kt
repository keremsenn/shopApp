package com.keremsen.e_commerce.models.entityModel

data class CartItem(
    val id: Int,
    val quantity: Int,
    val subtotal: Double,
    val product: Product?
) {
    val product_id: Int
        get() = product?.id ?: 0
}

