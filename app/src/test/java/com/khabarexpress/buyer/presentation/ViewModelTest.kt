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
import com.khabarexpress.buyer.domain.usecase.auth.CheckAuthStatusUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LoginWithCredentialsUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LoginWithOtpUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LoginWithPhoneUseCase
import com.khabarexpress.buyer.domain.usecase.auth.SendOtpUseCase
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
        val loginWithPhoneUseCase = mockk<LoginWithPhoneUseCase>(relaxed = true)
        val loginWithCredentialsUseCase = mockk<LoginWithCredentialsUseCase>(relaxed = true)
        val loginWithOtpUseCase = mockk<LoginWithOtpUseCase>(relaxed = true)
        val sendOtpUseCase = mockk<SendOtpUseCase>(relaxed = true)
        val checkAuthStatusUseCase = mockk<CheckAuthStatusUseCase>(relaxed = true)
        coEvery { checkAuthStatusUseCase.isAuthenticated() } returns false

        val viewModel = LoginViewModel(
            loginWithPhoneUseCase, loginWithCredentialsUseCase,
            loginWithOtpUseCase, sendOtpUseCase, checkAuthStatusUseCase
        )

        // Initially idle (checkAuthStatus may run but no authenticated user)
        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Idle ||
                viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Loading)
    }

    @Test
    fun `LoginViewModel loginWithPhoneOnly emits Success on valid phone`() = runTest {
        val loginWithPhoneUseCase = mockk<LoginWithPhoneUseCase>(relaxed = true)
        val loginWithCredentialsUseCase = mockk<LoginWithCredentialsUseCase>(relaxed = true)
        val loginWithOtpUseCase = mockk<LoginWithOtpUseCase>(relaxed = true)
        val sendOtpUseCase = mockk<SendOtpUseCase>(relaxed = true)
        val checkAuthStatusUseCase = mockk<CheckAuthStatusUseCase>(relaxed = true)
        coEvery { checkAuthStatusUseCase.isAuthenticated() } returns false
        coEvery { loginWithPhoneUseCase("01712345678") } returns Result.success(testUser)

        val viewModel = LoginViewModel(
            loginWithPhoneUseCase, loginWithCredentialsUseCase,
            loginWithOtpUseCase, sendOtpUseCase, checkAuthStatusUseCase
        )
        viewModel.loginWithPhoneOnly("01712345678")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Success)
    }

    @Test
    fun `LoginViewModel loginWithPhoneOnly emits Error on blank phone`() = runTest {
        val loginWithPhoneUseCase = mockk<LoginWithPhoneUseCase>(relaxed = true)
        val loginWithCredentialsUseCase = mockk<LoginWithCredentialsUseCase>(relaxed = true)
        val loginWithOtpUseCase = mockk<LoginWithOtpUseCase>(relaxed = true)
        val sendOtpUseCase = mockk<SendOtpUseCase>(relaxed = true)
        val checkAuthStatusUseCase = mockk<CheckAuthStatusUseCase>(relaxed = true)
        coEvery { checkAuthStatusUseCase.isAuthenticated() } returns false

        val viewModel = LoginViewModel(
            loginWithPhoneUseCase, loginWithCredentialsUseCase,
            loginWithOtpUseCase, sendOtpUseCase, checkAuthStatusUseCase
        )
        viewModel.loginWithPhoneOnly("")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Error)
    }

    @Test
    fun `LoginViewModel loginWithPhoneOnly emits Error on invalid phone format`() = runTest {
        val loginWithPhoneUseCase = mockk<LoginWithPhoneUseCase>(relaxed = true)
        val loginWithCredentialsUseCase = mockk<LoginWithCredentialsUseCase>(relaxed = true)
        val loginWithOtpUseCase = mockk<LoginWithOtpUseCase>(relaxed = true)
        val sendOtpUseCase = mockk<SendOtpUseCase>(relaxed = true)
        val checkAuthStatusUseCase = mockk<CheckAuthStatusUseCase>(relaxed = true)
        coEvery { checkAuthStatusUseCase.isAuthenticated() } returns false

        val viewModel = LoginViewModel(
            loginWithPhoneUseCase, loginWithCredentialsUseCase,
            loginWithOtpUseCase, sendOtpUseCase, checkAuthStatusUseCase
        )
        viewModel.loginWithPhoneOnly("12345")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Error)
    }

    @Test
    fun `LoginViewModel resetState sets state to Idle`() = runTest {
        val loginWithPhoneUseCase = mockk<LoginWithPhoneUseCase>(relaxed = true)
        val loginWithCredentialsUseCase = mockk<LoginWithCredentialsUseCase>(relaxed = true)
        val loginWithOtpUseCase = mockk<LoginWithOtpUseCase>(relaxed = true)
        val sendOtpUseCase = mockk<SendOtpUseCase>(relaxed = true)
        val checkAuthStatusUseCase = mockk<CheckAuthStatusUseCase>(relaxed = true)
        coEvery { checkAuthStatusUseCase.isAuthenticated() } returns false
        coEvery { loginWithPhoneUseCase(any()) } returns Result.failure(Exception("error"))

        val viewModel = LoginViewModel(
            loginWithPhoneUseCase, loginWithCredentialsUseCase,
            loginWithOtpUseCase, sendOtpUseCase, checkAuthStatusUseCase
        )
        viewModel.loginWithPhoneOnly("01712345678")
        advanceUntilIdle()

        viewModel.resetState()
        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.auth.login.LoginUiState.Idle)
    }

    // ==================== CartViewModel ====================

    @Test
    fun `CartViewModel initial state is Loading`() {
        val getCartUseCase = mockk<GetCartUseCase>(relaxed = true)
        val updateCartItemQuantityUseCase = mockk<UpdateCartItemQuantityUseCase>(relaxed = true)
        val removeCartItemUseCase = mockk<RemoveCartItemUseCase>(relaxed = true)
        val clearCartUseCase = mockk<ClearCartUseCase>(relaxed = true)
        val applyPromoCodeUseCase = mockk<ApplyPromoCodeUseCase>(relaxed = true)
        val removePromoCodeUseCase = mockk<RemovePromoCodeUseCase>(relaxed = true)
        every { getCartUseCase() } returns flowOf(null)

        val viewModel = CartViewModel(
            getCartUseCase, updateCartItemQuantityUseCase, removeCartItemUseCase,
            clearCartUseCase, applyPromoCodeUseCase, removePromoCodeUseCase
        )

        // Initial state before coroutines run is Loading
        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.cart.CartUiState.Loading ||
                viewModel.uiState.value is com.khabarexpress.buyer.presentation.cart.CartUiState.Empty)
    }

    @Test
    fun `CartViewModel shows Empty when cart has no items`() = runTest {
        val getCartUseCase = mockk<GetCartUseCase>(relaxed = true)
        val updateCartItemQuantityUseCase = mockk<UpdateCartItemQuantityUseCase>(relaxed = true)
        val removeCartItemUseCase = mockk<RemoveCartItemUseCase>(relaxed = true)
        val clearCartUseCase = mockk<ClearCartUseCase>(relaxed = true)
        val applyPromoCodeUseCase = mockk<ApplyPromoCodeUseCase>(relaxed = true)
        val removePromoCodeUseCase = mockk<RemovePromoCodeUseCase>(relaxed = true)
        every { getCartUseCase() } returns flowOf(null)

        val viewModel = CartViewModel(
            getCartUseCase, updateCartItemQuantityUseCase, removeCartItemUseCase,
            clearCartUseCase, applyPromoCodeUseCase, removePromoCodeUseCase
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.cart.CartUiState.Empty)
    }

    @Test
    fun `CartViewModel shows Empty for cart with empty items`() = runTest {
        val getCartUseCase = mockk<GetCartUseCase>(relaxed = true)
        val updateCartItemQuantityUseCase = mockk<UpdateCartItemQuantityUseCase>(relaxed = true)
        val removeCartItemUseCase = mockk<RemoveCartItemUseCase>(relaxed = true)
        val clearCartUseCase = mockk<ClearCartUseCase>(relaxed = true)
        val applyPromoCodeUseCase = mockk<ApplyPromoCodeUseCase>(relaxed = true)
        val removePromoCodeUseCase = mockk<RemovePromoCodeUseCase>(relaxed = true)
        val emptyCart = Cart(restaurantId = "rest-1", items = emptyList())
        every { getCartUseCase() } returns flowOf(emptyCart)

        val viewModel = CartViewModel(
            getCartUseCase, updateCartItemQuantityUseCase, removeCartItemUseCase,
            clearCartUseCase, applyPromoCodeUseCase, removePromoCodeUseCase
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is com.khabarexpress.buyer.presentation.cart.CartUiState.Empty)
    }

    // ==================== OrderHistoryViewModel ====================

    @Test
    fun `OrderHistoryViewModel loads orders on init`() = runTest {
        val getUserOrdersUseCase = mockk<GetUserOrdersUseCase>(relaxed = true)
        val orders = listOf(testOrder)
        every { getUserOrdersUseCase() } returns flowOf(orders)

        val viewModel = OrderHistoryViewModel(getUserOrdersUseCase)
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
        val getUserOrdersUseCase = mockk<GetUserOrdersUseCase>(relaxed = true)
        every { getUserOrdersUseCase() } returns flowOf(emptyList())

        val viewModel = OrderHistoryViewModel(getUserOrdersUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is com.khabarexpress.buyer.presentation.order.history.OrderHistoryUiState.Empty)
    }
}
