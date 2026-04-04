package com.khabarexpress.seller.data.repository

import com.khabarexpress.seller.data.local.preferences.AppPreferences
import com.khabarexpress.seller.data.remote.api.AuthApi
import com.khabarexpress.seller.data.remote.dto.LoginRequest
import com.khabarexpress.seller.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val appPreferences: AppPreferences
) : AuthRepository {

    override suspend fun login(phone: String, password: String): Result<Unit> {
        return try {
            val response = authApi.login(LoginRequest(phone, password))
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                if (authResponse.success && authResponse.token != null) {
                    appPreferences.saveAuthToken(authResponse.token)
                    authResponse.refreshToken?.let { appPreferences.saveRefreshToken(it) }
                    authResponse.user?.let { user ->
                        appPreferences.saveUserId(user.id)
                        user.restaurantId?.let { appPreferences.saveRestaurantId(it) }
                        appPreferences.saveRestaurantName(user.name)
                    }
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(authResponse.message))
                }
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val token = appPreferences.getAuthTokenSync()
            if (token != null) {
                authApi.logout("Bearer $token")
            }
            appPreferences.clearAuthData()
            Result.success(Unit)
        } catch (e: Exception) {
            appPreferences.clearAuthData()
            Result.success(Unit)
        }
    }

    override suspend fun isAuthenticated(): Boolean {
        return appPreferences.isAuthenticated()
    }

    override fun getAuthToken(): Flow<String?> {
        return appPreferences.getAuthToken()
    }

    override suspend fun getRestaurantId(): String? {
        return appPreferences.getRestaurantId()
    }
}
