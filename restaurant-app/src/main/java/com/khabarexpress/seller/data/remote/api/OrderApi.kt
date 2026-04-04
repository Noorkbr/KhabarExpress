package com.khabarexpress.seller.data.remote.api

import com.khabarexpress.seller.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @GET("orders/restaurant/my")
    suspend fun getRestaurantOrders(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null
    ): Response<OrderListResponse>

    @GET("orders/{id}")
    suspend fun getOrderById(
        @Header("Authorization") token: String,
        @Path("id") orderId: String
    ): Response<OrderResponse>

    @PATCH("orders/{id}/accept")
    suspend fun acceptOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: String,
        @Body request: AcceptOrderRequest
    ): Response<OrderResponse>

    @PATCH("orders/{id}/reject")
    suspend fun rejectOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: String,
        @Body request: RejectOrderRequest
    ): Response<OrderResponse>

    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Path("id") orderId: String,
        @Body request: UpdateStatusRequest
    ): Response<OrderResponse>
}
