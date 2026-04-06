package com.khabarexpress.seller.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.usecase.orders.AcceptOrderUseCase
import com.khabarexpress.seller.domain.usecase.orders.GetPendingOrdersUseCase
import com.khabarexpress.seller.domain.usecase.orders.RejectOrderUseCase
import com.khabarexpress.seller.domain.usecase.orders.UpdateOrderStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val selectedFilter: String = "all",
    val error: String? = null
)

sealed class OrdersEvent {
    data class ShowError(val message: String) : OrdersEvent()
    data class ShowSuccess(val message: String) : OrdersEvent()
}

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val getPendingOrdersUseCase: GetPendingOrdersUseCase,
    private val acceptOrderUseCase: AcceptOrderUseCase,
    private val rejectOrderUseCase: RejectOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState

    private val _events = MutableSharedFlow<OrdersEvent>()
    val events: SharedFlow<OrdersEvent> = _events

    init {
        loadOrders()
    }

    fun loadOrders(filter: String = _uiState.value.selectedFilter) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedFilter = filter, error = null)
            val status = if (filter == "all") null else filter
            getPendingOrdersUseCase(status).fold(
                onSuccess = { orders ->
                    _uiState.value = _uiState.value.copy(isLoading = false, orders = orders)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load orders"
                    )
                }
            )
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            acceptOrderUseCase(orderId).fold(
                onSuccess = {
                    _events.emit(OrdersEvent.ShowSuccess("Order accepted"))
                    loadOrders()
                },
                onFailure = { e ->
                    _events.emit(OrdersEvent.ShowError(e.message ?: "Failed"))
                }
            )
        }
    }

    fun rejectOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            rejectOrderUseCase(orderId, reason).fold(
                onSuccess = {
                    _events.emit(OrdersEvent.ShowSuccess("Order rejected"))
                    loadOrders()
                },
                onFailure = { e ->
                    _events.emit(OrdersEvent.ShowError(e.message ?: "Failed"))
                }
            )
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            updateOrderStatusUseCase(orderId, status).fold(
                onSuccess = {
                    _events.emit(OrdersEvent.ShowSuccess("Status updated"))
                    loadOrders()
                },
                onFailure = { e ->
                    _events.emit(OrdersEvent.ShowError(e.message ?: "Failed"))
                }
            )
        }
    }
}
