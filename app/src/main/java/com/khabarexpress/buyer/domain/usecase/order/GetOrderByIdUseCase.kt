package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.repository.OrderRepository
import javax.inject.Inject

class GetOrderByIdUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(orderId: String): Result<Order> =
        orderRepository.getOrderById(orderId)
}
