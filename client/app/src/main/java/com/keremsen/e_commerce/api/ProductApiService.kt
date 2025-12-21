package com.keremsen.e_commerce.api

import com.keremsen.e_commerce.models.entityModel.Product
import com.keremsen.e_commerce.models.requestModel.CreateProductRequest
import com.keremsen.e_commerce.models.requestModel.UpdateProductRequest
import com.keremsen.e_commerce.models.responseModel.ProductActionResponse
import com.keremsen.e_commerce.models.responseModel.ProductImageResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApiService {
    @GET("api/products/search")
    suspend fun searchProducts(@Query("q") query: String): Response<List<Product>>

    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>

    @GET("api/products/{product_id}")
    suspend fun getProductById(@Path("product_id") productId: Int): Response<Product>

    @POST("api/products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<ProductActionResponse>

    @PUT("api/products/{product_id}")
    suspend fun updateProduct(@Path("product_id") productId: Int, @Body request: UpdateProductRequest): Response<ProductActionResponse>

    @DELETE("api/products/{product_id}")
    suspend fun deleteProduct(@Path("product_id") productId: Int): Response<Map<String, String>>

    @Multipart
    @POST("api/products/{product_id}/images")
    suspend fun addProductImages(@Path("product_id") productId: Int, @Part files: List<MultipartBody.Part>): Response<ProductImageResponse>

    @DELETE("api/products/images/{image_id}")
    suspend fun deleteProductImage(@Path("image_id") image_id: Int): Response<Map<String, String>>

    @GET("api/products/seller/{seller_id}")
    suspend fun getProductsBySeller(@Path("seller_id") sellerId: Int): Response<List<Product>>

    @GET("api/products/category/{category_id}")
    suspend fun getProductsByCategory(@Path("category_id") categoryId: Int): Response<List<Product>>
}