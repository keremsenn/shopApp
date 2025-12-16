package com.keremsen.e_commerce.models.requestModel

data class CategoryRequest(
    val name: String,
    val parent_id: Int? = null
)