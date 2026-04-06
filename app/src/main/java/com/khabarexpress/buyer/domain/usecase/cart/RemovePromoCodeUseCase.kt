package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.repository.CartRepository
import javax.inject.Inject

class RemovePromoCodeUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        cartRepository.removePromoCode()
}
