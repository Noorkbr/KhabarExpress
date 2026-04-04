package com.khabarexpress.seller.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.seller.domain.model.Analytics
import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.repository.AuthRepository
import com.khabarexpress.seller.domain.repository.OrderRepository
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val analytics: Analytics = Analytics(),
    val pendingOrders: List<Order> = emptyList(),
    val activeOrders: List<Order> = emptyList(),
    val isOpen: Boolean = false,
    val restaurantName: String = "",
    val error: String? = null
)

sealed class DashboardEvent {
    data class ShowError(val message: String) : DashboardEvent()
    data class ShowSuccess(val message: String) : DashboardEvent()
    object NavigateToLogin : DashboardEvent()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val restaurantRepository: RestaurantRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    private val _events = MutableSharedFlow<DashboardEvent>()
    val events: SharedFlow<DashboardEvent> = _events

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Load analytics
            restaurantRepository.getAnalytics("today").fold(
                onSuccess = { analytics ->
                    _uiState.value = _uiState.value.copy(analytics = analytics)
                },
                onFailure = { }
            )

            // Load pending orders
            orderRepository.getOrders(status = "pending").fold(
                onSuccess = { orders ->
                    _uiState.value = _uiState.value.copy(pendingOrders = orders)
                },
                onFailure = { }
            )

            // Load active orders (confirmed + preparing)
            orderRepository.getOrders(status = "confirmed").fold(
                onSuccess = { confirmedOrders ->
                    orderRepository.getOrders(status = "preparing").fold(
                        onSuccess = { preparingOrders ->
                            _uiState.value = _uiState.value.copy(
                                activeOrders = confirmedOrders + preparingOrders
                            )
                        },
                        onFailure = { }
                    )
                },
                onFailure = { }
            )

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            loadDashboard()
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.acceptOrder(orderId).fold(
                onSuccess = {
                    _events.emit(DashboardEvent.ShowSuccess("Order accepted"))
                    loadDashboard()
                },
                onFailure = { e ->
                    _events.emit(DashboardEvent.ShowError(e.message ?: "Failed to accept order"))
                }
            )
        }
    }

    fun rejectOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            orderRepository.rejectOrder(orderId, reason).fold(
                onSuccess = {
                    _events.emit(DashboardEvent.ShowSuccess("Order rejected"))
                    loadDashboard()
                },
                onFailure = { e ->
                    _events.emit(DashboardEvent.ShowError(e.message ?: "Failed to reject order"))
                }
            )
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status).fold(
                onSuccess = {
                    _events.emit(DashboardEvent.ShowSuccess("Order updated to $status"))
                    loadDashboard()
                },
                onFailure = { e ->
                    _events.emit(DashboardEvent.ShowError(e.message ?: "Failed to update order"))
                }
            )
        }
    }

    fun toggleOpenStatus() {
        viewModelScope.launch {
            restaurantRepository.toggleOpenStatus().fold(
                onSuccess = { isOpen ->
                    _uiState.value = _uiState.value.copy(isOpen = isOpen)
                    _events.emit(DashboardEvent.ShowSuccess(
                        if (isOpen) "Restaurant is now open" else "Restaurant is now closed"
                    ))
                },
                onFailure = { e ->
                    _events.emit(DashboardEvent.ShowError(e.message ?: "Failed to toggle status"))
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _events.emit(DashboardEvent.NavigateToLogin)
        }
    }
}
