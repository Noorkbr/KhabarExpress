package com.khabarexpress.seller.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRestaurantRequest(
    val name: String,
    val phone: String,
    val email: String? = null,
    val description: String? = null,
    val cuisines: List<String> = emptyList(),
    val category: String = "Restaurant"
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val description: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val cuisines: List<String>? = null,
    val category: String? = null,
    val coverImage: String? = null,
    val logo: String? = null,
    val deliveryRadius: Double? = null,
    val minOrderAmount: Double? = null
)

@Serializable
data class RestaurantResponse(
    val success: Boolean,
    val message: String,
    val data: RestaurantDataDto? = null
)

@Serializable
data class RestaurantDataDto(
    val id: String? = null,
    val name: String = "",
    val nameBn: String? = null,
    val description: String? = null,
    val phone: String = "",
    val email: String? = null,
    val cuisines: List<String> = emptyList(),
    val category: String = "Restaurant",
    val coverImage: String? = null,
    val logo: String? = null,
    val rating: Double = 0.0,
    val totalReviews: Int = 0,
    val totalOrders: Int = 0,
    val isOpen: Boolean = false,
    val isActive: Boolean = false,
    val approvalStatus: String = "pending",
    val deliveryRadius: Double = 5.0,
    val minOrderAmount: Double = 0.0,
    val commission: Double = 18.0
)

@Serializable
data class ToggleStatusResponse(
    val success: Boolean,
    val message: String,
    val data: IsOpenDto? = null
)

@Serializable
data class IsOpenDto(
    val isOpen: Boolean
)

@Serializable
data class AnalyticsResponse(
    val success: Boolean,
    val data: AnalyticsDataDto? = null
)

@Serializable
data class AnalyticsDataDto(
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val period: String = "today"
)
