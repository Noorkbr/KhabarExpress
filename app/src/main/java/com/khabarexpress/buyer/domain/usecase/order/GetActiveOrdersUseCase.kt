package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveOrdersUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(): Flow<List<Order>> =
        orderRepository.getActiveOrders()
}
