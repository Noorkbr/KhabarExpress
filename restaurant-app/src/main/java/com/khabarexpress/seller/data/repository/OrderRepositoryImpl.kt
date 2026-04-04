package com.khabarexpress.seller.data.repository

import com.khabarexpress.seller.data.local.preferences.AppPreferences
import com.khabarexpress.seller.data.remote.api.OrderApi
import com.khabarexpress.seller.data.remote.dto.AcceptOrderRequest
import com.khabarexpress.seller.data.remote.dto.RejectOrderRequest
import com.khabarexpress.seller.data.remote.dto.UpdateStatusRequest
import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.model.OrderItem
import com.khabarexpress.seller.domain.repository.OrderRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderApi: OrderApi,
    private val appPreferences: AppPreferences
) : OrderRepository {

    private suspend fun getToken(): String = "Bearer ${appPreferences.getAuthTokenSync() ?: ""}"

    override suspend fun getOrders(page: Int, status: String?): Result<List<Order>> {
        return try {
            val response = orderApi.getRestaurantOrders(getToken(), page, status = status)
            if (response.isSuccessful && response.body()?.success == true) {
                val orders = response.body()!!.data?.orders?.map { dto ->
                    Order(
                        id = dto.id ?: "",
                        orderNumber = dto.orderNumber,
                        customerName = dto.user?.name ?: "",
                        customerPhone = dto.user?.phone ?: "",
                        items = dto.items.map { item ->
                            OrderItem(
                                name = item.name,
                                quantity = item.quantity,
                                price = item.price,
                                customizations = item.customizations.map { "${it.name}: ${it.option}" }
                            )
                        },
                        status = dto.status,
                        subtotal = dto.subtotal,
                        deliveryFee = dto.deliveryFee,
                        vat = dto.vat,
                        discount = dto.discount,
                        total = dto.total,
                        paymentMethod = dto.paymentMethod,
                        paymentStatus = dto.paymentStatus,
                        specialInstructions = dto.specialInstructions,
                        createdAt = dto.createdAt
                    )
                } ?: emptyList()
                Result.success(orders)
            } else {
                Result.failure(Exception("Failed to load orders: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val response = orderApi.getOrderById(getToken(), orderId)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data?.order
                if (dto != null) {
                    Result.success(Order(
                        id = dto.id ?: "",
                        orderNumber = dto.orderNumber,
                        customerName = dto.user?.name ?: "",
                        customerPhone = dto.user?.phone ?: "",
                        items = dto.items.map { item ->
                            OrderItem(name = item.name, quantity = item.quantity, price = item.price)
                        },
                        status = dto.status,
                        total = dto.total,
                        paymentMethod = dto.paymentMethod,
                        createdAt = dto.createdAt
                    ))
                } else {
                    Result.failure(Exception("Order not found"))
                }
            } else {
                Result.failure(Exception("Failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun acceptOrder(orderId: String, estimatedPrepTime: Int?): Result<Order> {
        return try {
            val response = orderApi.acceptOrder(getToken(), orderId, AcceptOrderRequest(estimatedPrepTime))
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data?.order
                Result.success(Order(
                    id = dto?.id ?: orderId,
                    status = dto?.status ?: "confirmed",
                    total = dto?.total ?: 0.0,
                    customerName = dto?.user?.name ?: ""
                ))
            } else {
                Result.failure(Exception("Failed to accept order"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun rejectOrder(orderId: String, reason: String?): Result<Order> {
        return try {
            val response = orderApi.rejectOrder(getToken(), orderId, RejectOrderRequest(reason))
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data?.order
                Result.success(Order(
                    id = dto?.id ?: orderId,
                    status = dto?.status ?: "cancelled",
                    total = dto?.total ?: 0.0,
                    customerName = dto?.user?.name ?: ""
                ))
            } else {
                Result.failure(Exception("Failed to reject order"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> {
        return try {
            val response = orderApi.updateOrderStatus(getToken(), orderId, UpdateStatusRequest(status))
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data?.order
                Result.success(Order(
                    id = dto?.id ?: orderId,
                    status = dto?.status ?: status,
                    total = dto?.total ?: 0.0,
                    customerName = dto?.user?.name ?: ""
                ))
            } else {
                Result.failure(Exception("Failed to update status"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}
