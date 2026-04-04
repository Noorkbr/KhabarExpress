package com.khabarexpress.seller.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
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
data class UserDto(
    val id: String,
    val name: String,
    val phone: String,
    val email: String? = null,
    val role: String = "restaurant",
    val restaurantId: String? = null
)
