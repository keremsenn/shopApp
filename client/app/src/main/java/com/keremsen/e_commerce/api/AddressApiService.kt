package com.keremsen.e_commerce.api

import com.keremsen.e_commerce.models.entityModel.Address
import com.keremsen.e_commerce.models.requestModel.AddressRequest
import com.keremsen.e_commerce.models.responseModel.AddressActionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AddressApiService {

    @GET("api/addresses")
    suspend fun getAddresses(): Response<List<Address>>

    @GET("api/addresses/{address_id}")
    suspend fun getAddressById(@Path("address_id") addressId: Int): Response<Address>

    @POST("api/addresses")
    suspend fun createAddress(@Body request: AddressRequest): Response<AddressActionResponse>

    @PUT("api/addresses/{address_id}")
    suspend fun updateAddress(@Path("address_id") addressId: Int, @Body request: AddressRequest): Response<AddressActionResponse>

    @DELETE("api/addresses/{address_id}")
    suspend fun deleteAddress(@Path("address_id") addressId: Int): Response<Map<String, String>>
}