package com.khabarexpress.seller.domain.repository

import com.khabarexpress.seller.domain.model.Analytics
import com.khabarexpress.seller.domain.model.RestaurantInfo

interface RestaurantRepository {
    suspend fun getProfile(): Result<RestaurantInfo>
    suspend fun updateProfile(
        name: String? = null,
        description: String? = null,
        phone: String? = null,
        cuisines: List<String>? = null
    ): Result<RestaurantInfo>
    suspend fun updateOperatingHours(
        openingTime: String,
        closingTime: String,
        openDays: List<String>? = null
    ): Result<RestaurantInfo>
    suspend fun toggleOpenStatus(): Result<Boolean>
    suspend fun getAnalytics(period: String = "today"): Result<Analytics>
}
