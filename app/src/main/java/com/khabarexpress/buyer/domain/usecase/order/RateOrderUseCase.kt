package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.repository.OrderRepository
import javax.inject.Inject

class RateOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, rating: Int, review: String?): Result<Unit> =
        orderRepository.rateOrder(orderId, rating, review)
}
