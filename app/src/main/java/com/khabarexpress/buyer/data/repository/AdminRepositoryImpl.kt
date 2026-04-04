package com.khabarexpress.buyer.data.repository

import com.khabarexpress.buyer.data.local.preferences.AppPreferences
import com.khabarexpress.buyer.data.remote.api.AdminApi
import com.khabarexpress.buyer.data.remote.dto.*
import com.khabarexpress.buyer.domain.repository.AdminRepository
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val adminApi: AdminApi,
    private val appPreferences: AppPreferences
) : AdminRepository {
    
    private suspend fun getAuthHeader(): String {
        val token = appPreferences.getAuthTokenSync() ?: ""
        return "Bearer $token"
    }
    
    override suspend fun getDashboardStats(): Result<AdminDashboardData> {
        return try {
            val response = adminApi.getDashboardStats(getAuthHeader())
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to fetch dashboard stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProfitAnalytics(
        startDate: String?,
        endDate: String?,
        groupBy: String
    ): Result<ProfitAnalyticsData> {
        return try {
            val response = adminApi.getProfitAnalytics(
                getAuthHeader(), startDate, endDate, groupBy
            )
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to fetch profit analytics"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getVerificationStats(): Result<VerificationStatsData> {
        return try {
            val response = adminApi.getVerificationStats(getAuthHeader())
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to fetch verification stats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPendingRestaurants(): Result<List<PendingRestaurantDto>> {
        return try {
            val response = adminApi.getPendingRestaurants(getAuthHeader())
            if (response.success) {
                Result.success(response.data)
            } else {
                Result.failure(Exception("Failed to fetch pending restaurants"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun approveRestaurant(restaurantId: String): Result<String> {
        return try {
            val response = adminApi.approveRestaurant(getAuthHeader(), restaurantId)
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun rejectRestaurant(restaurantId: String, reason: String): Result<String> {
        return try {
            val response = adminApi.rejectRestaurant(
                getAuthHeader(), restaurantId, RejectRequest(reason)
            )
            if (response.success) {
                Result.success(response.message)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
