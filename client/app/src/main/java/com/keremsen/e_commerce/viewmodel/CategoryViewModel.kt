package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.models.entityModel.Category
import com.keremsen.e_commerce.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchRootCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getRootCategories()
                if (response.isSuccessful) {
                    _categories.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Kategoriler yüklenemedi"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchChildCategories(parentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.getChildCategories(parentId)
            if (response.isSuccessful) {
                _categories.value = response.body() ?: emptyList()
            }
            _isLoading.value = false
        }
    }
}

