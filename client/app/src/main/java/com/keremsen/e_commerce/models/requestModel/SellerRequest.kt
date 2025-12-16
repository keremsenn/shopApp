package com.keremsen.e_commerce.models.requestModel

data class SellerRequest(
    val id: Int,
    val user_id: Int,
    val company_name: String?,
    val tax_number: String?,
    val status: String,
    val created_at: String,
    val updated_at: String?
)
