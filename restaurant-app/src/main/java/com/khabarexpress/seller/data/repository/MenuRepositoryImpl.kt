package com.khabarexpress.seller.data.repository

import com.khabarexpress.seller.data.local.preferences.AppPreferences
import com.khabarexpress.seller.data.remote.api.MenuApi
import com.khabarexpress.seller.data.remote.dto.CreateMenuItemRequest
import com.khabarexpress.seller.data.remote.dto.UpdateMenuItemRequest
import com.khabarexpress.seller.domain.model.MenuItem
import com.khabarexpress.seller.domain.repository.MenuRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val menuApi: MenuApi,
    private val appPreferences: AppPreferences
) : MenuRepository {

    private suspend fun getToken(): String = "Bearer ${appPreferences.getAuthTokenSync() ?: ""}"

    override suspend fun getMenuItems(): Result<List<MenuItem>> {
        return try {
            val restaurantId = appPreferences.getRestaurantId() ?: return Result.failure(Exception("No restaurant ID"))
            val response = menuApi.getMenuItems(restaurantId)
            if (response.isSuccessful && response.body()?.success == true) {
                val items = response.body()!!.data.map { dto ->
                    MenuItem(
                        id = dto.id ?: "",
                        name = dto.name,
                        nameBn = dto.nameBn,
                        description = dto.description,
                        image = dto.image,
                        category = dto.category,
                        categoryName = dto.categoryName,
                        price = dto.price,
                        discountPrice = dto.discountPrice,
                        isVegetarian = dto.isVegetarian,
                        isHalal = dto.isHalal,
                        spiceLevel = dto.spiceLevel,
                        prepTime = dto.prepTime,
                        rating = dto.rating,
                        totalOrders = dto.totalOrders,
                        isAvailable = dto.isAvailable,
                        isPopular = dto.isPopular
                    )
                }
                Result.success(items)
            } else {
                Result.failure(Exception("Failed to load menu: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun createMenuItem(
        name: String,
        description: String?,
        category: String,
        price: Double,
        isAvailable: Boolean,
        isHalal: Boolean,
        spiceLevel: String,
        prepTime: Int
    ): Result<MenuItem> {
        return try {
            val response = menuApi.createMenuItem(
                getToken(),
                CreateMenuItemRequest(
                    name = name,
                    description = description,
                    category = category,
                    price = price,
                    isAvailable = isAvailable,
                    isHalal = isHalal,
                    spiceLevel = spiceLevel,
                    prepTime = prepTime
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data
                Result.success(MenuItem(
                    id = dto?.id ?: "",
                    name = dto?.name ?: name,
                    description = dto?.description ?: description,
                    category = dto?.category ?: category,
                    price = dto?.price ?: price,
                    isAvailable = dto?.isAvailable ?: isAvailable,
                    isHalal = dto?.isHalal ?: isHalal,
                    spiceLevel = dto?.spiceLevel ?: spiceLevel,
                    prepTime = dto?.prepTime ?: prepTime
                ))
            } else {
                Result.failure(Exception("Failed to create item: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun updateMenuItem(
        id: String,
        name: String?,
        description: String?,
        category: String?,
        price: Double?,
        isAvailable: Boolean?
    ): Result<MenuItem> {
        return try {
            val response = menuApi.updateMenuItem(
                getToken(), id,
                UpdateMenuItemRequest(
                    name = name,
                    description = description,
                    category = category,
                    price = price,
                    isAvailable = isAvailable
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data
                Result.success(MenuItem(
                    id = dto?.id ?: id,
                    name = dto?.name ?: "",
                    description = dto?.description,
                    category = dto?.category,
                    price = dto?.price ?: 0.0,
                    isAvailable = dto?.isAvailable ?: true
                ))
            } else {
                Result.failure(Exception("Failed to update item: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun toggleAvailability(id: String): Result<MenuItem> {
        return try {
            val response = menuApi.toggleAvailability(getToken(), id)
            if (response.isSuccessful && response.body()?.success == true) {
                val dto = response.body()!!.data
                Result.success(MenuItem(
                    id = dto?.id ?: id,
                    name = dto?.name ?: "",
                    price = dto?.price ?: 0.0,
                    isAvailable = dto?.isAvailable ?: true
                ))
            } else {
                Result.failure(Exception("Failed to toggle availability"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun deleteMenuItem(id: String): Result<Unit> {
        return try {
            val response = menuApi.deleteMenuItem(getToken(), id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete item: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}
