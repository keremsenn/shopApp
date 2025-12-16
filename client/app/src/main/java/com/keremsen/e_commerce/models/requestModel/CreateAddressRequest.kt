package com.keremsen.e_commerce.models.requestModel

data class CreateAddressRequest(
    val title: String,
    val city: String,
    val district: String,
    val detail: String
)
