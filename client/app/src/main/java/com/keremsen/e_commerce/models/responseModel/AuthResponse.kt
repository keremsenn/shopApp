package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.entityModel.User

data class AuthResponse(
    val user: User?,
    val access_token: String?,
    val refresh_token:String?,
    val message: String? = null,
    val error: String? = null
)

