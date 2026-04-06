package com.khabarexpress.seller.domain.usecase.orders

import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * UseCase for rejecting an incoming order.
 *
 * @param orderId Unique identifier of the order to reject.
 * @param reason  Optional rejection reason shown to the customer.
 * @return [Result] wrapping the updated [Order] on success.
 */
class RejectOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        orderId: String,
        reason: String? = null
    ): Result<Order> = orderRepository.rejectOrder(orderId, reason)
}
