package com.keremsen.e_commerce.models.entityModel

data class Product(
    val id: Int,
    val seller_id: Int? = null,
    val seller_name: String? = null,
    val category_id: Int? = null,
    val category_name: String? = null,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val rating: Double,
    val is_deleted: Boolean,
    val images: List<ProductImage>? = null
)
