package com.khabarexpress.buyer.domain.repository

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.OrderTracking
import com.khabarexpress.buyer.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for order operations
 */
interface OrderRepository {
    
    /**
     * Place a new order
     */
    suspend fun placeOrder(
        restaurantId: String,
        deliveryAddress: Address,
        paymentMethod: PaymentMethod,
        specialInstructions: String?
    ): Result<Order>
    
    /**
     * Get order by ID
     */
    suspend fun getOrderById(orderId: String): Result<Order>
    
    /**
     * Get all user orders
     */
    fun getUserOrders(): Flow<List<Order>>
    
    /**
     * Get active orders
     */
    fun getActiveOrders(): Flow<List<Order>>
    
    /**
     * Cancel order
     */
    suspend fun cancelOrder(orderId: String, reason: String): Result<Unit>
    
    /**
     * Track order in real-time
     */
    fun trackOrder(orderId: String): Flow<OrderTracking>
    
    /**
     * Rate order
     */
    suspend fun rateOrder(
        orderId: String,
        rating: Int,
        review: String?
    ): Result<Unit>
}
