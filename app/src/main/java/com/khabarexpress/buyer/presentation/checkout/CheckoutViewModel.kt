package com.khabarexpress.buyer.presentation.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.PaymentMethod
import com.khabarexpress.buyer.domain.repository.CartRepository
import com.khabarexpress.buyer.domain.repository.OrderRepository
import com.khabarexpress.buyer.domain.repository.UserRepository
import com.khabarexpress.buyer.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Checkout screen
 * Manages address selection, payment method, and order placement
 */
@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private val _placeOrderState = MutableStateFlow<PlaceOrderState>(PlaceOrderState.Idle)
    val placeOrderState: StateFlow<PlaceOrderState> = _placeOrderState.asStateFlow()

    private val _savedAddresses = MutableStateFlow<List<Address>>(emptyList())
    val savedAddresses: StateFlow<List<Address>> = _savedAddresses.asStateFlow()

    private var selectedAddress: Address? = null
    private var selectedPaymentMethod: PaymentMethod? = null
    private var specialInstructions: String? = null
    private var restaurantId: String? = null

    init {
        loadCheckoutData()
    }

    /**
     * Load checkout data (addresses, cart)
     */
    fun loadCheckoutData() {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.Loading
            
            try {
                // Collect addresses
                userRepository.getUserAddresses()
                    .catch { error ->
                        _uiState.value = CheckoutUiState.Error(
                            error.message ?: "Failed to load addresses"
                        )
                    }
                    .collect { addresses ->
                        // Collect cart for order summary
                        cartRepository.getCart()
                            .catch { error ->
                                _uiState.value = CheckoutUiState.Error(
                                    error.message ?: "Failed to load cart"
                                )
                            }
                            .collect { cart ->
                                if (cart == null || cart.items.isEmpty()) {
                                    _uiState.value = CheckoutUiState.Error("Cart is empty")
                                } else {
                                    restaurantId = cart.restaurantId
                                    val defaultAddress = addresses.find { it.isDefault } ?: addresses.firstOrNull()
                                    selectedAddress = defaultAddress
                                    _savedAddresses.value = addresses
                                    
                                    val orderSummary = OrderSummary(
                                        subtotal = cart.subtotal,
                                        discount = cart.discount,
                                        deliveryFee = Constants.DEFAULT_DELIVERY_FEE,
                                        tax = cart.subtotal * Constants.TAX_RATE,
                                        total = cart.subtotal - cart.discount + Constants.DEFAULT_DELIVERY_FEE + (cart.subtotal * Constants.TAX_RATE)
                                    )
                                    
                                    _uiState.value = CheckoutUiState.Success(
                                        addresses = addresses,
                                        selectedAddress = defaultAddress,
                                        selectedPaymentMethod = null,
                                        orderSummary = orderSummary
                                    )
                                }
                            }
                    }
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error(
                    e.message ?: "Failed to load checkout data"
                )
            }
        }
    }

    /**
     * Select delivery address
     */
    fun selectAddress(address: Address) {
        selectedAddress = address
        val currentState = _uiState.value
        if (currentState is CheckoutUiState.Success) {
            _uiState.value = currentState.copy(selectedAddress = address)
        }
    }
    
    /**
     * Add a new address
     */
    fun addAddress(address: Address) {
        viewModelScope.launch {
            try {
                userRepository.addAddress(address)
                loadCheckoutData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Update an existing address
     */
    fun updateAddress(address: Address) {
        viewModelScope.launch {
            try {
                userRepository.updateAddress(address)
                loadCheckoutData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Delete an address
     */
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            try {
                userRepository.deleteAddress(addressId)
                loadCheckoutData()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Select payment method
     */
    fun selectPaymentMethod(method: PaymentMethod) {
        selectedPaymentMethod = method
        val currentState = _uiState.value
        if (currentState is CheckoutUiState.Success) {
            _uiState.value = currentState.copy(selectedPaymentMethod = method)
        }
    }

    /**
     * Set special instructions
     */
    fun setSpecialInstructions(instructions: String) {
        specialInstructions = instructions.ifBlank { null }
    }

    /**
     * Place order
     */
    fun placeOrder() {
        val address = selectedAddress
        val paymentMethod = selectedPaymentMethod
        val restId = restaurantId

        when {
            address == null -> {
                _placeOrderState.value = PlaceOrderState.Error("Please select a delivery address")
                return
            }
            paymentMethod == null -> {
                _placeOrderState.value = PlaceOrderState.Error("Please select a payment method")
                return
            }
            restId == null -> {
                _placeOrderState.value = PlaceOrderState.Error("Restaurant information is missing")
                return
            }
        }

        viewModelScope.launch {
            _placeOrderState.value = PlaceOrderState.Loading
            orderRepository.placeOrder(
                restaurantId = restId,
                deliveryAddress = address,
                paymentMethod = paymentMethod,
                specialInstructions = specialInstructions
            )
                .onSuccess { order ->
                    // Clear cart after successful order
                    cartRepository.clearCart()
                    _placeOrderState.value = PlaceOrderState.Success(order)
                }
                .onFailure { error ->
                    _placeOrderState.value = PlaceOrderState.Error(
                        error.message ?: "Failed to place order"
                    )
                }
        }
    }

    /**
     * Reset place order state
     */
    fun resetPlaceOrderState() {
        _placeOrderState.value = PlaceOrderState.Idle
    }

    /**
     * Retry loading checkout data
     */
    fun retry() {
        loadCheckoutData()
    }
}

/**
 * UI state for Checkout screen
 */
sealed class CheckoutUiState {
    object Loading : CheckoutUiState()
    data class Success(
        val addresses: List<Address>,
        val selectedAddress: Address?,
        val selectedPaymentMethod: PaymentMethod?,
        val orderSummary: OrderSummary
    ) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

/**
 * Order summary data
 */
data class OrderSummary(
    val subtotal: Double,
    val discount: Double,
    val deliveryFee: Double,
    val tax: Double,
    val total: Double
)

/**
 * State for place order operation
 */
sealed class PlaceOrderState {
    object Idle : PlaceOrderState()
    object Loading : PlaceOrderState()
    data class Success(val order: Order) : PlaceOrderState()
    data class Error(val message: String) : PlaceOrderState()
}
