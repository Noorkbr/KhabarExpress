package com.khabarexpress.seller.domain.model

data class RestaurantInfo(
    val id: String,
    val name: String,
    val description: String? = null,
    val phone: String,
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
    val approvalStatus: String = "pending"
)

data class Order(
    val id: String,
    val orderNumber: String? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<OrderItem> = emptyList(),
    val status: String = "pending",
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val vat: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String = "cod",
    val paymentStatus: String = "pending",
    val specialInstructions: String? = null,
    val createdAt: String? = null
)

data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val customizations: List<String> = emptyList()
)

data class MenuItem(
    val id: String,
    val name: String,
    val nameBn: String? = null,
    val description: String? = null,
    val image: String? = null,
    val category: String? = null,
    val categoryName: String? = null,
    val price: Double = 0.0,
    val discountPrice: Double? = null,
    val isVegetarian: Boolean = false,
    val isHalal: Boolean = true,
    val spiceLevel: String = "None",
    val prepTime: Int = 15,
    val rating: Double = 0.0,
    val totalOrders: Int = 0,
    val isAvailable: Boolean = true,
    val isPopular: Boolean = false
)

data class Analytics(
    val totalOrders: Int = 0,
    val totalRevenue: Double = 0.0,
    val averageOrderValue: Double = 0.0,
    val period: String = "today"
)
