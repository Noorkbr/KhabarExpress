package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for searching restaurants by a text query.
 * Performs a live search and emits updated results as a [Flow].
 *
 * @param query The user-entered search text.
 * @return [Flow] emitting a filtered [List] of [Restaurant].
 */
class SearchRestaurantsUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    operator fun invoke(query: String): Flow<List<Restaurant>> =
        restaurantRepository.searchRestaurants(query)
}
