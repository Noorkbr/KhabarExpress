package com.khabarexpress.buyer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val restaurantId: String,
    val restaurantName: String,
    val menuItemId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String?,
    val quantity: Int,
    val customizations: String?, // JSON string of customizations
    val addedAt: Long
)
