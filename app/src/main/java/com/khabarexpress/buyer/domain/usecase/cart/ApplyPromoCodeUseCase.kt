package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.model.PromoCode
import com.khabarexpress.buyer.domain.repository.CartRepository
import javax.inject.Inject

/**
 * UseCase for applying a promotional discount code to the current cart.
 *
 * @param code The promo code string entered by the user.
 * @return [Result] wrapping the validated [PromoCode] on success or an error
 *         (e.g. expired or invalid code).
 */
class ApplyPromoCodeUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(code: String): Result<PromoCode> =
        cartRepository.applyPromoCode(code)
}
