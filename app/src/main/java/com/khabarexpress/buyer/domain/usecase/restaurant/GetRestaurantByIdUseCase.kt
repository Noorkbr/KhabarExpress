package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import javax.inject.Inject

/**
 * UseCase for retrieving a single restaurant by its identifier.
 *
 * @param id Unique identifier of the restaurant.
 * @return [Result] wrapping the [Restaurant] on success or an error.
 */
class GetRestaurantByIdUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(id: String): Result<Restaurant> =
        restaurantRepository.getRestaurantById(id)
}
