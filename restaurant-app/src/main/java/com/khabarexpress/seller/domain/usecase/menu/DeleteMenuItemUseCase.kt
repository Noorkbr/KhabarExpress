package com.khabarexpress.seller.domain.usecase.menu

import com.khabarexpress.seller.domain.repository.MenuRepository
import javax.inject.Inject

/**
 * UseCase for permanently removing a menu item.
 *
 * @param id Unique identifier of the menu item to delete.
 * @return [Result] wrapping [Unit] on success.
 */
class DeleteMenuItemUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = menuRepository.deleteMenuItem(id)
}
