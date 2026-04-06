package com.khabarexpress.buyer.presentation.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.repository.AdminRepository
import com.khabarexpress.buyer.domain.usecase.admin.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val todayOrders: Int = 0,
    val todayRevenue: Double = 0.0,
    val totalUsers: Int = 0,
    val totalRestaurants: Int = 0,
    val totalOrders: Int = 0,
    val activeOrders: Int = 0,
    val pendingRestaurants: Int = 0,
    val totalPayments: Double = 0.0,
    val totalAdminProfit: Double = 0.0,
    val totalRestaurantPayout: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Fetch dashboard stats
            val dashResult = getDashboardStatsUseCase()
            dashResult.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        todayOrders = data.today.orders,
                        todayRevenue = data.today.revenue,
                        totalUsers = data.totals.users,
                        totalRestaurants = data.totals.restaurants,
                        totalOrders = data.totals.orders,
                        activeOrders = data.active.orders,
                        pendingRestaurants = data.pending.restaurants
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }

            // Fetch profit analytics
            val profitResult = adminRepository.getProfitAnalytics()
            profitResult.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        totalPayments = data.summary.totalAmount,
                        totalAdminProfit = data.summary.totalAdminProfit,
                        totalRestaurantPayout = data.summary.totalRestaurantPayout
                    )
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
