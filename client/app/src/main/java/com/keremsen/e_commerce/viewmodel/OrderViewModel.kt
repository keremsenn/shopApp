package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.models.entityModel.Order
import com.keremsen.e_commerce.models.requestModel.OrderItemRequest
import com.keremsen.e_commerce.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: OrderRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    private val _orderDetail = MutableStateFlow<Order?>(null)
    val orderDetail: StateFlow<Order?> = _orderDetail

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess


    fun getOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getOrders()
                if (response.isSuccessful) {
                    _orders.value = response.body() ?: emptyList()
                } else {
                    _message.value = "Siparişler alınamadı: ${response.message()}"
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getOrderDetail(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getOrderDetail(orderId)
                if (response.isSuccessful) {
                    _orderDetail.value = response.body()
                } else {
                    _message.value = "Sipariş detayı bulunamadı."
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createOrder(addressId: Int, items: List<OrderItemRequest>? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false // Resetle
            try {
                val response = repository.createOrder(addressId, items)
                if (response.isSuccessful) {
                    _message.value = "Siparişiniz başarıyla oluşturuldu!"
                    _operationSuccess.value = true
                    // Sipariş oluştu, listeyi yenileyelim
                    getOrders()
                } else {
                    _message.value = "Sipariş oluşturulamadı: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cancelOrder(orderId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.cancelOrder(orderId)
                if (response.isSuccessful) {
                    _message.value = "Sipariş iptal edildi."
                    getOrders()
                    if (_orderDetail.value?.id == orderId) {
                        getOrderDetail(orderId)
                    }
                } else {
                    _message.value = "İptal işlemi başarısız."
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}