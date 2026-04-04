package com.khabarexpress.seller.presentation.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.seller.domain.model.Analytics
import com.khabarexpress.seller.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EarningsUiState(
    val isLoading: Boolean = false,
    val todayAnalytics: Analytics = Analytics(),
    val weekAnalytics: Analytics = Analytics(),
    val monthAnalytics: Analytics = Analytics(),
    val selectedPeriod: String = "today",
    val error: String? = null
)

@HiltViewModel
class EarningsViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState

    init {
        loadAllAnalytics()
    }

    fun loadAllAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            restaurantRepository.getAnalytics("today").fold(
                onSuccess = { _uiState.value = _uiState.value.copy(todayAnalytics = it) },
                onFailure = { }
            )

            restaurantRepository.getAnalytics("week").fold(
                onSuccess = { _uiState.value = _uiState.value.copy(weekAnalytics = it) },
                onFailure = { }
            )

            restaurantRepository.getAnalytics("month").fold(
                onSuccess = { _uiState.value = _uiState.value.copy(monthAnalytics = it) },
                onFailure = { }
            )

            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun selectPeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }
}
