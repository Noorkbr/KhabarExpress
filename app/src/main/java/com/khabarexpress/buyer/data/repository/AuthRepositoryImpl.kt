package com.khabarexpress.buyer.data.repository

import com.khabarexpress.buyer.data.local.dao.UserDao
import com.khabarexpress.buyer.data.local.preferences.AppPreferences
import com.khabarexpress.buyer.data.mappers.toDomainModel
import com.khabarexpress.buyer.data.mappers.toEntity
import com.khabarexpress.buyer.data.remote.api.AuthApi
import com.khabarexpress.buyer.data.remote.dto.*
import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of AuthRepository
 * Handles user authentication operations including login, register, OTP verification
 * Manages auth tokens using DataStore and caches user data in Room database
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val appPreferences: AppPreferences
) : AuthRepository {

    /**
     * Seamless phone-only login for end users (no verification required).
     * The server creates or retrieves the user account based on phone number alone.
     */
    override suspend fun loginWithPhoneOnly(phone: String): Result<User> {
        return try {
            val response = authApi.phoneLogin(PhoneLoginRequest(phone))

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!

                if (authResponse.success && authResponse.user != null && authResponse.token != null) {
                    // Save auth token
                    appPreferences.saveAuthToken(authResponse.token)
                    authResponse.refreshToken?.let { appPreferences.saveRefreshToken(it) }
                    appPreferences.saveUserId(authResponse.user.id)

                    // Cache user in Room
                    userDao.insertUser(authResponse.user.toEntity())

                    Result.success(authResponse.user.toDomainModel())
                } else {
                    Result.failure(Exception(authResponse.message))
                }
            } else {
                Result.failure(Exception("Phone login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Login with email and password
     */
    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                if (authResponse.success && authResponse.user != null && authResponse.token != null) {
                    // Save auth token
                    appPreferences.saveAuthToken(authResponse.token)
                    authResponse.refreshToken?.let { appPreferences.saveRefreshToken(it) }
                    appPreferences.saveUserId(authResponse.user.id)
                    
                    // Cache user in Room
                    userDao.insertUser(authResponse.user.toEntity())
                    
                    Result.success(authResponse.user.toDomainModel())
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

    /**
     * Login with phone number and OTP
     */
    override suspend fun loginWithPhone(phone: String, otp: String): Result<User> {
        return try {
            val response = authApi.verifyOtp(VerifyOtpRequest(phone, otp))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                if (authResponse.success && authResponse.user != null && authResponse.token != null) {
                    // Save auth token
                    appPreferences.saveAuthToken(authResponse.token)
                    authResponse.refreshToken?.let { appPreferences.saveRefreshToken(it) }
                    appPreferences.saveUserId(authResponse.user.id)
                    
                    // Cache user in Room
                    userDao.insertUser(authResponse.user.toEntity())
                    
                    Result.success(authResponse.user.toDomainModel())
                } else {
                    Result.failure(Exception(authResponse.message))
                }
            } else {
                Result.failure(Exception("OTP verification failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Register new user
     */
    override suspend fun register(
        name: String,
        email: String,
        phone: String,
        password: String
    ): Result<User> {
        return try {
            val response = authApi.register(RegisterRequest(name, email, phone, password))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                if (authResponse.success && authResponse.user != null && authResponse.token != null) {
                    // Save auth token
                    appPreferences.saveAuthToken(authResponse.token)
                    authResponse.refreshToken?.let { appPreferences.saveRefreshToken(it) }
                    appPreferences.saveUserId(authResponse.user.id)
                    
                    // Cache user in Room
                    userDao.insertUser(authResponse.user.toEntity())
                    
                    Result.success(authResponse.user.toDomainModel())
                } else {
                    Result.failure(Exception(authResponse.message))
                }
            } else {
                Result.failure(Exception("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Logout current user
     */
    override suspend fun logout(): Result<Unit> {
        return try {
            val token = appPreferences.getAuthTokenSync()
            
            if (token != null) {
                // Call logout API
                authApi.logout("Bearer $token")
            }
            
            // Clear local data
            appPreferences.clearAuthData()
            userDao.deleteAllUsers()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Even if API call fails, clear local data
            appPreferences.clearAuthData()
            userDao.deleteAllUsers()
            Result.success(Unit)
        }
    }

    /**
     * Get current authenticated user as Flow
     */
    override fun getCurrentUser(): Flow<User?> {
        return appPreferences.getAuthToken().map { token ->
            if (token != null) {
                val userId = appPreferences.getUserId()
                if (userId != null) {
                    var user: User? = null
                    userDao.getUserById(userId).collect { userEntity ->
                        user = userEntity?.toDomainModel()
                    }
                    user
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Check if user is authenticated
     */
    override suspend fun isAuthenticated(): Boolean {
        val token = appPreferences.getAuthTokenSync()
        return token != null
    }

    /**
     * Send OTP to phone number
     */
    override suspend fun sendOtp(phone: String): Result<Unit> {
        return try {
            val response = authApi.sendOtp(PhoneRequest(phone))
            
            if (response.isSuccessful && response.body() != null) {
                val otpResponse = response.body()!!
                
                if (otpResponse.success && otpResponse.otpSent) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(otpResponse.message))
                }
            } else {
                Result.failure(Exception("Failed to send OTP: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Verify OTP
     */
    override suspend fun verifyOtp(phone: String, otp: String): Result<Boolean> {
        return try {
            val response = authApi.verifyOtp(VerifyOtpRequest(phone, otp))
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                Result.success(authResponse.success)
            } else {
                Result.failure(Exception("OTP verification failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}
