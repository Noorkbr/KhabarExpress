package com.khabarexpress.buyer.data.remote.api

import com.khabarexpress.buyer.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface RestaurantApi {
    @GET("api/v1/restaurants")
    suspend fun getRestaurants(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<RestaurantListResponse>
    
    @GET("api/v1/restaurants/{id}")
    suspend fun getRestaurantById(@Path("id") restaurantId: String): Response<RestaurantDto>
    
    @GET("api/v1/restaurants/{id}/menu")
    suspend fun getRestaurantMenu(@Path("id") restaurantId: String): Response<MenuResponse>
    
    @GET("api/v1/restaurants/{id}/reviews")
    suspend fun getRestaurantReviews(
        @Path("id") restaurantId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<ReviewListResponse>
    
    @POST("api/v1/restaurants/{id}/reviews")
    suspend fun submitReview(
        @Header("Authorization") token: String,
        @Path("id") restaurantId: String,
        @Body request: SubmitReviewRequest
    ): Response<ReviewDto>
    
    @GET("api/v1/restaurants/featured")
    suspend fun getFeaturedRestaurants(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): Response<RestaurantListResponse>
    
    @GET("api/v1/restaurants/nearby")
    suspend fun getNearbyRestaurants(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double = 5.0
    ): Response<RestaurantListResponse>
    
    @GET("api/v1/search")
    suspend fun searchRestaurants(
        @Query("q") query: String,
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("type") type: String? = null // "restaurant", "dish", "cuisine"
    ): Response<SearchResultResponse>
}
