package com.khabarexpress.buyer.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderDto(
    val id: String,
    val userId: String,
    val restaurantId: String,
    val restaurant: RestaurantDto,
    val items: List<OrderItemDto>,
    val deliveryAddress: AddressDto,
    val status: String,
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    val paymentMethod: String,
    val paymentStatus: String,
    val rider: RiderDto? = null,
    val estimatedDeliveryTime: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val specialInstructions: String? = null
)

@Serializable
data class OrderItemDto(
    val menuItemId: String,
    val name: String,
    val description: String,
    val price: Double,
    val quantity: Int,
    val customizations: List<String> = emptyList()
)

@Serializable
data class AddressDto(
    val id: String,
    val label: String,
    val houseNo: String,
    val roadNo: String,
    val area: String,
    val thana: String,
    val district: String,
    val division: String,
    val postalCode: String,
    val landmark: String? = null,
    val deliveryInstructions: String? = null,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class RiderDto(
    val id: String,
    val name: String,
    val phone: String,
    val profileImageUrl: String? = null,
    val rating: Double,
    val vehicleNumber: String,
    val currentLatitude: Double,
    val currentLongitude: Double
)

@Serializable
data class OrderListResponse(
    val success: Boolean,
    val orders: List<OrderDto>,
    val page: Int,
    val totalPages: Int,
    val totalCount: Int
)

@Serializable
data class PlaceOrderRequest(
    val restaurantId: String,
    val items: List<OrderItemRequest>,
    val deliveryAddressId: String,
    val paymentMethod: String,
    val specialInstructions: String? = null,
    val promoCode: String? = null
)

@Serializable
data class OrderItemRequest(
    val menuItemId: String,
    val quantity: Int,
    val customizations: List<String> = emptyList()
)

@Serializable
data class OrderTrackingDto(
    val orderId: String,
    val status: String,
    val riderLocation: LocationDto? = null,
    val estimatedArrival: Long? = null,
    val timeline: List<OrderTimelineEventDto>
)

@Serializable
data class LocationDto(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)

@Serializable
data class OrderTimelineEventDto(
    val status: String,
    val timestamp: Long,
    val message: String
)

@Serializable
data class CancelOrderRequest(
    val reason: String
)

@Serializable
data class RateOrderRequest(
    val restaurantRating: Double,
    val restaurantReview: String? = null,
    val riderRating: Double? = null,
    val riderReview: String? = null
)
