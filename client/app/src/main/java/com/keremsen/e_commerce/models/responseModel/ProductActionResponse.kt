package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.entityModel.Product

data class ProductActionResponse(
    val message: String?,
    val product: Product?
)
