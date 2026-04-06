package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.model.OrderTracking
import com.khabarexpress.buyer.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(orderId: String): Flow<OrderTracking> =
        orderRepository.trackOrder(orderId)
}
