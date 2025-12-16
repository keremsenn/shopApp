package com.keremsen.e_commerce.models.entityModel

data class Product(
    val id: Int,
    val seller_id: Int,
    val category_id: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val rating: Double,
    val is_deleted: Boolean,
    val images: List<ProductImage>? = null
)

