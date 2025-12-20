package com.keremsen.e_commerce.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.keremsen.e_commerce.models.entityModel.Address
import com.keremsen.e_commerce.models.requestModel.AddressRequest
import com.keremsen.e_commerce.repository.AddressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val repository: AddressRepository
) : ViewModel() {

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess

    init {
        getAddresses()
    }

    fun getAddresses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getAddresses()
                if (response.isSuccessful) {
                    _addresses.value = response.body() ?: emptyList()
                } else {
                    _message.value = "Adresler alınamadı."
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveAddress(id: Int, title: String, city: String, district: String, detail: String) {
        val request = AddressRequest(
            title = title,
            city = city,
            district = district,
            detail = detail
        )

        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false

            try {
                val response = if (id == 0) {
                    repository.createAddress(request)
                } else {
                    repository.updateAddress(id, request)
                }

                if (response.isSuccessful) {
                    _message.value = if (id == 0) "Adres eklendi" else "Adres güncellendi"
                    _operationSuccess.value = true
                    getAddresses()
                } else {
                    _message.value = "İşlem başarısız: ${response.code()}"
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteAddress(addressId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.deleteAddress(addressId)
                if (response.isSuccessful) {
                    _message.value = "Adres silindi"
                    getAddresses()
                } else {
                    _message.value = "Silme başarısız."
                }
            } catch (e: Exception) {
                _message.value = "Hata: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAddressFromCache(id: Int): Address? {
        return _addresses.value.find { it.id == id }
    }

    fun clearMessage() {
        _message.value = null
    }
}