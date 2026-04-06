package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.model.OrderTracking
import com.khabarexpress.buyer.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase for subscribing to real-time tracking updates of an active order.
 * Emits rider location and order status changes via a [Flow].
 *
 * @param orderId Unique identifier of the order to track.
 * @return [Flow] emitting [OrderTracking] updates.
 */
class TrackOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    operator fun invoke(orderId: String): Flow<OrderTracking> =
        orderRepository.trackOrder(orderId)
}
