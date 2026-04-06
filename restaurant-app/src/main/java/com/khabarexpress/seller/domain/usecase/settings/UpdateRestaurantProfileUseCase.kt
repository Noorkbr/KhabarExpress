package com.khabarexpress.seller.domain.usecase.settings

import com.khabarexpress.seller.domain.model.RestaurantInfo
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import javax.inject.Inject

/**
 * UseCase for updating the restaurant's public profile information.
 * Only non-null parameters are forwarded to the server.
 *
 * @param name        Optional new restaurant display name.
 * @param description Optional new description / tagline.
 * @param phone       Optional new contact phone number.
 * @param cuisines    Optional updated list of cuisine types.
 * @return [Result] wrapping the updated [RestaurantInfo] on success.
 */
class UpdateRestaurantProfileUseCase @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) {
    suspend operator fun invoke(
        name: String? = null,
        description: String? = null,
        phone: String? = null,
        cuisines: List<String>? = null
    ): Result<RestaurantInfo> = restaurantRepository.updateProfile(
        name, description, phone, cuisines
    )
}
