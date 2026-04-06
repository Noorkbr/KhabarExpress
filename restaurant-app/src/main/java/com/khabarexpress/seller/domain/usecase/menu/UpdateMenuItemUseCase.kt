package com.khabarexpress.seller.domain.usecase.menu

import com.khabarexpress.seller.domain.model.MenuItem
import com.khabarexpress.seller.domain.repository.MenuRepository
import javax.inject.Inject

/**
 * UseCase for updating an existing menu item.
 * Only non-null parameters are sent to the server — pass `null` to leave
 * a field unchanged.
 *
 * @param id          Unique identifier of the menu item.
 * @param name        Optional new name.
 * @param description Optional new description.
 * @param category    Optional new category.
 * @param price       Optional new price in BDT.
 * @param isAvailable Optional availability flag.
 * @return [Result] wrapping the updated [MenuItem] on success.
 */
class UpdateMenuItemUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(
        id: String,
        name: String? = null,
        description: String? = null,
        category: String? = null,
        price: Double? = null,
        isAvailable: Boolean? = null
    ): Result<MenuItem> = menuRepository.updateMenuItem(
        id, name, description, category, price, isAvailable
    )
}
