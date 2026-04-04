package com.khabarexpress.buyer.data.remote.dto

import kotlinx.serialization.Serializable

// Admin Dashboard DTOs
@Serializable
data class AdminDashboardResponse(
    val success: Boolean,
    val data: AdminDashboardData
)

@Serializable
data class AdminDashboardData(
    val today: TodayStats,
    val totals: TotalStats,
    val pending: PendingStats,
    val active: ActiveStats
)

@Serializable
data class TodayStats(
    val orders: Int,
    val revenue: Double
)

@Serializable
data class TotalStats(
    val users: Int,
    val restaurants: Int,
    val riders: Int,
    val orders: Int
)

@Serializable
data class PendingStats(
    val restaurants: Int,
    val riders: Int
)

@Serializable
data class ActiveStats(
    val orders: Int,
    val riders: Int
)

// Restaurant Verification DTOs
@Serializable
data class PendingRestaurantListResponse(
    val success: Boolean,
    val data: List<PendingRestaurantDto>
)

@Serializable
data class PendingRestaurantDto(
    val id: String,
    val name: String,
    val nameBn: String? = null,
    val phone: String,
    val email: String? = null,
    val cuisines: List<String> = emptyList(),
    val category: String = "Restaurant",
    val address: RestaurantAddressDto? = null,
    val approvalStatus: String = "pending",
    val createdAt: String? = null
)

@Serializable
data class RestaurantAddressDto(
    val street: String? = null,
    val area: String? = null,
    val thana: String? = null,
    val district: String? = null,
    val postalCode: String? = null
)

@Serializable
data class ApproveRejectResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class RejectRequest(
    val reason: String
)

// Profit Analytics DTOs
@Serializable
data class ProfitAnalyticsResponse(
    val success: Boolean,
    val data: ProfitAnalyticsData
)

@Serializable
data class ProfitAnalyticsData(
    val summary: ProfitSummary,
    val timeline: List<ProfitTimelineEntry>,
    val byPaymentMethod: List<ProfitByMethod>,
    val adminProfitRate: Double
)

@Serializable
data class ProfitSummary(
    val totalAmount: Double = 0.0,
    val totalAdminProfit: Double = 0.0,
    val totalRestaurantPayout: Double = 0.0,
    val totalTransactions: Int = 0,
    val avgAdminProfit: Double = 0.0
)

@Serializable
data class ProfitTimelineEntry(
    val totalAmount: Double,
    val adminProfit: Double,
    val restaurantPayout: Double,
    val transactionCount: Int
)

@Serializable
data class ProfitByMethod(
    val method: String? = null,
    val totalAmount: Double,
    val adminProfit: Double,
    val restaurantPayout: Double,
    val transactionCount: Int
)

// Verification Stats DTO
@Serializable
data class VerificationStatsResponse(
    val success: Boolean,
    val data: VerificationStatsData
)

@Serializable
data class VerificationStatsData(
    val pending: Int,
    val approved: Int,
    val rejected: Int,
    val suspended: Int,
    val total: Int
)
