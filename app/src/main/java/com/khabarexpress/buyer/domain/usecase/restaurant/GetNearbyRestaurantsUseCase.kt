package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import javax.inject.Inject

/**
 * UseCase for retrieving restaurants closest to the user's current location.
 *
 * @param latitude  Latitude of the user's current position.
 * @param longitude Longitude of the user's current position.
 * @param radiusKm  Search radius in kilometres (default 10 km).
 * @return [Result] wrapping a [List] of nearby [Restaurant] objects on success.
 */
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
