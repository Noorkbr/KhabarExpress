package com.khabarexpress.buyer.data.remote.dto

import kotlinx.serialization.Serializable

// Auth DTOs
@Serializable
data class PhoneRequest(
    val phone: String
)

@Serializable
data class OtpResponse(
    val success: Boolean,
    val message: String,
    val otpSent: Boolean
)

@Serializable
data class VerifyOtpRequest(
    val phone: String,
    val otp: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val refreshToken: String? = null,
    val user: UserDto? = null
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val profileImageUrl: String? = null
)

@Serializable
data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val profileImageUrl: String? = null,
    val createdAt: Long
)
