package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.models.entityModel.Favorite
import com.keremsen.e_commerce.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<Favorite>>(emptyList())
    val favorites: StateFlow<List<Favorite>> = _favorites

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun getFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = favoriteRepository.getFavorites()
                if (response.isSuccessful) {
                    _favorites.value = response.body() ?: emptyList()
                } else {
                    _message.value = "Favoriler alınamadı: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFavorite(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = favoriteRepository.addFavorite(productId)
                if (response.isSuccessful) {
                    _message.value = response.body()?.message ?: "Favorilere eklendi"
                    getFavorites() // Listeyi güncelle
                } else {
                    _message.value = "Ekleme başarısız"
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFavorite(productId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = favoriteRepository.removeFavorite(productId)
                if (response.isSuccessful) {
                    _message.value = response.body()?.message ?: "Favorilerden çıkarıldı"
                    getFavorites() // Listeyi güncelle
                } else {
                    _message.value = "Silme başarısız"
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun isFavorite(productId: Int): Boolean {
        return _favorites.value.any { it.product_id == productId }
    }

    fun toggleFavorite(productId: Int) {
        if (isFavorite(productId)) {
            removeFavorite(productId)
        } else {
            addFavorite(productId)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}