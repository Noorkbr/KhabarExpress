package com.khabarexpress.seller.domain.repository

import com.khabarexpress.seller.domain.model.MenuItem

interface MenuRepository {
    suspend fun getMenuItems(): Result<List<MenuItem>>
    suspend fun createMenuItem(
        name: String,
        description: String?,
        category: String,
        price: Double,
        isAvailable: Boolean = true,
        isHalal: Boolean = true,
        spiceLevel: String = "None",
        prepTime: Int = 15
    ): Result<MenuItem>
    suspend fun updateMenuItem(
        id: String,
        name: String? = null,
        description: String? = null,
        category: String? = null,
        price: Double? = null,
        isAvailable: Boolean? = null
    ): Result<MenuItem>
    suspend fun toggleAvailability(id: String): Result<MenuItem>
    suspend fun deleteMenuItem(id: String): Result<Unit>
}
