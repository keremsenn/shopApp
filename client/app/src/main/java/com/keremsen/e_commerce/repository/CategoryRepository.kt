package com.keremsen.e_commerce.repository

import com.keremsen.e_commerce.api.CategoryApiService
import com.keremsen.e_commerce.models.requestModel.CategoryRequest
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: CategoryApiService
) {
    suspend fun getAllCategories() = apiService.getAllCategories()

    suspend fun getRootCategories() = apiService.getRootCategories()

    suspend fun getChildCategories(parentId: Int) = apiService.getChildCategories(parentId)

    suspend fun createCategory(name: String, parentId: Int?) =
        apiService.createCategory(CategoryRequest(name, parentId))

    suspend fun deleteCategory(categoryId: Int) = apiService.deleteCategory(categoryId)
}

