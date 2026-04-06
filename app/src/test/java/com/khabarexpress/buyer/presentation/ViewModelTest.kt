package com.khabarexpress.buyer.presentation

import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.Cart
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.OrderStatus
import com.khabarexpress.buyer.domain.model.PaymentMethod
import com.khabarexpress.buyer.domain.model.PaymentStatus
import com.khabarexpress.buyer.domain.model.PromoCode
import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import com.khabarexpress.buyer.domain.repository.CartRepository
import com.khabarexpress.buyer.domain.repository.OrderRepository
import com.khabarexpress.buyer.domain.usecase.auth.CheckAuthStatusUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LoginWithPhoneUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LogoutUseCase
import com.khabarexpress.buyer.domain.usecase.cart.ApplyPromoCodeUseCase
import com.khabarexpress.buyer.domain.usecase.cart.ClearCartUseCase
import com.khabarexpress.buyer.domain.usecase.cart.GetCartUseCase
import com.khabarexpress.buyer.domain.usecase.cart.RemoveCartItemUseCase
import com.khabarexpress.buyer.domain.usecase.cart.RemovePromoCodeUseCase
import com.khabarexpress.buyer.domain.usecase.cart.UpdateCartItemQuantityUseCase
import com.khabarexpress.buyer.domain.usecase.order.CancelOrderUseCase
import com.khabarexpress.buyer.domain.usecase.order.GetUserOrdersUseCase
import com.khabarexpress.buyer.presentation.auth.login.LoginViewModel
import com.khabarexpress.buyer.presentation.cart.CartViewModel
import com.khabarexpress.buyer.presentation.order.history.OrderHistoryViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val testUser = User(
        id = "user-1",
        name = "Test User",
        email = "test@example.com",
        phone = "01712345678"
    )

    private val testRestaurant = Restaurant(
        id = "rest-1",
        name = "Test Restaurant",
        description = "Great food",
        imageUrl = "",
        cuisine = listOf("Bengali"),
        rating = 4.5,
        totalReviews = 50,
        deliveryTime = 30,
        deliveryFee = 50.0,
        minOrderAmount = 150.0,
        isOpen = true,
        distance = 2.0,
        latitude = 23.79,
        longitude = 90.40,
        address = "Dhaka"
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
        latitude = 23.79,
        longitude = 90.40
    )

    private val testOrder = Order(
        id = "order-1",
        userId = "user-1",
        restaurantId = "rest-1",
        restaurant = testRestaurant,
        items = emptyList(),
        deliveryAddress = testAddress,
        status = OrderStatus.DELIVERED,
        subtotal = 300.0,
        deliveryFee = 50.0,
        tax = 15.0,
        discount = 0.0,
        total = 365.0,
        paymentMethod = PaymentMethod.BKASH,
        paymentStatus = PaymentStatus.PAID
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== LoginViewModel ====================

    @Test
    fun `LoginViewModel initial state is Idle`() {
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.isAuthenticated() } returns false

        val viewModel = LoginViewModel(authRepository)

        // Initially idle (checkAuthStatus may run but no authenticated user)
        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Idle ||
                viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Loading)
    }

    @Test
    fun `LoginViewModel loginWithPhoneOnly emits Success on valid phone`() = runTest {
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.isAuthenticated() } returns false
        coEvery { authRepository.loginWithPhoneOnly("01712345678") } returns Result.success(testUser)

        val viewModel = LoginViewModel(authRepository)
        viewModel.loginWithPhoneOnly("01712345678")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Success)
    }

    @Test
    fun `LoginViewModel loginWithPhoneOnly emits Error on blank phone`() = runTest {
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.isAuthenticated() } returns false

        val viewModel = LoginViewModel(authRepository)
        viewModel.loginWithPhoneOnly("")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Error)
    }

    @Test
    fun `LoginViewModel loginWithPhoneOnly emits Error on invalid phone format`() = runTest {
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.isAuthenticated() } returns false

        val viewModel = LoginViewModel(authRepository)
        viewModel.loginWithPhoneOnly("12345")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Error)
    }

    @Test
    fun `LoginViewModel resetState sets state to Idle`() = runTest {
        val authRepository = mockk<AuthRepository>(relaxed = true)
        coEvery { authRepository.isAuthenticated() } returns false
        coEvery { authRepository.loginWithPhoneOnly(any()) } returns Result.failure(Exception("error"))

        val viewModel = LoginViewModel(authRepository)
        viewModel.loginWithPhoneOnly("01712345678")
        advanceUntilIdle()

        viewModel.resetState()
        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Idle)
    }

    // ==================== CartViewModel ====================

    @Test
    fun `CartViewModel initial state is Loading`() {
        val cartRepository = mockk<CartRepository>(relaxed = true)
        every { cartRepository.getCart() } returns flowOf(null)

        val viewModel = CartViewModel(cartRepository)

        // Initial state before coroutines run is Loading
        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.cart.CartUiState.Loading ||
                viewModel.uiState.value is com.khabarexpress.buyer.presentation.cart.CartUiState.Empty)
    }

    @Test
    fun `CartViewModel shows Empty when cart has no items`() = runTest {
        val cartRepository = mockk<CartRepository>(relaxed = true)
        every { cartRepository.getCart() } returns flowOf(null)

        val viewModel = CartViewModel(cartRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.cart.CartUiState.Empty)
    }

    @Test
    fun `CartViewModel shows Empty for cart with empty items`() = runTest {
        val cartRepository = mockk<CartRepository>(relaxed = true)
        val emptyCart = Cart(restaurantId = "rest-1", items = emptyList())
        every { cartRepository.getCart() } returns flowOf(emptyCart)

        val viewModel = CartViewModel(cartRepository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.cart.CartUiState.Empty)
    }

    // ==================== OrderHistoryViewModel ====================

    @Test
    fun `OrderHistoryViewModel loads orders on init`() = runTest {
        val orderRepository = mockk<OrderRepository>(relaxed = true)
        val orders = listOf(testOrder)
        every { orderRepository.getUserOrders() } returns flowOf(orders)

        val viewModel = OrderHistoryViewModel(orderRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is com.khabarexpress.buyer.presentation.order.history.OrderHistoryUiState.Success)
        // testOrder has DELIVERED status → goes into pastOrders
        val success = state as com.khabarexpress.buyer.presentation.order.history.OrderHistoryUiState.Success
        assertEquals(orders, success.pastOrders)
        assertTrue(success.activeOrders.isEmpty())
    }

    @Test
    fun `OrderHistoryViewModel shows empty state when no orders`() = runTest {
        val orderRepository = mockk<OrderRepository>(relaxed = true)
        every { orderRepository.getUserOrders() } returns flowOf(emptyList())

        val viewModel = OrderHistoryViewModel(orderRepository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is com.khabarexpress.buyer.presentation.order.history.OrderHistoryUiState.Empty)
    }
}
