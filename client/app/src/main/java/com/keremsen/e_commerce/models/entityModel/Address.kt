package com.keremsen.e_commerce.models.entityModel

data class Address(
    val id: Int,
    val user_id: Int,
    val title: String,
    val city: String,
    val district: String,
    val detail: String,
    val is_deleted: Boolean
)

