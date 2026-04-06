package com.khabarexpress.buyer.presentation.order.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.OrderStatus
import com.khabarexpress.buyer.domain.usecase.order.CancelOrderUseCase
import com.khabarexpress.buyer.domain.usecase.order.GetOrderByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    private val getOrderByIdUseCase: GetOrderByIdUseCase,
    private val cancelOrderUseCase: CancelOrderUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>("orderId") ?: ""

    private val _uiState = MutableStateFlow<OrderDetailsUiState>(OrderDetailsUiState.Loading)
    val uiState: StateFlow<OrderDetailsUiState> = _uiState.asStateFlow()

    private val _cancelOrderState = MutableStateFlow<CancelOrderState>(CancelOrderState.Idle)
    val cancelOrderState: StateFlow<CancelOrderState> = _cancelOrderState.asStateFlow()

    init {
        loadOrderDetails()
    }

    fun loadOrderDetails() {
        viewModelScope.launch {
            _uiState.value = OrderDetailsUiState.Loading
            getOrderByIdUseCase(orderId)
                .onSuccess { order ->
                    _uiState.value = OrderDetailsUiState.Success(order)
                }
                .onFailure { error ->
                    _uiState.value = OrderDetailsUiState.Error(
                        error.message ?: "Failed to load order details"
                    )
                }
        }
    }

    fun cancelOrder(reason: String) {
        viewModelScope.launch {
            _cancelOrderState.value = CancelOrderState.Loading
            cancelOrderUseCase(orderId, reason)
                .onSuccess {
                    _cancelOrderState.value = CancelOrderState.Success
                    loadOrderDetails()
                }
                .onFailure { error ->
                    _cancelOrderState.value = CancelOrderState.Error(
                        error.message ?: "Failed to cancel order"
                    )
                }
        }
    }

    fun canCancelOrder(order: Order): Boolean {
        return order.status in listOf(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED
        )
    }

    fun resetCancelState() {
        _cancelOrderState.value = CancelOrderState.Idle
    }

    fun retry() {
        loadOrderDetails()
    }
}

sealed class OrderDetailsUiState {
    object Loading : OrderDetailsUiState()
    data class Success(val order: Order) : OrderDetailsUiState()
    data class Error(val message: String) : OrderDetailsUiState()
}

sealed class CancelOrderState {
    object Idle : CancelOrderState()
    object Loading : CancelOrderState()
    object Success : CancelOrderState()
    data class Error(val message: String) : CancelOrderState()
}
