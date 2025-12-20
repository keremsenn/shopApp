package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.models.entityModel.Cart
import com.keremsen.e_commerce.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _cart = MutableStateFlow<Cart?>(null)
    val cart: StateFlow<Cart?> = _cart

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _cartMessage = MutableStateFlow<String?>(null)
    val cartMessage: StateFlow<String?> = _cartMessage

    fun getCart() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = cartRepository.getCart()
                if (response.isSuccessful) {
                    _cart.value = response.body()
                } else {
                    _cartMessage.value = "Sepet güncellenemedi: ${response.message()}"
                }
            } catch (e: Exception) {
                _cartMessage.value = "Bağlantı hatası: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = cartRepository.addItem(productId, quantity)
                if (response.isSuccessful) {
                    _cartMessage.value = "Ürün sepete eklendi"
                    getCart() // Sepeti güncelle ki UI yenilensin
                } else {
                    _cartMessage.value = "Ekleme başarısız: ${response.message()}"
                }
            } catch (e: Exception) {
                _cartMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCartItem(cartItemId: Int, quantity: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = cartRepository.updateItem(cartItemId, quantity)
                if (response.isSuccessful) {
                    getCart()
                } else {
                    _cartMessage.value = "Güncelleme başarısız"
                }
            } catch (e: Exception) {
                _cartMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun removeFromCart(cartItemId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = cartRepository.removeItem(cartItemId)
                if (response.isSuccessful) {
                    _cartMessage.value = "Ürün silindi"
                    getCart() // Listeyi güncelle
                } else {
                    _cartMessage.value = "Silme başarısız"
                }
            } catch (e: Exception) {
                _cartMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun clearCart() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = cartRepository.clearCart()
                if (response.isSuccessful) {
                    _cartMessage.value = "Sepet boşaltıldı"
                    _cart.value = null
                } else {
                    _cartMessage.value = "İşlem başarısız"
                }
            } catch (e: Exception) {
                _cartMessage.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _cartMessage.value = null
    }
}