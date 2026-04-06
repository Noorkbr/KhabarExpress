package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.repository.CartRepository
import javax.inject.Inject

/**
 * UseCase for removing all items from the shopping cart.
 * Typically called after a successful order placement.
 *
 * @return [Result] wrapping [Unit] on success or an error.
 */
class ClearCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        cartRepository.clearCart()
}
