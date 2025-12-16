package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.entityModel.User

data class UserUpdateResponse(
    val message: String?,
    val user: User?,
    val error: String?
)
