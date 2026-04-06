package com.khabarexpress.seller.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.seller.domain.model.Analytics
import com.khabarexpress.seller.domain.model.Order
import com.khabarexpress.seller.domain.repository.AuthRepository
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import com.khabarexpress.seller.domain.usecase.auth.SellerLogoutUseCase
import com.khabarexpress.seller.domain.usecase.dashboard.GetDashboardUseCase
import com.khabarexpress.seller.domain.usecase.orders.AcceptOrderUseCase
import com.khabarexpress.seller.domain.usecase.orders.RejectOrderUseCase
import com.khabarexpress.seller.domain.usecase.orders.UpdateOrderStatusUseCase
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
    private val getDashboardUseCase: GetDashboardUseCase,
    private val acceptOrderUseCase: AcceptOrderUseCase,
    private val rejectOrderUseCase: RejectOrderUseCase,
    private val updateOrderStatusUseCase: UpdateOrderStatusUseCase,
    private val sellerLogoutUseCase: SellerLogoutUseCase,
    private val restaurantRepository: RestaurantRepository
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

            getDashboardUseCase().fold(
                onSuccess = { data ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        analytics = data.analytics,
                        pendingOrders = data.pendingOrders,
                        activeOrders = data.activeOrders,
                        isOpen = data.isOpen
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load dashboard"
                    )
                }
            )
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
            acceptOrderUseCase(orderId).fold(
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
            rejectOrderUseCase(orderId, reason).fold(
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
            updateOrderStatusUseCase(orderId, status).fold(
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
            sellerLogoutUseCase()
            _events.emit(DashboardEvent.NavigateToLogin)
        }
    }
}
