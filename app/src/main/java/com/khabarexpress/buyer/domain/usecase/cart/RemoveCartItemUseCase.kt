package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.repository.CartRepository
import javax.inject.Inject

/**
 * UseCase for removing a single item from the shopping cart.
 *
 * @param itemId Unique identifier of the cart item to remove.
 * @return [Result] wrapping [Unit] on success or an error.
 */
class RemoveCartItemUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(itemId: String): Result<Unit> =
        cartRepository.removeItem(itemId)
}
