package com.khabarexpress.seller.domain.usecase.menu

import com.khabarexpress.seller.domain.model.MenuItem
import com.khabarexpress.seller.domain.repository.MenuRepository
import javax.inject.Inject

/**
 * UseCase for retrieving all menu items belonging to the authenticated restaurant.
 *
 * @return [Result] wrapping the list of [MenuItem] objects on success.
 */
class GetMenuItemsUseCase @Inject constructor(
    private val menuRepository: MenuRepository
) {
    suspend operator fun invoke(): Result<List<MenuItem>> = menuRepository.getMenuItems()
}
