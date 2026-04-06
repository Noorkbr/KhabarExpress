package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.model.Cart
import com.khabarexpress.buyer.domain.model.CartItem
import com.khabarexpress.buyer.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(): Flow<Cart?> =
        cartRepository.getCart()

    suspend fun addItem(item: CartItem): Result<Unit> =
        cartRepository.addItem(item)
}
