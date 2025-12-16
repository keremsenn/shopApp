package com.keremsen.e_commerce.models.requestModel

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val category_id: Int? = null
)