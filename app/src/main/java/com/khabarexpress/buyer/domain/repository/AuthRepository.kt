package com.khabarexpress.buyer.domain.repository

import com.khabarexpress.buyer.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {
    
    /**
     * Seamless phone-only login for end users (no verification required)
     */
    suspend fun loginWithPhoneOnly(phone: String): Result<User>

    /**
     * Login with email and password
     */
    suspend fun login(email: String, password: String): Result<User>
    
    /**
     * Login with phone number and OTP
     */
    suspend fun loginWithPhone(phone: String, otp: String): Result<User>
    
    /**
     * Register new user
     */
    suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<User>
    
    /**
     * Logout current user
     */
    suspend fun logout(): Result<Unit>
    
    /**
     * Get current authenticated user
     */
    fun getCurrentUser(): Flow<User?>
    
    /**
     * Check if user is authenticated
     */
    suspend fun isAuthenticated(): Boolean
    
    /**
     * Send OTP to phone number
     */
    suspend fun sendOtp(phone: String): Result<Unit>
    
    /**
     * Verify OTP
     */
    suspend fun verifyOtp(phone: String, otp: String): Result<Boolean>
}
