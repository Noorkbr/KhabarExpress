package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for retrieving all orders placed by the current user.
 * Exposes a [Flow] that emits updated order history whenever it changes.
 *
 * @return [Flow] emitting a [List] of [Order] objects.
 */
class GetUserOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(): Flow<List<Order>> =
        orderRepository.getUserOrders()
}
