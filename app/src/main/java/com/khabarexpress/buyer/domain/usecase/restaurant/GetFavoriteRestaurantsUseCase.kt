package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for retrieving the current user's favorite restaurants.
 * Exposes a [Flow] that emits the updated list whenever favorites change.
 *
 * @return [Flow] emitting a [List] of favorited [Restaurant] objects.
 */
class GetFavoriteRestaurantsUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    operator fun invoke(): Flow<List<Restaurant>> =
        restaurantRepository.getFavoriteRestaurants()

    suspend fun isFavorite(restaurantId: String): Boolean =
        restaurantRepository.isFavorite(restaurantId)
}
