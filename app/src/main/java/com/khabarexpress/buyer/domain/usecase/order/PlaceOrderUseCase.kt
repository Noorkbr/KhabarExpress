package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.PaymentMethod
import com.khabarexpress.buyer.domain.repository.OrderRepository
import javax.inject.Inject

class PlaceOrderUseCase @Inject constructor(
    private val orderRepository: OrderRepository
) {
    suspend operator fun invoke(
        restaurantId: String,
        deliveryAddress: Address,
        paymentMethod: PaymentMethod,
        specialInstructions: String?
    ): Result<Order> = orderRepository.placeOrder(
        restaurantId, deliveryAddress, paymentMethod, specialInstructions
    )
}
