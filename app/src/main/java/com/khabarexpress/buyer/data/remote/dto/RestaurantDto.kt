package com.khabarexpress.buyer.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RestaurantDto(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val coverImageUrl: String? = null,
    val cuisine: List<String>,
    val rating: Double,
    val totalReviews: Int,
    val deliveryTime: Int,
    val deliveryFee: Double,
    val minOrderAmount: Double,
    val isOpen: Boolean,
    val distance: Double,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val tags: List<String> = emptyList(),
    val approvalStatus: String = "approved"
)

@Serializable
data class RestaurantListResponse(
    val success: Boolean,
    val restaurants: List<RestaurantDto>,
    val page: Int,
    val totalPages: Int,
    val totalCount: Int
)

@Serializable
data class MenuCategoryDto(
    val id: String,
    val name: String,
    val items: List<MenuItemDto>
)

@Serializable
data class MenuItemDto(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String? = null,
    val isVegetarian: Boolean = false,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val customizations: List<CustomizationOptionDto> = emptyList(),
    val isAvailable: Boolean = true,
    val rating: Double = 0.0,
    val totalOrders: Int = 0
)

@Serializable
data class CustomizationOptionDto(
    val id: String,
    val name: String,
    val type: String, // "SINGLE_SELECT", "MULTIPLE_SELECT"
    val options: List<CustomizationChoiceDto>,
    val isRequired: Boolean = false
)

@Serializable
data class CustomizationChoiceDto(
    val id: String,
    val name: String,
    val additionalPrice: Double = 0.0
)

@Serializable
data class MenuResponse(
    val success: Boolean,
    val categories: List<MenuCategoryDto>
)

@Serializable
data class ReviewDto(
    val id: String,
    val userId: String,
    val userName: String,
    val userImageUrl: String? = null,
    val rating: Double,
    val comment: String,
    val createdAt: Long
)

@Serializable
data class ReviewListResponse(
    val success: Boolean,
    val reviews: List<ReviewDto>,
    val page: Int,
    val totalPages: Int,
    val averageRating: Double
)

@Serializable
data class SubmitReviewRequest(
    val rating: Double,
    val comment: String
)

@Serializable
data class SearchResultResponse(
    val success: Boolean,
    val restaurants: List<RestaurantDto>,
    val dishes: List<MenuItemDto>,
    val totalResults: Int
)
