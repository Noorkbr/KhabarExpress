package com.khabarexpress.buyer.data.mappers

import com.khabarexpress.buyer.data.remote.dto.*
import com.khabarexpress.buyer.domain.model.*

/**
 * Mapper functions for order related models
 */

/**
 * Convert OrderDto from API to domain Order model
 */
fun OrderDto.toDomainModel(): Order {
    return Order(
        id = id,
        userId = userId,
        restaurantId = restaurantId,
        restaurant = restaurant.toDomainModel(),
        items = items.map { it.toDomainModel() },
        deliveryAddress = deliveryAddress.toDomainModel(),
        status = status.toOrderStatus(),
        subtotal = subtotal,
        deliveryFee = deliveryFee,
        tax = tax,
        discount = discount,
        total = total,
        paymentMethod = paymentMethod.toPaymentMethod(),
        paymentStatus = paymentStatus.toPaymentStatus(),
        rider = rider?.toDomainModel(),
        estimatedDeliveryTime = estimatedDeliveryTime,
        createdAt = createdAt,
        updatedAt = updatedAt,
        specialInstructions = specialInstructions
    )
}

/**
 * Convert OrderItemDto to domain CartItem model
 */
fun OrderItemDto.toDomainModel(): CartItem {
    val menuItem = MenuItem(
        id = menuItemId,
        name = name,
        description = description,
        price = price,
        imageUrl = null,
        isVegetarian = false,
        isVegan = false,
        isGlutenFree = false,
        customizations = emptyList(),
        isAvailable = true,
        rating = 0.0,
        totalOrders = 0
    )
    
    return CartItem(
        id = menuItemId,
        menuItem = menuItem,
        quantity = quantity,
        selectedCustomizations = emptyList(), // Customizations are stored as strings in DTO
        specialInstructions = null
    )
}

/**
 * Convert RiderDto to domain Rider model
 */
fun RiderDto.toDomainModel(): Rider {
    return Rider(
        id = id,
        name = name,
        phone = phone,
        profileImageUrl = profileImageUrl,
        rating = rating,
        vehicleNumber = vehicleNumber,
        currentLatitude = currentLatitude,
        currentLongitude = currentLongitude
    )
}

/**
 * Convert OrderTrackingDto to domain OrderTracking model
 */
fun OrderTrackingDto.toDomainModel(): OrderTracking {
    return OrderTracking(
        orderId = orderId,
        status = status.toOrderStatus(),
        riderLocation = riderLocation?.toDomainModel(),
        estimatedArrival = estimatedArrival,
        timeline = timeline.map { it.toDomainModel() }
    )
}

/**
 * Convert LocationDto to domain Location model
 */
fun LocationDto.toDomainModel(): Location {
    return Location(
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )
}

/**
 * Convert OrderTimelineEventDto to domain OrderTimelineEvent model
 */
fun OrderTimelineEventDto.toDomainModel(): OrderTimelineEvent {
    return OrderTimelineEvent(
        status = status.toOrderStatus(),
        timestamp = timestamp,
        message = message
    )
}

/**
 * Convert string status to OrderStatus enum
 */
fun String.toOrderStatus(): OrderStatus {
    return when (this.uppercase()) {
        "PENDING" -> OrderStatus.PENDING
        "CONFIRMED" -> OrderStatus.CONFIRMED
        "PREPARING" -> OrderStatus.PREPARING
        "READY_FOR_PICKUP" -> OrderStatus.READY_FOR_PICKUP
        "PICKED_UP" -> OrderStatus.PICKED_UP
        "ON_THE_WAY" -> OrderStatus.ON_THE_WAY
        "DELIVERED" -> OrderStatus.DELIVERED
        "CANCELLED" -> OrderStatus.CANCELLED
        else -> OrderStatus.PENDING
    }
}

/**
 * Convert string payment method to PaymentMethod enum
 */
fun String.toPaymentMethod(): PaymentMethod {
    return when (this.uppercase()) {
        "BKASH" -> PaymentMethod.BKASH
        "NAGAD" -> PaymentMethod.NAGAD
        "ROCKET" -> PaymentMethod.ROCKET
        "UPAY" -> PaymentMethod.UPAY
        "SSL_COMMERZ" -> PaymentMethod.SSL_COMMERZ
        "CASH_ON_DELIVERY", "COD" -> PaymentMethod.CASH_ON_DELIVERY
        "CREDIT_CARD" -> PaymentMethod.CREDIT_CARD
        "DEBIT_CARD" -> PaymentMethod.DEBIT_CARD
        else -> PaymentMethod.CASH_ON_DELIVERY
    }
}

/**
 * Convert string payment status to PaymentStatus enum
 */
fun String.toPaymentStatus(): PaymentStatus {
    return when (this.uppercase()) {
        "PENDING" -> PaymentStatus.PENDING
        "PAID" -> PaymentStatus.PAID
        "FAILED" -> PaymentStatus.FAILED
        "REFUNDED" -> PaymentStatus.REFUNDED
        else -> PaymentStatus.PENDING
    }
}
