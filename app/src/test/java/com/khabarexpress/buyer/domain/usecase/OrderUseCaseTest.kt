package com.khabarexpress.buyer.domain.usecase

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.OrderStatus
import com.khabarexpress.buyer.domain.model.PaymentMethod
import com.khabarexpress.buyer.domain.model.PaymentStatus
import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.OrderRepository
import com.khabarexpress.buyer.domain.usecase.order.CancelOrderUseCase
import com.khabarexpress.buyer.domain.usecase.order.GetActiveOrdersUseCase
import com.khabarexpress.buyer.domain.usecase.order.GetOrderByIdUseCase
import com.khabarexpress.buyer.domain.usecase.order.GetUserOrdersUseCase
import com.khabarexpress.buyer.domain.usecase.order.PlaceOrderUseCase
import com.khabarexpress.buyer.domain.usecase.order.RateOrderUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OrderUseCaseTest {

    private lateinit var orderRepository: OrderRepository
    private lateinit var placeOrderUseCase: PlaceOrderUseCase
    private lateinit var getOrderByIdUseCase: GetOrderByIdUseCase
    private lateinit var getUserOrdersUseCase: GetUserOrdersUseCase
    private lateinit var getActiveOrdersUseCase: GetActiveOrdersUseCase
    private lateinit var cancelOrderUseCase: CancelOrderUseCase
    private lateinit var rateOrderUseCase: RateOrderUseCase

    private val testRestaurant = Restaurant(
        id = "rest-1",
        name = "Test Restaurant",
        description = "Great food",
        imageUrl = "https://example.com/img.jpg",
        cuisine = listOf("Bengali"),
        rating = 4.5,
        totalReviews = 100,
        deliveryTime = 30,
        deliveryFee = 50.0,
        minOrderAmount = 150.0,
        isOpen = true,
        distance = 2.5,
        latitude = 23.7945,
        longitude = 90.4044,
        address = "Gulshan, Dhaka"
    )

    private val testAddress = Address(
        id = "addr-1",
        label = "Home",
        houseNo = "10",
        roadNo = "5",
        area = "Gulshan",
        thana = "Gulshan",
        district = "Dhaka",
        division = "Dhaka",
        postalCode = "1212",
        latitude = 23.7945,
        longitude = 90.4044
    )

    private val testOrder = Order(
        id = "order-1",
        userId = "user-1",
        restaurantId = "rest-1",
        restaurant = testRestaurant,
        items = emptyList(),
        deliveryAddress = testAddress,
        status = OrderStatus.PENDING,
        subtotal = 300.0,
        deliveryFee = 50.0,
        tax = 15.0,
        discount = 0.0,
        total = 365.0,
        paymentMethod = PaymentMethod.BKASH,
        paymentStatus = PaymentStatus.PENDING
    )

    @Before
    fun setUp() {
        orderRepository = mockk()
        placeOrderUseCase = PlaceOrderUseCase(orderRepository)
        getOrderByIdUseCase = GetOrderByIdUseCase(orderRepository)
        getUserOrdersUseCase = GetUserOrdersUseCase(orderRepository)
        getActiveOrdersUseCase = GetActiveOrdersUseCase(orderRepository)
        cancelOrderUseCase = CancelOrderUseCase(orderRepository)
        rateOrderUseCase = RateOrderUseCase(orderRepository)
    }

    @Test
    fun `placeOrder returns order on success`() = runTest {
        coEvery {
            orderRepository.placeOrder("rest-1", testAddress, PaymentMethod.BKASH, null)
        } returns Result.success(testOrder)

        val result = placeOrderUseCase("rest-1", testAddress, PaymentMethod.BKASH, null)

        assertTrue(result.isSuccess)
        assertEquals(testOrder, result.getOrNull())
        coVerify { orderRepository.placeOrder("rest-1", testAddress, PaymentMethod.BKASH, null) }
    }

    @Test
    fun `placeOrder propagates failure from repository`() = runTest {
        coEvery {
            orderRepository.placeOrder("rest-1", testAddress, PaymentMethod.BKASH, null)
        } returns Result.failure(Exception("Restaurant is closed"))

        val result = placeOrderUseCase("rest-1", testAddress, PaymentMethod.BKASH, null)

        assertTrue(result.isFailure)
        assertEquals("Restaurant is closed", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getOrderById returns order on success`() = runTest {
        coEvery { orderRepository.getOrderById("order-1") } returns Result.success(testOrder)

        val result = getOrderByIdUseCase("order-1")

        assertTrue(result.isSuccess)
        assertEquals(testOrder, result.getOrNull())
    }

    @Test
    fun `getUserOrders emits orders from repository`() = runTest {
        val orders = listOf(testOrder)
        every { orderRepository.getUserOrders() } returns flowOf(orders)

        val result = getUserOrdersUseCase().first()

        assertEquals(orders, result)
        verify { orderRepository.getUserOrders() }
    }

    @Test
    fun `getActiveOrders emits active orders`() = runTest {
        val activeOrders = listOf(testOrder.copy(status = OrderStatus.PREPARING))
        every { orderRepository.getActiveOrders() } returns flowOf(activeOrders)

        val result = getActiveOrdersUseCase().first()

        assertEquals(activeOrders, result)
    }

    @Test
    fun `cancelOrder delegates to repository`() = runTest {
        coEvery { orderRepository.cancelOrder("order-1", "Changed my mind") } returns Result.success(Unit)

        val result = cancelOrderUseCase("order-1", "Changed my mind")

        assertTrue(result.isSuccess)
        coVerify { orderRepository.cancelOrder("order-1", "Changed my mind") }
    }

    @Test
    fun `cancelOrder returns failure when order cannot be cancelled`() = runTest {
        coEvery {
            orderRepository.cancelOrder("order-1", "Late")
        } returns Result.failure(Exception("Order already delivered"))

        val result = cancelOrderUseCase("order-1", "Late")

        assertTrue(result.isFailure)
        assertEquals("Order already delivered", result.exceptionOrNull()?.message)
    }

    @Test
    fun `rateOrder delegates to repository with valid rating`() = runTest {
        coEvery { orderRepository.rateOrder("order-1", 5, "Excellent!") } returns Result.success(Unit)

        val result = rateOrderUseCase("order-1", 5, "Excellent!")

        assertTrue(result.isSuccess)
        coVerify { orderRepository.rateOrder("order-1", 5, "Excellent!") }
    }
}
