package com.keremsen.e_commerce.repository

import com.keremsen.e_commerce.api.SellerRequestApiService
import com.keremsen.e_commerce.models.requestModel.SellerRequestApply
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SellerRequestRepository @Inject constructor(
    private val apiService: SellerRequestApiService
) {
    suspend fun applyForSeller(company_name: String, tax_number: String) =
        apiService.applyForSeller(SellerRequestApply(company_name, tax_number))

    suspend fun getPendingApplications() = apiService.getPendingApplications()

    suspend fun approveApplication(requestId: Int) = apiService.approveApplication(requestId)

    suspend fun rejectApplication(requestId: Int) = apiService.rejectApplication(requestId)
}