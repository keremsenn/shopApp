package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.models.entityModel.Product
import com.keremsen.e_commerce.models.requestModel.CreateProductRequest
import com.keremsen.e_commerce.models.requestModel.UpdateProductRequest
import com.keremsen.e_commerce.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    // Arama gecikmesini yönetmek için Job
    private var searchJob: Job? = null

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val _productDetail = MutableStateFlow<Product?>(null)
    val productDetail: StateFlow<Product?> = _productDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _actionSuccess = MutableStateFlow(false)
    val actionSuccess: StateFlow<Boolean> = _actionSuccess

    fun searchProducts(query: String) {
        searchJob?.cancel()

        if (query.isEmpty()) {
            fetchAllProducts()
            return
        }

        if (query.length < 2) return

        searchJob = viewModelScope.launch {
            delay(500)


            executeApiCall(showLoading = true) {
                val response = repository.searchProducts(query)
                if (response.isSuccessful) {
                    _products.value = response.body() ?: emptyList()
                }
                response
            }
        }
    }

    fun fetchAllProducts() {
        executeApiCall {
            repository.getAllProducts().also {
                if (it.isSuccessful) _products.value = it.body() ?: emptyList()
            }
        }
    }

    fun fetchProductById(productId: Int) {
        executeApiCall {
            _productDetail.value = null
            repository.getProductById(productId).also { if (it.isSuccessful) _productDetail.value = it.body() }
        }
    }

    fun fetchProductsByCategory(categoryId: Int) {
        executeApiCall {
            repository.getProductsByCategory(categoryId).also {
                if (it.isSuccessful) _products.value = it.body() ?: emptyList()
            }
        }
    }

    fun fetchProductsBySeller(sellerId: Int) {
        executeApiCall {
            repository.getProductsBySeller(sellerId).also {
                if (it.isSuccessful) _products.value = it.body() ?: emptyList()
            }
        }
    }

    fun createProduct(request: CreateProductRequest) {
        executeApiCall(showLoading = true) {
            val response = repository.createProduct(request)
            if (response.isSuccessful) _actionSuccess.value = true
            response
        }
    }

    fun updateProduct(productId: Int, request: UpdateProductRequest) {
        executeApiCall(showLoading = true) {
            val response = repository.updateProduct(productId, request)
            if (response.isSuccessful) _actionSuccess.value = true
            response
        }
    }

    fun deleteProduct(productId: Int) {
        executeApiCall(showLoading = true) {
            val response = repository.deleteProduct(productId)
            if (response.isSuccessful) {
                _products.value = _products.value.filter { it.id != productId }
                _actionSuccess.value = true
            }
            response
        }
    }

    fun addProductImages(productId: Int, files: List<MultipartBody.Part>) {
        executeApiCall(showLoading = true) {
            val response = repository.addProductImages(productId, files)
            if (response.isSuccessful) fetchProductById(productId)
            response
        }
    }

    fun deleteProductImage(imageId: Int, productId: Int) {
        executeApiCall(showLoading = true) {
            val response = repository.deleteProductImage(imageId)
            if (response.isSuccessful) fetchProductById(productId)
            response
        }
    }

    // ⭐ Senin mevcut API Çağrı yapın (Hataları çözen kısım burası)
    private fun <T> executeApiCall(
        showLoading: Boolean = true,
        call: suspend () -> retrofit2.Response<T>
    ) {
        viewModelScope.launch {
            if (showLoading) _isLoading.value = true
            _errorMessage.value = null
            _actionSuccess.value = false
            try {
                val response = call()
                if (!response.isSuccessful) {
                    _errorMessage.value = "Hata: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Bağlantı Hatası: ${e.localizedMessage}"
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
    fun resetActionSuccess() { _actionSuccess.value = false }
}