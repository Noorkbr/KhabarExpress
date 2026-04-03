package com.khabarexpress.buyer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val label: String,
    val houseNo: String,
    val roadNo: String,
    val area: String,
    val thana: String,
    val district: String,
    val division: String,
    val postalCode: String,
    val landmark: String?,
    val deliveryInstructions: String?,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean
)
