package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.repository.CartRepository
import javax.inject.Inject

/**
 * UseCase for removing the currently applied promotional code from the cart,
 * restoring the original subtotal.
 *
 * @return [Result] wrapping [Unit] on success or an error.
 */
class RemovePromoCodeUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        cartRepository.removePromoCode()
}
