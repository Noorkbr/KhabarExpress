package com.khabarexpress.seller.data.repository

import com.khabarexpress.seller.data.local.preferences.AppPreferences
import com.khabarexpress.seller.data.remote.api.RestaurantApi
import com.khabarexpress.seller.data.remote.dto.UpdateProfileRequest
import com.khabarexpress.seller.domain.model.Analytics
import com.khabarexpress.seller.domain.model.RestaurantInfo
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    private val restaurantApi: RestaurantApi,
    private val appPreferences: AppPreferences
) : RestaurantRepository {

    private suspend fun getToken(): String = "Bearer ${appPreferences.getAuthTokenSync() ?: ""}"

    override suspend fun getProfile(): Result<RestaurantInfo> {
        return try {
            val response = restaurantApi.getAnalytics(getToken(), "today")
            // Profile data comes from analytics endpoint or we use stored data
            Result.success(RestaurantInfo(
                id = appPreferences.getRestaurantId() ?: "",
                name = appPreferences.getRestaurantName() ?: "",
                phone = ""
            ))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to load profile: ${e.message}", e))
        }
    }

    override suspend fun updateProfile(
        name: String?,
        description: String?,
        phone: String?,
        cuisines: List<String>?
    ): Result<RestaurantInfo> {
        return try {
            val response = restaurantApi.updateProfile(
                getToken(),
                UpdateProfileRequest(
                    name = name,
                    description = description,
                    phone = phone,
                    cuisines = cuisines
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data
                if (data != null) {
                    Result.success(RestaurantInfo(
                        id = data.id ?: "",
                        name = data.name,
                        description = data.description,
                        phone = data.phone,
                        email = data.email,
                        cuisines = data.cuisines,
                        category = data.category,
                        coverImage = data.coverImage,
                        logo = data.logo,
                        rating = data.rating,
                        totalReviews = data.totalReviews,
                        totalOrders = data.totalOrders,
                        isOpen = data.isOpen,
                        isActive = data.isActive,
                        approvalStatus = data.approvalStatus
                    ))
                } else {
                    Result.failure(Exception("No data returned"))
                }
            } else {
                Result.failure(Exception("Update failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun updateOperatingHours(
        openingTime: String,
        closingTime: String,
        openDays: List<String>?
    ): Result<RestaurantInfo> {
        return try {
            val response = restaurantApi.updateProfile(
                getToken(),
                UpdateProfileRequest(
                    openingTime = openingTime,
                    closingTime = closingTime,
                    openDays = openDays
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data
                if (data != null) {
                    Result.success(RestaurantInfo(
                        id = data.id ?: "",
                        name = data.name,
                        description = data.description,
                        phone = data.phone,
                        email = data.email,
                        cuisines = data.cuisines,
                        category = data.category,
                        coverImage = data.coverImage,
                        logo = data.logo,
                        rating = data.rating,
                        totalReviews = data.totalReviews,
                        totalOrders = data.totalOrders,
                        isOpen = data.isOpen,
                        isActive = data.isActive,
                        approvalStatus = data.approvalStatus
                    ))
                } else {
                    Result.failure(Exception("No data returned"))
                }
            } else {
                Result.failure(Exception("Update failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun toggleOpenStatus(): Result<Boolean> {
        return try {
            val response = restaurantApi.toggleOpenStatus(getToken())
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data?.isOpen ?: false)
            } else {
                Result.failure(Exception("Toggle failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun getAnalytics(period: String): Result<Analytics> {
        return try {
            val response = restaurantApi.getAnalytics(getToken(), period)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data
                Result.success(Analytics(
                    totalOrders = data?.totalOrders ?: 0,
                    totalRevenue = data?.totalRevenue ?: 0.0,
                    averageOrderValue = data?.averageOrderValue ?: 0.0,
                    period = data?.period ?: period
                ))
            } else {
                Result.failure(Exception("Failed to load analytics: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}
