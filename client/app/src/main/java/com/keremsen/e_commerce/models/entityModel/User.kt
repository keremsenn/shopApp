package com.keremsen.e_commerce.models.entityModel

data class User(
    val id: Int,
    val fullname: String,
    val email: String,
    val phone: String?,
    val role: String,
    val created_at: String,
    val is_deleted: Boolean
)

