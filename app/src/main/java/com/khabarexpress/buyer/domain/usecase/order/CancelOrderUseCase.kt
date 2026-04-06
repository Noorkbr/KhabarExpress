package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.repository.OrderRepository
import javax.inject.Inject

class CancelOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, reason: String): Result<Unit> =
        orderRepository.cancelOrder(orderId, reason)
}
