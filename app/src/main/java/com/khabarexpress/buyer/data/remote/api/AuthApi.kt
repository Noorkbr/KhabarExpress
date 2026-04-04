package com.khabarexpress.buyer.data.remote.api

import com.khabarexpress.buyer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    @POST("auth/phone-login")
    suspend fun phoneLogin(@Body request: PhoneLoginRequest): Response<AuthResponse>

    @POST("auth/send-otp")
    suspend fun sendOtp(@Body request: PhoneRequest): Response<OtpResponse>
    
    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>
    
    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<Unit>
    
    @GET("auth/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<UserDto>
    
    @PUT("auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<UserDto>
}
