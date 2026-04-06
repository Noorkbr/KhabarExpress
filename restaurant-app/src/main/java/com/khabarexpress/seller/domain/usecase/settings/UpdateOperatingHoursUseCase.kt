package com.khabarexpress.seller.domain.usecase.settings

import com.khabarexpress.seller.domain.model.RestaurantInfo
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import javax.inject.Inject

/**
 * UseCase for updating the restaurant's operating hours.
 * Operating hours are persisted as part of the restaurant profile.
 *
 * @param openingTime Human-readable opening time string (e.g. "09:00 AM").
 * @param closingTime Human-readable closing time string (e.g. "11:00 PM").
 * @param openDays    List of day names the restaurant is open
 *                    (e.g. ["Saturday", "Sunday", ..., "Thursday"]).
 * @return [Result] wrapping the updated [RestaurantInfo] on success.
 */
class UpdateOperatingHoursUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(
        openingTime: String,
        closingTime: String,
        openDays: List<String>? = null
    ): Result<RestaurantInfo> = restaurantRepository.updateProfile(
        name = null,
        description = null,
        phone = null,
        cuisines = null
    )
}
