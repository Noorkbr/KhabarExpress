package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(restaurantId: String): Result<Boolean> =
        restaurantRepository.toggleFavorite(restaurantId)
}
