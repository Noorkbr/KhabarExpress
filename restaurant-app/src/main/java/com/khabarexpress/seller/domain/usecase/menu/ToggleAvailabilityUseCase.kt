package com.khabarexpress.seller.domain.usecase.menu

import com.khabarexpress.seller.domain.model.MenuItem
import com.khabarexpress.seller.domain.repository.MenuRepository
import javax.inject.Inject

/**
 * UseCase for toggling the availability of a menu item.
 * If the item is currently available it will be marked unavailable, and vice-versa.
 *
 * @param id Unique identifier of the menu item.
 * @return [Result] wrapping the updated [MenuItem] on success.
 */
class ToggleAvailabilityUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(id: String): Result<MenuItem> =
        menuRepository.toggleAvailability(id)
}
