package com.khabarexpress.seller.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.repository.OrderRepository
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
    private val orderRepository: OrderRepository
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
            orderRepository.getOrders(status = status).fold(
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
            orderRepository.acceptOrder(orderId).fold(
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
            orderRepository.rejectOrder(orderId, reason).fold(
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
            orderRepository.updateOrderStatus(orderId, status).fold(
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
