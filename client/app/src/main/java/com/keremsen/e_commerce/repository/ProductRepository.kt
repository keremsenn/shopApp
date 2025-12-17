package com.keremsen.e_commerce.repository

import com.keremsen.e_commerce.api.ProductApiService
import com.keremsen.e_commerce.models.requestModel.CreateProductRequest
import com.keremsen.e_commerce.models.requestModel.UpdateProductRequest
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ProductApiService
) {
    suspend fun getAllProducts() = apiService.getAllProducts()

    suspend fun getProductById(productId: Int) = apiService.getProductById(productId)

    suspend fun createProduct(request: CreateProductRequest) = apiService.createProduct(request)

    suspend fun updateProduct(productId: Int, request: UpdateProductRequest) =
        apiService.updateProduct(productId, request)

    suspend fun deleteProduct(productId: Int) = apiService.deleteProduct(productId)

    suspend fun addProductImages(productId: Int, files: List<MultipartBody.Part>) =
        apiService.addProductImages(productId, files)

    suspend fun deleteProductImage(imageId: Int) = apiService.deleteProductImage(imageId)

    suspend fun getProductsByCategory(categoryId: Int) = apiService.getProductsByCategory(categoryId)

    suspend fun getProductsBySeller(sellerId: Int) = apiService.getProductsBySeller(sellerId)
}

