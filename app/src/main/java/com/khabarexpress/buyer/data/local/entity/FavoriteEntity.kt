package com.khabarexpress.buyer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val restaurantId: String,
    val restaurantName: String,
    val restaurantImageUrl: String,
    val cuisine: String, // Comma-separated list
    val rating: Double,
    val deliveryTime: Int,
    val addedAt: Long
)
