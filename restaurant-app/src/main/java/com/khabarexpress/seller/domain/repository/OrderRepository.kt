package com.khabarexpress.seller.domain.repository

import com.khabarexpress.seller.domain.model.Order

interface OrderRepository {
    suspend fun getOrders(page: Int = 1, status: String? = null): Result<List<Order>>
    suspend fun getOrderById(orderId: String): Result<Order>
    suspend fun acceptOrder(orderId: String, estimatedPrepTime: Int? = null): Result<Order>
    suspend fun rejectOrder(orderId: String, reason: String? = null): Result<Order>
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Order>
}
