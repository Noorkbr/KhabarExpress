package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import javax.inject.Inject

/**
 * UseCase for toggling the favorite status of a restaurant for the current user.
 *
 * @param restaurantId Unique identifier of the restaurant.
 * @return [Result] wrapping `true` if the restaurant is now a favorite,
 *         `false` if it was removed from favorites.
 */
class ToggleFavoriteUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(restaurantId: String): Result<Boolean> =
        restaurantRepository.toggleFavorite(restaurantId)
}
