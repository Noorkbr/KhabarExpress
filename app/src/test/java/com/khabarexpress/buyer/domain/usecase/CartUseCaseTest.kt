package com.khabarexpress.buyer.domain.usecase

import com.khabarexpress.buyer.domain.model.Cart
import com.khabarexpress.buyer.domain.model.PromoCode
import com.khabarexpress.buyer.domain.repository.CartRepository
import com.khabarexpress.buyer.domain.usecase.cart.ApplyPromoCodeUseCase
import com.khabarexpress.buyer.domain.usecase.cart.ClearCartUseCase
import com.khabarexpress.buyer.domain.usecase.cart.GetCartUseCase
import com.khabarexpress.buyer.domain.usecase.cart.RemoveCartItemUseCase
import com.khabarexpress.buyer.domain.usecase.cart.RemovePromoCodeUseCase
import com.khabarexpress.buyer.domain.usecase.cart.UpdateCartItemQuantityUseCase
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

class CartUseCaseTest {

    private lateinit var cartRepository: CartRepository
    private lateinit var getCartUseCase: GetCartUseCase
    private lateinit var applyPromoCodeUseCase: ApplyPromoCodeUseCase
    private lateinit var clearCartUseCase: ClearCartUseCase
    private lateinit var removeCartItemUseCase: RemoveCartItemUseCase
    private lateinit var removePromoCodeUseCase: RemovePromoCodeUseCase
    private lateinit var updateCartItemQuantityUseCase: UpdateCartItemQuantityUseCase

    private val testCart = Cart(restaurantId = "rest-1", items = emptyList())
    private val testPromoCode = PromoCode(
        code = "WELCOME50",
        description = "50% off for new users",
        discountType = com.khabarexpress.buyer.domain.model.DiscountType.PERCENTAGE,
        discountValue = 50.0,
        minOrderAmount = 200.0,
        validUntil = System.currentTimeMillis() + 86400000,
        isActive = true
    )

    @Before
    fun setUp() {
        cartRepository = mockk()
        getCartUseCase = GetCartUseCase(cartRepository)
        applyPromoCodeUseCase = ApplyPromoCodeUseCase(cartRepository)
        clearCartUseCase = ClearCartUseCase(cartRepository)
        removeCartItemUseCase = RemoveCartItemUseCase(cartRepository)
        removePromoCodeUseCase = RemovePromoCodeUseCase(cartRepository)
        updateCartItemQuantityUseCase = UpdateCartItemQuantityUseCase(cartRepository)
    }

    @Test
    fun `getCart emits cart from repository`() = runTest {
        every { cartRepository.getCart() } returns flowOf(testCart)

        val result = getCartUseCase().first()

        assertEquals(testCart, result)
        verify { cartRepository.getCart() }
    }

    @Test
    fun `getCart emits null when cart is empty`() = runTest {
        every { cartRepository.getCart() } returns flowOf(null)

        val result = getCartUseCase().first()

        assertEquals(null, result)
    }

    @Test
    fun `applyPromoCode returns promo code on success`() = runTest {
        coEvery { cartRepository.applyPromoCode("WELCOME50") } returns Result.success(testPromoCode)

        val result = applyPromoCodeUseCase("WELCOME50")

        assertTrue(result.isSuccess)
        assertEquals(testPromoCode, result.getOrNull())
        coVerify { cartRepository.applyPromoCode("WELCOME50") }
    }

    @Test
    fun `applyPromoCode returns failure on invalid code`() = runTest {
        coEvery { cartRepository.applyPromoCode("INVALID") } returns Result.failure(
            Exception("Invalid promo code")
        )

        val result = applyPromoCodeUseCase("INVALID")

        assertTrue(result.isFailure)
        assertEquals("Invalid promo code", result.exceptionOrNull()?.message)
    }

    @Test
    fun `clearCart delegates to repository`() = runTest {
        coEvery { cartRepository.clearCart() } returns Result.success(Unit)

        val result = clearCartUseCase()

        assertTrue(result.isSuccess)
        coVerify { cartRepository.clearCart() }
    }

    @Test
    fun `removeCartItem delegates to repository`() = runTest {
        coEvery { cartRepository.removeItem("item-1") } returns Result.success(Unit)

        val result = removeCartItemUseCase("item-1")

        assertTrue(result.isSuccess)
        coVerify { cartRepository.removeItem("item-1") }
    }

    @Test
    fun `removePromoCode delegates to repository`() = runTest {
        coEvery { cartRepository.removePromoCode() } returns Result.success(Unit)

        val result = removePromoCodeUseCase()

        assertTrue(result.isSuccess)
        coVerify { cartRepository.removePromoCode() }
    }

    @Test
    fun `updateCartItemQuantity delegates to repository`() = runTest {
        coEvery { cartRepository.updateQuantity("item-1", 3) } returns Result.success(Unit)

        val result = updateCartItemQuantityUseCase("item-1", 3)

        assertTrue(result.isSuccess)
        coVerify { cartRepository.updateQuantity("item-1", 3) }
    }

    @Test
    fun `updateCartItemQuantity returns failure when item not found`() = runTest {
        coEvery { cartRepository.updateQuantity("missing-item", 2) } returns Result.failure(
            Exception("Item not found")
        )

        val result = updateCartItemQuantityUseCase("missing-item", 2)

        assertTrue(result.isFailure)
    }
}
