package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.entityModel.User

data class RegisterResponse(
    val message: String?,
    val user: User
)

