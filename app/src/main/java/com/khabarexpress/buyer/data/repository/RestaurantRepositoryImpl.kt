package com.khabarexpress.buyer.data.repository

import com.khabarexpress.buyer.data.local.dao.FavoriteDao
import com.khabarexpress.buyer.data.mappers.toDomainModel
import com.khabarexpress.buyer.data.mappers.toFavoriteEntity
import com.khabarexpress.buyer.data.remote.api.RestaurantApi
import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RestaurantRepository
 * Handles restaurant data operations including fetching, searching, and favorites
 * Uses API for restaurant data and Room for favorites caching
 */
@Singleton
class RestaurantRepositoryImpl @Inject constructor(
    private val restaurantApi: RestaurantApi,
    private val favoriteDao: FavoriteDao
) : RestaurantRepository {

    // Default location (Dhaka, Bangladesh) if user location is not available
    private val defaultLatitude = 23.8103
    private val defaultLongitude = 90.4125

    /**
     * Get all restaurants as Flow
     */
    override fun getRestaurants(): Flow<List<Restaurant>> = flow {
        try {
            val response = restaurantApi.getRestaurants(
                lat = defaultLatitude,
                lng = defaultLongitude
            )
            
            if (response.isSuccessful && response.body() != null) {
                val restaurants = response.body()!!.restaurants.map { it.toDomainModel() }
                emit(restaurants)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Get restaurant by ID with full details
     */
    override suspend fun getRestaurantById(id: String): Result<Restaurant> {
        return try {
            val response = restaurantApi.getRestaurantById(id)
            
            if (response.isSuccessful && response.body() != null) {
                val restaurant = response.body()!!.toDomainModel()
                
                // Fetch menu if needed
                try {
                    val menuResponse = restaurantApi.getRestaurantMenu(id)
                    if (menuResponse.isSuccessful && menuResponse.body() != null) {
                        val categories = menuResponse.body()!!.categories.map { it.toDomainModel() }
                        Result.success(restaurant.copy(categories = categories))
                    } else {
                        Result.success(restaurant)
                    }
                } catch (e: Exception) {
                    Result.success(restaurant)
                }
            } else {
                Result.failure(Exception("Restaurant not found: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Search restaurants by query
     */
    override fun searchRestaurants(query: String): Flow<List<Restaurant>> = flow {
        try {
            val response = restaurantApi.searchRestaurants(
                query = query,
                lat = defaultLatitude,
                lng = defaultLongitude
            )
            
            if (response.isSuccessful && response.body() != null) {
                val restaurants = response.body()!!.restaurants.map { it.toDomainModel() }
                emit(restaurants)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Filter restaurants by category
     */
    override fun filterByCategory(category: String): Flow<List<Restaurant>> = flow {
        try {
            val response = restaurantApi.getRestaurants(
                lat = defaultLatitude,
                lng = defaultLongitude,
                category = category
            )
            
            if (response.isSuccessful && response.body() != null) {
                val restaurants = response.body()!!.restaurants.map { it.toDomainModel() }
                emit(restaurants)
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * Get nearby restaurants based on location
     */
    override suspend fun getNearbyRestaurants(
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<Restaurant>> {
        return try {
            val response = restaurantApi.getNearbyRestaurants(
                lat = latitude,
                lng = longitude,
                radius = radiusKm
            )
            
            if (response.isSuccessful && response.body() != null) {
                val restaurants = response.body()!!.restaurants.map { it.toDomainModel() }
                Result.success(restaurants)
            } else {
                Result.failure(Exception("Failed to fetch nearby restaurants: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }

    /**
     * Toggle favorite status for restaurant
     */
    override suspend fun toggleFavorite(restaurantId: String): Result<Boolean> {
        return try {
            // Check if already favorite
            var isFav = false
            favoriteDao.isFavorite(restaurantId).collect { isFav = it }
            
            if (isFav) {
                // Remove from favorites
                favoriteDao.deleteFavoriteById(restaurantId)
                Result.success(false)
            } else {
                // Add to favorites - fetch restaurant details first
                val restaurantResult = getRestaurantById(restaurantId)
                if (restaurantResult.isSuccess) {
                    val restaurant = restaurantResult.getOrNull()!!
                    
                    // Convert to DTO-like object for mapping
                    val restaurantDto = com.khabarexpress.buyer.data.remote.dto.RestaurantDto(
                        id = restaurant.id,
                        name = restaurant.name,
                        description = restaurant.description,
                        imageUrl = restaurant.imageUrl,
                        coverImageUrl = restaurant.coverImageUrl,
                        cuisine = restaurant.cuisine,
                        rating = restaurant.rating,
                        totalReviews = restaurant.totalReviews,
                        deliveryTime = restaurant.deliveryTime,
                        deliveryFee = restaurant.deliveryFee,
                        minOrderAmount = restaurant.minOrderAmount,
                        isOpen = restaurant.isOpen,
                        distance = restaurant.distance,
                        latitude = restaurant.latitude,
                        longitude = restaurant.longitude,
                        address = restaurant.address,
                        tags = restaurant.tags
                    )
                    
                    favoriteDao.insertFavorite(restaurantDto.toFavoriteEntity())
                    Result.success(true)
                } else {
                    Result.failure(Exception("Failed to fetch restaurant details"))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to toggle favorite: ${e.message}", e))
        }
    }

    /**
     * Get favorite restaurants as Flow
     */
    override fun getFavoriteRestaurants(): Flow<List<Restaurant>> {
        return favoriteDao.getAllFavorites().map { favorites ->
            favorites.map { it.toDomainModel() }
        }
    }

    /**
     * Check if restaurant is favorite
     */
    override suspend fun isFavorite(restaurantId: String): Boolean {
        var isFav = false
        favoriteDao.isFavorite(restaurantId).collect { isFav = it }
        return isFav
    }
}
