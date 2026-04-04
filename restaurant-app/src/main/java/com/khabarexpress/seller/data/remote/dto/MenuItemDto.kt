package com.khabarexpress.seller.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MenuItemListResponse(
    val success: Boolean,
    val data: List<MenuItemDataDto> = emptyList()
)

@Serializable
data class MenuItemResponse(
    val success: Boolean,
    val message: String? = null,
    val data: MenuItemDataDto? = null
)

@Serializable
data class MenuItemDataDto(
    val id: String? = null,
    val name: String = "",
    val nameBn: String? = null,
    val description: String? = null,
    val descriptionBn: String? = null,
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

@Serializable
data class CreateMenuItemRequest(
    val name: String,
    val nameBn: String? = null,
    val description: String? = null,
    val descriptionBn: String? = null,
    val image: String? = null,
    val category: String,
    val categoryName: String? = null,
    val price: Double,
    val discountPrice: Double? = null,
    val isVegetarian: Boolean = false,
    val isHalal: Boolean = true,
    val spiceLevel: String = "None",
    val prepTime: Int = 15,
    val isAvailable: Boolean = true
)

@Serializable
data class UpdateMenuItemRequest(
    val name: String? = null,
    val nameBn: String? = null,
    val description: String? = null,
    val descriptionBn: String? = null,
    val image: String? = null,
    val category: String? = null,
    val categoryName: String? = null,
    val price: Double? = null,
    val discountPrice: Double? = null,
    val isVegetarian: Boolean? = null,
    val isHalal: Boolean? = null,
    val spiceLevel: String? = null,
    val prepTime: Int? = null,
    val isAvailable: Boolean? = null
)
