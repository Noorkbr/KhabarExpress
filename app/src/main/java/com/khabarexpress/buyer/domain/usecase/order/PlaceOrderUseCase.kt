package com.khabarexpress.buyer.domain.usecase.order

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.PaymentMethod
import com.khabarexpress.buyer.domain.repository.OrderRepository
import javax.inject.Inject

/**
 * UseCase for placing a new food order.
 *
 * @param restaurantId         Identifier of the restaurant the order is placed at.
 * @param deliveryAddress      Selected delivery address.
 * @param paymentMethod        Chosen payment method (bKash, Nagad, COD, etc.).
 * @param specialInstructions  Optional cooking or delivery instructions.
 * @return [Result] wrapping the created [Order] on success or an error.
 */
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
