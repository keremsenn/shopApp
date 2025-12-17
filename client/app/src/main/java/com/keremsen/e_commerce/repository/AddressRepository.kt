package com.keremsen.e_commerce.repository

import com.keremsen.e_commerce.api.AddressApiService
import com.keremsen.e_commerce.models.requestModel.AddressRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
    private val apiService: AddressApiService
) {
    suspend fun getAddresses() = apiService.getAddresses()

    suspend fun getAddressById(addressId: Int) = apiService.getAddressById(addressId)

    suspend fun createAddress(request: AddressRequest) = apiService.createAddress(request)

    suspend fun updateAddress(addressId: Int, request: AddressRequest) =
        apiService.updateAddress(addressId, request)

    suspend fun deleteAddress(addressId: Int) = apiService.deleteAddress(addressId)
}

