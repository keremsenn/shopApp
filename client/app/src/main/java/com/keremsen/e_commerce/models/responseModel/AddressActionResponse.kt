package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.entityModel.Address

data class AddressActionResponse(
    val message: String?,
    val error: String?,
    val address: Address?
)
