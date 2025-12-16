package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.entityModel.ProductImage


data class ProductImageResponse(
    val message: String?,
    val images: List<ProductImage>?
)
