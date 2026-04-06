package com.khabarexpress.buyer.presentation.order.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.OrderStatus
import com.khabarexpress.buyer.domain.usecase.order.GetUserOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val getUserOrdersUseCase: GetUserOrdersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            _uiState.value = OrderHistoryUiState.Loading
            getUserOrdersUseCase()
                .catch { error ->
                    _uiState.value = OrderHistoryUiState.Error(
                        error.message ?: "Failed to load orders"
                    )
                }
                .collect { orders ->
                    if (orders.isEmpty()) {
                        _uiState.value = OrderHistoryUiState.Empty
                    } else {
                        val activeOrders = orders.filter { isActiveOrder(it.status) }
                        val pastOrders = orders.filter { !isActiveOrder(it.status) }
                        _uiState.value = OrderHistoryUiState.Success(
                            activeOrders = activeOrders.sortedByDescending { it.createdAt },
                            pastOrders = pastOrders.sortedByDescending { it.createdAt }
                        )
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getUserOrdersUseCase()
                .catch { error ->
                    _uiState.value = OrderHistoryUiState.Error(
                        error.message ?: "Failed to refresh orders"
                    )
                }
                .collect { orders ->
                    _isRefreshing.value = false
                    if (orders.isEmpty()) {
                        _uiState.value = OrderHistoryUiState.Empty
                    } else {
                        val activeOrders = orders.filter { isActiveOrder(it.status) }
                        val pastOrders = orders.filter { !isActiveOrder(it.status) }
                        _uiState.value = OrderHistoryUiState.Success(
                            activeOrders = activeOrders.sortedByDescending { it.createdAt },
                            pastOrders = pastOrders.sortedByDescending { it.createdAt }
                        )
                    }
                }
        }
    }

    private fun isActiveOrder(status: OrderStatus): Boolean {
        return status in listOf(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING,
            OrderStatus.READY_FOR_PICKUP,
            OrderStatus.PICKED_UP,
            OrderStatus.ON_THE_WAY
        )
    }

    fun retry() {
        loadOrders()
    }
}

sealed class OrderHistoryUiState {
    object Loading : OrderHistoryUiState()
    object Empty : OrderHistoryUiState()
    data class Success(
        val activeOrders: List<Order>,
        val pastOrders: List<Order>
    ) : OrderHistoryUiState()
    data class Error(val message: String) : OrderHistoryUiState()
}
