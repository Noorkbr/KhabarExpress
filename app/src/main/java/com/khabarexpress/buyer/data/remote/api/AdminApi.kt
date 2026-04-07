package com.khabarexpress.buyer.data.remote.api

import com.khabarexpress.buyer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AdminApi {
    
    @GET("api/v1/admin/dashboard")
    suspend fun getDashboardStats(
        @Header("Authorization") token: String
    ): Response<AdminDashboardResponse>
    
    @GET("api/v1/admin/profit")
    suspend fun getProfitAnalytics(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("groupBy") groupBy: String = "daily"
    ): Response<ProfitAnalyticsResponse>
    
    @GET("api/v1/admin/verification-stats")
    suspend fun getVerificationStats(
        @Header("Authorization") token: String
    ): Response<VerificationStatsResponse>
    
    @GET("api/v1/restaurants/admin/pending")
    suspend fun getPendingRestaurants(
        @Header("Authorization") token: String
    ): Response<PendingRestaurantListResponse>
    
    @PATCH("api/v1/restaurants/admin/{id}/approve")
    suspend fun approveRestaurant(
        @Header("Authorization") token: String,
        @Path("id") restaurantId: String
    ): Response<ApproveRejectResponse>
    
    @PATCH("api/v1/restaurants/admin/{id}/reject")
    suspend fun rejectRestaurant(
        @Header("Authorization") token: String,
        @Path("id") restaurantId: String,
        @Body request: RejectRequest
    ): Response<ApproveRejectResponse>
}
