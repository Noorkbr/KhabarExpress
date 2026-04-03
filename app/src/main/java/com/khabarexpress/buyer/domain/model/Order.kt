package com.khabarexpress.buyer.domain.model

data class Order(
    val id: String,
    val userId: String,
    val restaurantId: String,
    val restaurant: Restaurant,
    val items: List<CartItem>,
    val deliveryAddress: Address,
    val status: OrderStatus,
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val discount: Double,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val rider: Rider? = null,
    val estimatedDeliveryTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val specialInstructions: String? = null
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY_FOR_PICKUP,
    PICKED_UP,
    ON_THE_WAY,
    DELIVERED,
    CANCELLED
}

enum class PaymentMethod {
    BKASH,              // Most popular in Bangladesh
    NAGAD,              // 2nd most popular
    ROCKET,             // Dutch Bangla Bank
    UPAY,               // UCB Fintech
    SSL_COMMERZ,        // Card payments gateway
    CASH_ON_DELIVERY,   // Cash on delivery
    CREDIT_CARD,        // Legacy - keep for compatibility
    DEBIT_CARD          // Legacy - keep for compatibility
}

enum class PaymentStatus {
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}

data class Rider(
    val id: String,
    val name: String,
    val phone: String,
    val profileImageUrl: String? = null,
    val rating: Double,
    val vehicleNumber: String,
    val currentLatitude: Double,
    val currentLongitude: Double
)

data class OrderTracking(
    val orderId: String,
    val status: OrderStatus,
    val riderLocation: Location? = null,
    val estimatedArrival: Long? = null,
    val timeline: List<OrderTimelineEvent> = emptyList()
)

data class Location(
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis()
)

data class OrderTimelineEvent(
    val status: OrderStatus,
    val timestamp: Long,
    val message: String
)
