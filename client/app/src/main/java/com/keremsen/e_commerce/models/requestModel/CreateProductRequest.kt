package com.keremsen.e_commerce.models.requestModel

data class CreateProductRequest(
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val category_id: Int
)
