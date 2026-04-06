package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for filtering restaurants by cuisine category.
 * Emits a [Flow] of restaurants that match the given category.
 *
 * @param category The cuisine category name to filter by (e.g. "Bengali", "Chinese").
 * @return [Flow] emitting a [List] of [Restaurant] matching the category.
 */
class FilterByCategoryUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    operator fun invoke(category: String): Flow<List<Restaurant>> =
        restaurantRepository.filterByCategory(category)
}
