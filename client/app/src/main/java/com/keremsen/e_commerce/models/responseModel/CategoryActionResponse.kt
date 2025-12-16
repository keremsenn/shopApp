package com.keremsen.e_commerce.models.responseModel

import com.keremsen.e_commerce.models.entityModel.Category

data class CategoryActionResponse(
    val message: String?,
    val error: String?,
    val category: Category?
)
