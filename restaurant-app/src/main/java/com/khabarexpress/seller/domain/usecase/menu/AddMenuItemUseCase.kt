package com.khabarexpress.seller.domain.usecase.menu

import com.khabarexpress.seller.domain.model.MenuItem
import com.khabarexpress.seller.domain.repository.MenuRepository
import javax.inject.Inject

/**
 * UseCase for adding a new item to the restaurant's menu.
 *
 * @param name        Display name of the menu item.
 * @param description Optional description.
 * @param category    Category identifier string.
 * @param price       Price in BDT.
 * @param isAvailable Whether the item is currently available for ordering.
 * @param isHalal     Whether the item is halal-certified.
 * @param spiceLevel  Spice level label (e.g. "None", "Mild", "Medium", "Hot").
 * @param prepTime    Estimated preparation time in minutes.
 * @return [Result] wrapping the created [MenuItem] on success.
 */
class AddMenuItemUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String?,
        category: String,
        price: Double,
        isAvailable: Boolean = true,
        isHalal: Boolean = true,
        spiceLevel: String = "None",
        prepTime: Int = 15
    ): Result<MenuItem> = menuRepository.createMenuItem(
        name, description, category, price, isAvailable, isHalal, spiceLevel, prepTime
    )
}
