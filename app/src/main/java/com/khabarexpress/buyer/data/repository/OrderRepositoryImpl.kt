package com.khabarexpress.buyer.data.repository

import com.khabarexpress.buyer.data.local.dao.CartDao
import com.khabarexpress.buyer.data.local.preferences.AppPreferences
import com.khabarexpress.buyer.data.mappers.toDomainModel
import com.khabarexpress.buyer.data.remote.api.OrderApi
import com.khabarexpress.buyer.data.remote.dto.*
import com.khabarexpress.buyer.domain.model.*
import com.khabarexpress.buyer.domain.repository.OrderRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of OrderRepository
 * Handles order operations including placing orders, tracking, and order history
 * Uses OrderApi for backend communication
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderApi: OrderApi,
    private val appPreferences: AppPreferences,
    private val cartDao: CartDao
) : OrderRepository {

    /**
     * Place a new order
     */
    override suspend fun placeOrder(
        restaurantId: String,
        deliveryAddress: Address,
        paymentMethod: PaymentMethod,
        specialInstructions: String?
    ): Result<Order> {
        return try {
            val token = appPreferences.getAuthTokenSync()
                ?: return Result.failure(Exception("Not authenticated"))
            
            // Build order items from current cart
            val cartItems = cartDao.getAllCartItems().first()
            if (cartItems.isEmpty()) {
                return Result.failure(Exception("Cart is empty"))
            }
            
            val orderItems = cartItems.map { entity ->
                // Parse customization IDs from the stored JSON
                val customizationIds = try {
                    if (entity.customizations.isNullOrEmpty()) {
                        emptyList()
                    } else {
                        kotlinx.serialization.json.Json.decodeFromString<List<com.khabarexpress.buyer.domain.model.SelectedCustomization>>(entity.customizations)
                            .map { it.choice.id }
                    }
                } catch (e: Exception) {
                    emptyList()
                }
                
                OrderItemRequest(
                    menuItemId = entity.menuItemId,
                    quantity = entity.quantity,
                    customizations = customizationIds
                )
            }
            
            val request = PlaceOrderRequest(
                restaurantId = restaurantId,
                items = orderItems,
                deliveryAddressId = deliveryAddress.id,
                paymentMethod = paymentMethod.name,
                specialInstructions = specialInstructions,
                promoCode = null
            )
            
            val response = orderApi.placeOrder("Bearer $token", request)
            
            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!.toDomainModel()
                Result.success(order)
            } else {
                Result.failure(Exception("Failed to place order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Get order by ID
     */
    override suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val token = appPreferences.getAuthTokenSync()
                ?: return Result.failure(Exception("Not authenticated"))
            
            val response = orderApi.getOrderById("Bearer $token", orderId)
            
            if (response.isSuccessful && response.body() != null) {
                val order = response.body()!!.toDomainModel()
                Result.success(order)
            } else {
                Result.failure(Exception("Order not found: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Get all user orders as Flow
     */
    override fun getUserOrders(): Flow<List<Order>> = flow {
        try {
            val token = appPreferences.getAuthTokenSync()
            if (token == null) {
                emit(emptyList())
                return@flow
            }
            
            val response = orderApi.getOrders("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!.orders.map { it.toDomainModel() }
                emit(orders)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Get active orders (non-delivered/cancelled) as Flow
     */
    override fun getActiveOrders(): Flow<List<Order>> = flow {
        try {
            val token = appPreferences.getAuthTokenSync()
            if (token == null) {
                emit(emptyList())
                return@flow
            }
            
            // Get all orders and filter active ones
            val response = orderApi.getOrders("Bearer $token")
            
            if (response.isSuccessful && response.body() != null) {
                val orders = response.body()!!.orders
                    .map { it.toDomainModel() }
                    .filter { 
                        it.status != OrderStatus.DELIVERED && 
                        it.status != OrderStatus.CANCELLED 
                    }
                emit(orders)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Cancel order
     */
    override suspend fun cancelOrder(orderId: String, reason: String): Result<Unit> {
        return try {
            val token = appPreferences.getAuthTokenSync()
                ?: return Result.failure(Exception("Not authenticated"))
            
            val request = CancelOrderRequest(reason)
            val response = orderApi.cancelOrder("Bearer $token", orderId, request)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to cancel order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Track order in real-time
     * Returns Flow that emits tracking updates
     */
    override fun trackOrder(orderId: String): Flow<OrderTracking> = flow {
        // This would ideally use WebSocket or Server-Sent Events for real-time updates
        // For now, polling the tracking endpoint
        while (true) {
            try {
                val token = appPreferences.getAuthTokenSync()
                if (token == null) {
                    break
                }
                
                val response = orderApi.getOrderTracking("Bearer $token", orderId)
                
                if (response.isSuccessful && response.body() != null) {
                    val tracking = response.body()!!.toDomainModel()
                    emit(tracking)
                    
                    // Stop tracking if order is delivered or cancelled
                    if (tracking.status == OrderStatus.DELIVERED || 
                        tracking.status == OrderStatus.CANCELLED) {
                        break
                    }
                }
                
                // Poll every 10 seconds
                delay(10000)
            } catch (e: Exception) {
                // Continue tracking even on errors
                delay(10000)
            }
        }
    }

    /**
     * Rate order
     */
    override suspend fun rateOrder(
        orderId: String,
        rating: Int,
        review: String?
    ): Result<Unit> {
        return try {
            val token = appPreferences.getAuthTokenSync()
                ?: return Result.failure(Exception("Not authenticated"))
            
            val request = RateOrderRequest(
                restaurantRating = rating.toDouble(),
                restaurantReview = review,
                riderRating = null,
                riderReview = null
            )
            
            val response = orderApi.rateOrder("Bearer $token", orderId, request)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to rate order: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}
