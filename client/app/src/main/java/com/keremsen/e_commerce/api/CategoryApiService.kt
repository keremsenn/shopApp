package com.keremsen.e_commerce.api

import com.keremsen.e_commerce.models.entityModel.Category
import com.keremsen.e_commerce.models.requestModel.CategoryRequest
import com.keremsen.e_commerce.models.responseModel.CategoryActionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CategoryApiService {

        @GET("api/categories")
        suspend fun getAllCategories(): Response<List<Category>>

        @GET("api/categories/roots")
        suspend fun getRootCategories(): Response<List<Category>>

        @GET("api/categories/{category_id}")
        suspend fun getCategoryById(@Path("category_id") categoryId: Int): Response<Category>

        @GET("api/categories/parent/{parent_id}")
        suspend fun getChildCategories(@Path("parent_id") parentId: Int): Response<List<Category>>

        @POST("api/categories")
        suspend fun createCategory(@Body request: CategoryRequest): Response<CategoryActionResponse>

        @PUT("api/categories/{category_id}")
        suspend fun updateCategory(
            @Path("category_id") categoryId: Int,
            @Body request: CategoryRequest
        ): Response<CategoryActionResponse>

        @DELETE("api/categories/{category_id}")
        suspend fun deleteCategory(@Path("category_id") categoryId: Int): Response<Map<String, String>>
}
