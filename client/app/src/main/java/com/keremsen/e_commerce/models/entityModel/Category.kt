package com.keremsen.e_commerce.models.entityModel

data class Category(
    val id: Int,
    val name: String,
    val parent_id: Int?,
    val children: List<Category>?
)

