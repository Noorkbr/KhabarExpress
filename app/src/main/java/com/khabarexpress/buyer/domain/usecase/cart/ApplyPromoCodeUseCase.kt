package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.model.PromoCode
import com.khabarexpress.buyer.domain.repository.CartRepository
import javax.inject.Inject

class ApplyPromoCodeUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(code: String): Result<PromoCode> =
        cartRepository.applyPromoCode(code)
}
