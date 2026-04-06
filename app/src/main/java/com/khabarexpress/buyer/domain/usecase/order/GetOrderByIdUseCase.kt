package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * UseCase for fetching the details of a specific order.
 *
 * @param orderId Unique identifier of the order.
 * @return [Result] wrapping the [Order] on success or an error.
 */
class GetOrderByIdUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Order> =
        orderRepository.getOrderById(orderId)
}
