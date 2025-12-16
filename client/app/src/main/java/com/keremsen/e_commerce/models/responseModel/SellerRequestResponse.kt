package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.requestModel.SellerRequest

data class SellerRequestResponse(
    val message: String?,
    val error: String?,
    val request: SellerRequest?
)
