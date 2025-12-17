package com.keremsen.e_commerce.api


import com.keremsen.e_commerce.models.requestModel.SellerRequest
import com.keremsen.e_commerce.models.requestModel.SellerRequestApply
import com.keremsen.e_commerce.models.responseModel.SellerRequestResponse
import retrofit2.Response
import retrofit2.http.*

interface SellerRequestApiService {

    @POST("api/seller_requests/apply")
    suspend fun applyForSeller(@Body request: SellerRequestApply): Response<SellerRequestResponse>

    @GET("api/seller_requests/pending")
    suspend fun getPendingApplications(): Response<List<SellerRequest>>

    @POST("api/seller_requests/{request_id}/approve")
    suspend fun approveApplication(@Path("request_id") requestId: Int): Response<SellerRequestResponse>

    @POST("api/seller_requests/{request_id}/reject")
    suspend fun rejectApplication(@Path("request_id") requestId: Int): Response<SellerRequestResponse>

}