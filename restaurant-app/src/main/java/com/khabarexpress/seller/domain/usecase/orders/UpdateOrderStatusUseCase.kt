package com.khabarexpress.seller.domain.usecase.orders

import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * UseCase for advancing an order through the fulfilment pipeline.
 * Valid status values: "confirmed", "preparing", "ready", "picked_up", "delivered".
 *
 * @param orderId Unique identifier of the order to update.
 * @param status  New status string.
 * @return [Result] wrapping the updated [Order] on success.
 */
class UpdateOrderStatusUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String, status: String): Result<Order> =
        orderRepository.updateOrderStatus(orderId, status)
}
