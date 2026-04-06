package com.khabarexpress.buyer.domain.usecase.cart

import com.khabarexpress.buyer.domain.model.Cart
import com.khabarexpress.buyer.domain.model.CartItem
import com.khabarexpress.buyer.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for accessing the current shopping cart.
 * Exposes a [Flow] that emits whenever the cart changes, and provides an
 * [addItem] helper to add a new [CartItem].
 *
 * @return [Flow] emitting the current [Cart], or `null` when the cart is empty.
 */
class GetCartUseCase @Inject constructor(
    private val cartRepository: CartRepository
) {
    operator fun invoke(): Flow<Cart?> =
        cartRepository.getCart()

    suspend fun addItem(item: CartItem): Result<Unit> =
        cartRepository.addItem(item)
}
