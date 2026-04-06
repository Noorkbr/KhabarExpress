package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import javax.inject.Inject

class GetNearbyRestaurantsUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 10.0
    ): Result<List<Restaurant>> =
        restaurantRepository.getNearbyRestaurants(latitude, longitude, radiusKm)
}
