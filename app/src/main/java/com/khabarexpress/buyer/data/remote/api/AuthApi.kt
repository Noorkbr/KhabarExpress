package com.khabarexpress.buyer.data.remote.api

import com.khabarexpress.buyer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("api/v1/auth/phone-login")
    suspend fun phoneLogin(@Body request: PhoneRequest): Response<AuthResponse>

    @POST("api/v1/auth/send-otp")
    suspend fun sendOtp(@Body request: PhoneRequest): Response<OtpResponse>
    
    @POST("api/v1/auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>
    
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("api/v1/auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
    
    @POST("api/v1/auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
    
    @GET("api/v1/auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserDto>
    
    @PUT("api/v1/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserDto>
}
