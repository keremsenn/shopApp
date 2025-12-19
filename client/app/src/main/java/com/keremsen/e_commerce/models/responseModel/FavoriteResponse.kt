package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.entityModel.Favorite

data class FavoriteResponse(
    val message: String,
    val favorite: Favorite? = null
)
