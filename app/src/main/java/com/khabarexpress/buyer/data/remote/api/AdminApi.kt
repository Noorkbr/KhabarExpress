package com.khabarexpress.buyer.data.remote.api

import com.khabarexpress.buyer.data.remote.dto.*
import retrofit2.http.*

interface AdminApi {
    
    @GET("admin/dashboard")
    suspend fun getDashboardStats(
        @Header("Authorization") token: String
    ): AdminDashboardResponse
    
    @GET("admin/profit")
    suspend fun getProfitAnalytics(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("groupBy") groupBy: String = "daily"
    ): ProfitAnalyticsResponse
    
    @GET("admin/verification-stats")
    suspend fun getVerificationStats(
        @Header("Authorization") token: String
    ): VerificationStatsResponse
    
    @GET("restaurants/admin/pending")
    suspend fun getPendingRestaurants(
        @Header("Authorization") token: String
    ): PendingRestaurantListResponse
    
    @PATCH("restaurants/admin/{id}/approve")
    suspend fun approveRestaurant(
        @Header("Authorization") token: String,
        @Path("id") restaurantId: String
    ): ApproveRejectResponse
    
    @PATCH("restaurants/admin/{id}/reject")
    suspend fun rejectRestaurant(
        @Header("Authorization") token: String,
        @Path("id") restaurantId: String,
        @Body request: RejectRequest
    ): ApproveRejectResponse
}
