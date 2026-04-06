package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * UseCase for cancelling a placed order.
 * Only orders in cancellable statuses (e.g. PENDING, CONFIRMED) can be cancelled.
 *
 * @param orderId Unique identifier of the order to cancel.
 * @param reason  Human-readable reason for cancellation.
 * @return [Result] wrapping [Unit] on success or an error.
 */
class CancelOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, reason: String): Result<Unit> =
        orderRepository.cancelOrder(orderId, reason)
}
