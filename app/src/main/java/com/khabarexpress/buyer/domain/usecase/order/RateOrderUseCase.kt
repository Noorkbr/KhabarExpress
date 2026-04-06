package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * UseCase for submitting a rating and optional review for a delivered order.
 *
 * @param orderId Unique identifier of the delivered order.
 * @param rating  Integer rating from 1 to 5.
 * @param review  Optional written review text.
 * @return [Result] wrapping [Unit] on success or an error.
 */
class RateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, rating: Int, review: String?): Result<Unit> =
        orderRepository.rateOrder(orderId, rating, review)
}
