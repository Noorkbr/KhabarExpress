package com.khabarexpress.seller.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderListResponse(
    val success: Boolean,
    val data: OrderListDataDto? = null
)

@Serializable
data class OrderListDataDto(
    val orders: List<OrderDto> = emptyList(),
    val pagination: PaginationDto? = null
)

@Serializable
data class PaginationDto(
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val pages: Int = 0
)

@Serializable
data class OrderResponse(
    val success: Boolean,
    val message: String? = null,
    val data: OrderDataDto? = null
)

@Serializable
data class OrderDataDto(
    val order: OrderDto? = null
)

@Serializable
data class OrderDto(
    val id: String? = null,
    val orderNumber: String? = null,
    val user: OrderUserDto? = null,
    val items: List<OrderItemDto> = emptyList(),
    val status: String = "pending",
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val vat: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String = "cod",
    val paymentStatus: String = "pending",
    val specialInstructions: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class OrderUserDto(
    val id: String? = null,
    val name: String = "",
    val phone: String = ""
)

@Serializable
data class OrderItemDto(
    val name: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val customizations: List<OrderCustomizationDto> = emptyList()
)

@Serializable
data class OrderCustomizationDto(
    val name: String = "",
    val option: String = "",
    val price: Double = 0.0
)

@Serializable
data class AcceptOrderRequest(
    val estimatedPrepTime: Int? = null
)

@Serializable
data class RejectOrderRequest(
    val reason: String? = null
)

@Serializable
data class UpdateStatusRequest(
    val status: String,
    val note: String? = null
)
