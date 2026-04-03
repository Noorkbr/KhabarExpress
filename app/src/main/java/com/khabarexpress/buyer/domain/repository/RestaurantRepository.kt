package com.khabarexpress.buyer.domain.repository

import com.khabarexpress.buyer.domain.model.Restaurant
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for restaurant operations
 */
interface RestaurantRepository {
    
    /**
     * Get all restaurants
     */
    fun getRestaurants(): Flow<List<Restaurant>>
    
    /**
     * Get restaurant by ID with full details
     */
    suspend fun getRestaurantById(id: String): Result<Restaurant>
    
    /**
     * Search restaurants by query
     */
    fun searchRestaurants(query: String): Flow<List<Restaurant>>
    
    /**
     * Filter restaurants by category
     */
    fun filterByCategory(category: String): Flow<List<Restaurant>>
    
    /**
     * Get nearby restaurants based on location
     */
    suspend fun getNearbyRestaurants(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Result<List<Restaurant>>
    
    /**
     * Toggle favorite status for restaurant
     */
    suspend fun toggleFavorite(restaurantId: String): Result<Boolean>
    
    /**
     * Get favorite restaurants
     */
    fun getFavoriteRestaurants(): Flow<List<Restaurant>>
    
    /**
     * Check if restaurant is favorite
     */
    suspend fun isFavorite(restaurantId: String): Boolean
}
