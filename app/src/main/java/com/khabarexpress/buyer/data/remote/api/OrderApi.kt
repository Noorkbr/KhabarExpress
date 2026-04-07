package com.khabarexpress.buyer.data.remote.api

import com.khabarexpress.buyer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface OrderApi {
    @POST("api/v1/orders")
    suspend fun placeOrder(
        @Header("Authorization") token: String,
        @Body request: PlaceOrderRequest
    ): Response<OrderDto>
    
    @GET("api/v1/orders/{id}")
    suspend fun getOrderById(
        @Header("Authorization") token: String,
        @Path("id") orderId: String
    ): Response<OrderDto>
    
    @GET("api/v1/orders")
    suspend fun getOrders(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: String? = null
    ): Response<OrderListResponse>
    
    @GET("api/v1/orders/{id}/tracking")
    suspend fun getOrderTracking(
        @Header("Authorization") token: String,
        @Path("id") orderId: String
    ): Response<OrderTrackingDto>
    
    @PUT("api/v1/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: String,
        @Body request: CancelOrderRequest
    ): Response<OrderDto>
    
    @POST("api/v1/orders/{id}/rate")
    suspend fun rateOrder(
        @Header("Authorization") token: String,
        @Path("id") orderId: String,
        @Body request: RateOrderRequest
    ): Response<Unit>
}
