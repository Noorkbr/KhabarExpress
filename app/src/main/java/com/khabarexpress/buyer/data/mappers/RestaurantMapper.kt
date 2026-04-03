package com.khabarexpress.buyer.data.mappers

import com.khabarexpress.buyer.data.local.entity.FavoriteEntity
import com.khabarexpress.buyer.data.remote.dto.*
import com.khabarexpress.buyer.domain.model.*

/**
 * Mapper functions for restaurant related models
 */

/**
 * Convert RestaurantDto from API to domain Restaurant model
 */
fun RestaurantDto.toDomainModel(): Restaurant {
    return Restaurant(
        id = id,
        name = name,
        description = description,
        imageUrl = imageUrl,
        coverImageUrl = coverImageUrl,
        cuisine = cuisine,
        rating = rating,
        totalReviews = totalReviews,
        deliveryTime = deliveryTime,
        deliveryFee = deliveryFee,
        minOrderAmount = minOrderAmount,
        isOpen = isOpen,
        distance = distance,
        latitude = latitude,
        longitude = longitude,
        address = address,
        tags = tags,
        categories = emptyList() // Menu categories are loaded separately
    )
}

/**
 * Convert RestaurantDto to FavoriteEntity for Room database
 */
fun RestaurantDto.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        restaurantId = id,
        restaurantName = name,
        restaurantImageUrl = imageUrl,
        cuisine = cuisine.joinToString(","),
        rating = rating,
        deliveryTime = deliveryTime,
        addedAt = System.currentTimeMillis()
    )
}

/**
 * Convert FavoriteEntity to domain Restaurant model
 */
fun FavoriteEntity.toDomainModel(): Restaurant {
    return Restaurant(
        id = restaurantId,
        name = restaurantName,
        description = "",
        imageUrl = restaurantImageUrl,
        coverImageUrl = null,
        cuisine = cuisine.split(","),
        rating = rating,
        totalReviews = 0,
        deliveryTime = deliveryTime,
        deliveryFee = 0.0,
        minOrderAmount = 0.0,
        isOpen = true,
        distance = 0.0,
        latitude = 0.0,
        longitude = 0.0,
        address = "",
        tags = emptyList(),
        categories = emptyList()
    )
}

/**
 * Convert MenuCategoryDto to domain MenuCategory model
 */
fun MenuCategoryDto.toDomainModel(): MenuCategory {
    return MenuCategory(
        id = id,
        name = name,
        items = items.map { it.toDomainModel() }
    )
}

/**
 * Convert MenuItemDto to domain MenuItem model
 */
fun MenuItemDto.toDomainModel(): MenuItem {
    return MenuItem(
        id = id,
        name = name,
        description = description,
        price = price,
        imageUrl = imageUrl,
        isVegetarian = isVegetarian,
        isVegan = isVegan,
        isGlutenFree = isGlutenFree,
        customizations = customizations.map { it.toDomainModel() },
        isAvailable = isAvailable,
        rating = rating,
        totalOrders = totalOrders
    )
}

/**
 * Convert CustomizationOptionDto to domain CustomizationOption model
 */
fun CustomizationOptionDto.toDomainModel(): CustomizationOption {
    return CustomizationOption(
        id = id,
        name = name,
        type = when (type) {
            "MULTIPLE_SELECT" -> CustomizationType.MULTIPLE_SELECT
            else -> CustomizationType.SINGLE_SELECT
        },
        options = options.map { it.toDomainModel() },
        isRequired = isRequired
    )
}

/**
 * Convert CustomizationChoiceDto to domain CustomizationChoice model
 */
fun CustomizationChoiceDto.toDomainModel(): CustomizationChoice {
    return CustomizationChoice(
        id = id,
        name = name,
        additionalPrice = additionalPrice
    )
}
