package com.keremsen.e_commerce.models.requestModel

data class RegisterRequest(
    val fullname: String,
    val email: String,
    val password: String,
    val phone: String? = null
)
