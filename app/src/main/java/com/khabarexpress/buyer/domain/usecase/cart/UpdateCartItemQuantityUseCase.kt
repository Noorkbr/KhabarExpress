package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.repository.CartRepository
import javax.inject.Inject

/**
 * UseCase for updating the quantity of a specific item in the cart.
 *
 * @param itemId   Unique identifier of the cart item.
 * @param quantity New quantity (must be ≥ 1; use [RemoveCartItemUseCase] to remove).
 * @return [Result] wrapping [Unit] on success or an error.
 */
class UpdateCartItemQuantityUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(itemId: String, quantity: Int): Result<Unit> =
        cartRepository.updateQuantity(itemId, quantity)
}
