package com.khabarexpress.buyer.domain.repository

import com.khabarexpress.buyer.data.remote.dto.*

interface AdminRepository {
    suspend fun getDashboardStats(): Result<AdminDashboardData>
    suspend fun getProfitAnalytics(
        startDate: String? = null,
        endDate: String? = null,
        groupBy: String = "daily"
    ): Result<ProfitAnalyticsData>
    suspend fun getVerificationStats(): Result<VerificationStatsData>
    suspend fun getPendingRestaurants(): Result<List<PendingRestaurantDto>>
    suspend fun approveRestaurant(restaurantId: String): Result<String>
    suspend fun rejectRestaurant(restaurantId: String, reason: String): Result<String>
}
