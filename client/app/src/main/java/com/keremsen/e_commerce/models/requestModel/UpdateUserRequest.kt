package com.keremsen.e_commerce.models.requestModel

data class UpdateUserRequest(
    val fullname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val role: String? = null
)
