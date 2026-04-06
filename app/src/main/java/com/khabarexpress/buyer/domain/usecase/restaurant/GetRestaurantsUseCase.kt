package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for retrieving the full list of available restaurants.
 * Exposes a [Flow] that emits the list whenever the underlying data changes.
 *
 * @return [Flow] emitting [List] of [Restaurant].
 */
class GetRestaurantsUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    operator fun invoke(): Flow<List<Restaurant>> =
        restaurantRepository.getRestaurants()
}
