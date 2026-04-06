package com.khabarexpress.buyer.domain.usecase.restaurant

import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import javax.inject.Inject

class GetRestaurantByIdUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(id: String): Result<Restaurant> =
        restaurantRepository.getRestaurantById(id)
}
