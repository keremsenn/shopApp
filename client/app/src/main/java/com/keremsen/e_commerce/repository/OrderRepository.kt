package com.keremsen.e_commerce.repository


import com.keremsen.e_commerce.api.OrderApiService
import com.keremsen.e_commerce.models.requestModel.CreateOrderRequest
import com.keremsen.e_commerce.models.requestModel.OrderItemRequest
import javax.inject.Inject

class OrderRepository @Inject constructor(
    private val apiService: OrderApiService
) {
    suspend fun getOrders() = apiService.getOrders()

    suspend fun getOrderDetail(orderId: Int) = apiService.getOrderDetail(orderId)

    suspend fun createOrder(addressId: Int, items: List<OrderItemRequest>? = null) =
        apiService.createOrder(CreateOrderRequest(addressId, items))

    suspend fun cancelOrder(orderId: Int) = apiService.cancelOrder(orderId)
}

