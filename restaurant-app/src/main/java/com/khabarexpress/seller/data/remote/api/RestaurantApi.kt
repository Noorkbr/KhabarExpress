package com.khabarexpress.seller.data.remote.api

import com.khabarexpress.seller.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface RestaurantApi {
    @POST("restaurants/register")
    suspend fun registerRestaurant(@Body request: RegisterRestaurantRequest): Response<RestaurantResponse>

    @PUT("restaurants/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<RestaurantResponse>

    @PATCH("restaurants/status")
    suspend fun toggleOpenStatus(@Header("Authorization") token: String): Response<ToggleStatusResponse>

    @GET("restaurants/my/analytics")
    suspend fun getAnalytics(
        @Header("Authorization") token: String,
        @Query("period") period: String = "today"
    ): Response<AnalyticsResponse>
}
