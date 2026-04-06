package com.khabarexpress.seller.domain.usecase.orders

import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * UseCase for fetching all orders with an optional status filter.
 * Pass `status = "pending"` to retrieve only new/unconfirmed orders.
 *
 * @param status Optional order status filter (e.g. "pending", "preparing").
 * @return [Result] wrapping the list of [Order] objects on success.
 */
class GetPendingOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(status: String? = "pending"): Result<List<Order>> =
        orderRepository.getOrders(status = status)
}
