package com.khabarexpress.buyer.presentation.order.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.OrderStatus
import com.khabarexpress.buyer.domain.model.OrderTracking
import com.khabarexpress.buyer.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Order Tracking screen
 * Manages real-time order tracking, rider location, and status updates
 */
@HiltViewModel
class OrderTrackingViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderTrackingUiState>(OrderTrackingUiState.Loading)
    val uiState: StateFlow<OrderTrackingUiState> = _uiState.asStateFlow()

    private val _cancelOrderState = MutableStateFlow<CancelOrderState>(CancelOrderState.Idle)
    val cancelOrderState: StateFlow<CancelOrderState> = _cancelOrderState.asStateFlow()

    /**
     * Load order details
     */
    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _uiState.value = OrderTrackingUiState.Loading
            orderRepository.getOrderById(orderId)
                .onSuccess { order ->
                    _uiState.value = OrderTrackingUiState.Success(
                        order = order,
                        tracking = null
                    )
                    // Start tracking if order is active
                    if (isOrderActive(order.status)) {
                        subscribeToTracking(orderId)
                    }
                }
                .onFailure { error ->
                    _uiState.value = OrderTrackingUiState.Error(
                        error.message ?: "Failed to load order details"
                    )
                }
        }
    }

    /**
     * Subscribe to real-time order tracking updates
     */
    fun subscribeToTracking(orderId: String) {
        viewModelScope.launch {
            orderRepository.trackOrder(orderId)
                .catch { error ->
                    // Don't change state to error, just show old data
                    // Tracking can fail temporarily
                }
                .collect { tracking ->
                    val currentState = _uiState.value
                    if (currentState is OrderTrackingUiState.Success) {
                        _uiState.value = currentState.copy(tracking = tracking)
                    }
                }
        }
    }

    /**
     * Cancel order
     */
    fun cancelOrder(orderId: String, reason: String) {
        if (reason.isBlank()) {
            _cancelOrderState.value = CancelOrderState.Error("Please provide a reason for cancellation")
            return
        }

        val currentState = _uiState.value
        if (currentState is OrderTrackingUiState.Success) {
            if (!canCancelOrder(currentState.order.status)) {
                _cancelOrderState.value = CancelOrderState.Error(
                    "Order cannot be cancelled at this stage"
                )
                return
            }
        }

        viewModelScope.launch {
            _cancelOrderState.value = CancelOrderState.Loading
            orderRepository.cancelOrder(orderId, reason)
                .onSuccess {
                    _cancelOrderState.value = CancelOrderState.Success
                    // Reload order to show updated status
                    loadOrderDetails(orderId)
                }
                .onFailure { error ->
                    _cancelOrderState.value = CancelOrderState.Error(
                        error.message ?: "Failed to cancel order"
                    )
                }
        }
    }

    /**
     * Reset cancel order state
     */
    fun resetCancelOrderState() {
        _cancelOrderState.value = CancelOrderState.Idle
    }

    /**
     * Retry loading order
     */
    fun retry(orderId: String) {
        loadOrderDetails(orderId)
    }

    /**
     * Check if order is in an active state
     */
    private fun isOrderActive(status: OrderStatus): Boolean {
        return when (status) {
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.PICKED_UP,
            OrderStatus.ON_THE_WAY -> true
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED -> false
        }
    }

    /**
     * Check if order can be cancelled
     */
    private fun canCancelOrder(status: OrderStatus): Boolean {
        return when (status) {
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED -> true
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.PICKED_UP,
            OrderStatus.ON_THE_WAY,
            OrderStatus.DELIVERED,
            OrderStatus.CANCELLED -> false
        }
    }

    /**
     * Get progress percentage for order status
     */
    fun getProgressPercentage(status: OrderStatus): Float {
        return when (status) {
            OrderStatus.PENDING -> 0.1f
            OrderStatus.CONFIRMED -> 0.2f
            OrderStatus.PREPARING -> 0.4f
            OrderStatus.READY_FOR_PICKUP -> 0.6f
            OrderStatus.PICKED_UP -> 0.7f
            OrderStatus.ON_THE_WAY -> 0.85f
            OrderStatus.DELIVERED -> 1.0f
            OrderStatus.CANCELLED -> 0.0f
        }
    }

    /**
     * Get status display text
     */
    fun getStatusText(status: OrderStatus): String {
        return when (status) {
            OrderStatus.PENDING -> "Order Placed"
            OrderStatus.CONFIRMED -> "Order Confirmed"
            OrderStatus.PREPARING -> "Preparing Your Food"
            OrderStatus.READY_FOR_PICKUP -> "Ready for Pickup"
            OrderStatus.PICKED_UP -> "Picked Up by Rider"
            OrderStatus.ON_THE_WAY -> "On the Way"
            OrderStatus.DELIVERED -> "Delivered"
            OrderStatus.CANCELLED -> "Cancelled"
        }
    }
}

/**
 * UI state for Order Tracking screen
 */
sealed class OrderTrackingUiState {
    object Loading : OrderTrackingUiState()
    data class Success(
        val order: Order,
        val tracking: OrderTracking?
    ) : OrderTrackingUiState()
    data class Error(val message: String) : OrderTrackingUiState()
}

/**
 * State for cancel order operation
 */
sealed class CancelOrderState {
    object Idle : CancelOrderState()
    object Loading : CancelOrderState()
    object Success : CancelOrderState()
    data class Error(val message: String) : CancelOrderState()
}
