package com.keremsen.e_commerce.api

import com.keremsen.e_commerce.models.entityModel.Order
import com.keremsen.e_commerce.models.requestModel.CreateOrderRequest
import com.keremsen.e_commerce.models.requestModel.UpdateOrderStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface OrderApiService {

    @GET("api/orders")
    suspend fun getOrders(): Response<List<Order>>

    @GET("api/orders/{order_id}")
    suspend fun getOrderDetail(@Path("order_id") orderId: Int): Response<Order>

    @POST("api/orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<Map<String, Any>>

    @PUT("api/orders/{order_id}/status")
    suspend fun updateOrderStatus(@Path("order_id") orderId: Int, @Body request: UpdateOrderStatusRequest): Response<Map<String, Any>>

    @POST("api/orders/{order_id}/cancel")
    suspend fun cancelOrder(@Path("order_id") orderId: Int): Response<Map<String, String>>
}