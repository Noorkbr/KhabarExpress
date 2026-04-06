package com.khabarexpress.seller.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.seller.domain.repository.AuthRepository
import com.khabarexpress.seller.domain.usecase.auth.SellerLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LoginEvent {
    object LoginSuccess : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val sellerLoginUseCase: SellerLoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events

    fun login(phone: String, password: String) {
        if (phone.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Phone and password are required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = sellerLoginUseCase(phone, password)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(LoginEvent.LoginSuccess)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Login failed"
                    )
                    _events.emit(LoginEvent.ShowError(e.message ?: "Login failed"))
                }
            )
        }
    }

    suspend fun isAuthenticated(): Boolean {
        return authRepository.isAuthenticated()
    }
}
