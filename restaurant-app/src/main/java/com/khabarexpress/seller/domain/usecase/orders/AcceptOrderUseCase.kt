package com.khabarexpress.seller.domain.usecase.orders

import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * UseCase for accepting an incoming order.
 *
 * @param orderId          Unique identifier of the order to accept.
 * @param estimatedPrepTime Optional estimated preparation time in minutes.
 * @return [Result] wrapping the updated [Order] on success.
 */
class AcceptOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        estimatedPrepTime: Int? = null
    ): Result<Order> = orderRepository.acceptOrder(orderId, estimatedPrepTime)
}
