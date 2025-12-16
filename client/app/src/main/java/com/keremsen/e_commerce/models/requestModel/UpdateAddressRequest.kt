package com.keremsen.e_commerce.models.requestModel

data class UpdateAddressRequest(
    val title: String? = null,
    val city: String? = null,
    val district: String? = null,
    val detail: String? = null
)
