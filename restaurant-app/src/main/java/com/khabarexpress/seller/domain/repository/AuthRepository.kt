package com.khabarexpress.seller.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(phone: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun isAuthenticated(): Boolean
    fun getAuthToken(): Flow<String?>
    suspend fun getRestaurantId(): String?
}
