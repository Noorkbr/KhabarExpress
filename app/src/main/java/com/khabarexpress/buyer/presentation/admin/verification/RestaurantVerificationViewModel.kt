package com.khabarexpress.buyer.presentation.admin.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.data.remote.dto.PendingRestaurantDto
import com.khabarexpress.buyer.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RestaurantVerificationUiState(
    val isLoading: Boolean = false,
    val restaurants: List<PendingRestaurantDto> = emptyList(),
    val processingId: String? = null,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class RestaurantVerificationViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RestaurantVerificationUiState())
    val uiState: StateFlow<RestaurantVerificationUiState> = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            adminRepository.getPendingRestaurants()
                .onSuccess { restaurants ->
                    _uiState.update {
                        it.copy(isLoading = false, restaurants = restaurants)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }

    fun approveRestaurant(restaurantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingId = restaurantId) }
            adminRepository.approveRestaurant(restaurantId)
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(
                            processingId = null,
                            restaurants = state.restaurants.filter { it.id != restaurantId },
                            message = message
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(processingId = null, error = e.message)
                    }
                }
        }
    }

    fun rejectRestaurant(restaurantId: String, reason: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(processingId = restaurantId) }
            adminRepository.rejectRestaurant(restaurantId, reason)
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(
                            processingId = null,
                            restaurants = state.restaurants.filter { it.id != restaurantId },
                            message = message
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(processingId = null, error = e.message)
                    }
                }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
