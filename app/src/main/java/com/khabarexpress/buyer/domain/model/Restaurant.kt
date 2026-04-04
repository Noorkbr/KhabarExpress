package com.khabarexpress.buyer.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val coverImageUrl: String? = null,
    val cuisine: List<String>,
    val rating: Double,
    val totalReviews: Int,
    val deliveryTime: Int, // in minutes
    val deliveryFee: Double,
    val minOrderAmount: Double,
    val isOpen: Boolean,
    val distance: Double, // in km
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val tags: List<String> = emptyList(), // Featured, Popular, New, etc.
    val categories: List<MenuCategory> = emptyList(),
    val approvalStatus: String = "approved" // "pending", "approved", "rejected"
)

data class MenuCategory(
    val id: String,
    val name: String,
    val items: List<MenuItem> = emptyList()
)

data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String? = null,
    val isVegetarian: Boolean = false,
    val isVegan: Boolean = false,
    val isGlutenFree: Boolean = false,
    val customizations: List<CustomizationOption> = emptyList(),
    val isAvailable: Boolean = true,
    val rating: Double = 0.0,
    val totalOrders: Int = 0
)

data class CustomizationOption(
    val id: String,
    val name: String,
    val type: CustomizationType,
    val options: List<CustomizationChoice>,
    val isRequired: Boolean = false
)

enum class CustomizationType {
    SINGLE_SELECT, MULTIPLE_SELECT
}

data class CustomizationChoice(
    val id: String,
    val name: String,
    val additionalPrice: Double = 0.0
)
