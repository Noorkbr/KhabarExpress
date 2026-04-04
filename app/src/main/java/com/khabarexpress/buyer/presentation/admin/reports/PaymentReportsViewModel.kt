package com.khabarexpress.buyer.presentation.admin.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.data.remote.dto.ProfitByMethod
import com.khabarexpress.buyer.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentReportsUiState(
    val isLoading: Boolean = false,
    val adminProfitRate: Double = 5.0,
    val totalAmount: Double = 0.0,
    val totalAdminProfit: Double = 0.0,
    val totalRestaurantPayout: Double = 0.0,
    val totalTransactions: Int = 0,
    val byPaymentMethod: List<ProfitByMethod> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class PaymentReportsViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentReportsUiState())
    val uiState: StateFlow<PaymentReportsUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            adminRepository.getProfitAnalytics()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            adminProfitRate = data.adminProfitRate,
                            totalAmount = data.summary.totalAmount,
                            totalAdminProfit = data.summary.totalAdminProfit,
                            totalRestaurantPayout = data.summary.totalRestaurantPayout,
                            totalTransactions = data.summary.totalTransactions,
                            byPaymentMethod = data.byPaymentMethod
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }
}
