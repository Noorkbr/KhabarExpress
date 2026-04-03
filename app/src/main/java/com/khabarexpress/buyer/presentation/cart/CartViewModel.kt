package com.khabarexpress.buyer.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Cart
import com.khabarexpress.buyer.domain.model.PromoCode
import com.khabarexpress.buyer.domain.repository.CartRepository
import com.khabarexpress.buyer.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Cart screen
 * Manages cart items, quantities, promo codes, and totals
 */
@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _promoCodeState = MutableStateFlow<PromoCodeState>(PromoCodeState.Idle)
    val promoCodeState: StateFlow<PromoCodeState> = _promoCodeState.asStateFlow()

    init {
        loadCart()
    }

    /**
     * Load cart items
     */
    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            cartRepository.getCart()
                .catch { error ->
                    _uiState.value = CartUiState.Error(
                        error.message ?: "Failed to load cart"
                    )
                }
                .collect { cart ->
                    if (cart == null || cart.items.isEmpty()) {
                        _uiState.value = CartUiState.Empty
                    } else {
                        val totals = calculateTotals(cart)
                        _uiState.value = CartUiState.Success(
                            cart = cart,
                            totals = totals
                        )
                    }
                }
        }
    }

    /**
     * Update item quantity
     */
    fun updateQuantity(itemId: String, quantity: Int) {
        if (quantity < 1) {
            removeItem(itemId)
            return
        }

        viewModelScope.launch {
            cartRepository.updateQuantity(itemId, quantity)
                .onFailure { error ->
                    _uiState.value = CartUiState.Error(
                        error.message ?: "Failed to update quantity"
                    )
                }
        }
    }

    /**
     * Remove item from cart
     */
    fun removeItem(itemId: String) {
        viewModelScope.launch {
            cartRepository.removeItem(itemId)
                .onFailure { error ->
                    _uiState.value = CartUiState.Error(
                        error.message ?: "Failed to remove item"
                    )
                }
        }
    }

    /**
     * Clear entire cart
     */
    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
                .onFailure { error ->
                    _uiState.value = CartUiState.Error(
                        error.message ?: "Failed to clear cart"
                    )
                }
        }
    }

    /**
     * Apply promo code
     */
    fun applyPromoCode(code: String) {
        if (code.isBlank()) {
            _promoCodeState.value = PromoCodeState.Error("Promo code cannot be empty")
            return
        }

        val currentState = _uiState.value
        if (currentState !is CartUiState.Success) {
            _promoCodeState.value = PromoCodeState.Error("Cart is empty")
            return
        }

        viewModelScope.launch {
            _promoCodeState.value = PromoCodeState.Loading
            cartRepository.applyPromoCode(code)
                .onSuccess { promoCode ->
                    _promoCodeState.value = PromoCodeState.Success(promoCode)
                }
                .onFailure { error ->
                    _promoCodeState.value = PromoCodeState.Error(
                        error.message ?: "Invalid promo code"
                    )
                }
        }
    }

    /**
     * Remove applied promo code
     */
    fun removePromoCode() {
        viewModelScope.launch {
            cartRepository.removePromoCode()
                .onSuccess {
                    _promoCodeState.value = PromoCodeState.Idle
                }
                .onFailure { error ->
                    _promoCodeState.value = PromoCodeState.Error(
                        error.message ?: "Failed to remove promo code"
                    )
                }
        }
    }

    /**
     * Calculate cart totals
     */
    fun calculateTotals(cart: Cart): CartTotals {
        val subtotal = cart.subtotal
        val discount = cart.discount
        val deliveryFee = Constants.DEFAULT_DELIVERY_FEE
        val tax = subtotal * Constants.TAX_RATE
        val total = subtotal - discount + deliveryFee + tax

        return CartTotals(
            subtotal = subtotal,
            discount = discount,
            deliveryFee = deliveryFee,
            tax = tax,
            total = total
        )
    }

    /**
     * Reset promo code state
     */
    fun resetPromoCodeState() {
        _promoCodeState.value = PromoCodeState.Idle
    }

    /**
     * Retry loading cart
     */
    fun retry() {
        loadCart()
    }
}

/**
 * UI state for Cart screen
 */
sealed class CartUiState {
    object Loading : CartUiState()
    object Empty : CartUiState()
    data class Success(
        val cart: Cart,
        val totals: CartTotals
    ) : CartUiState()
    data class Error(val message: String) : CartUiState()
}

/**
 * Cart totals data
 */
data class CartTotals(
    val subtotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val tax: Double,
    val total: Double
)

/**
 * State for promo code operations
 */
sealed class PromoCodeState {
    object Idle : PromoCodeState()
    object Loading : PromoCodeState()
    data class Success(val promoCode: PromoCode) : PromoCodeState()
    data class Error(val message: String) : PromoCodeState()
}
