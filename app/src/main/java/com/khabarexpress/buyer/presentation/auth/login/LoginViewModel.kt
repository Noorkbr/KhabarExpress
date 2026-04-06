package com.khabarexpress.buyer.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.usecase.auth.CheckAuthStatusUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LoginWithCredentialsUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LoginWithOtpUseCase
import com.khabarexpress.buyer.domain.usecase.auth.LoginWithPhoneUseCase
import com.khabarexpress.buyer.domain.usecase.auth.SendOtpUseCase
import com.khabarexpress.buyer.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Login screen
 * Handles user authentication via phone number (buyer) or phone+password (staff)
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginWithPhoneUseCase: LoginWithPhoneUseCase,
    private val loginWithCredentialsUseCase: LoginWithCredentialsUseCase,
    private val loginWithOtpUseCase: LoginWithOtpUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    /**
     * Seamless phone-only login for end users (no verification required).
     * Bangladeshi phone numbers only.
     */
    fun loginWithPhoneOnly(phone: String) {
        if (phone.isBlank()) {
            _uiState.value = LoginUiState.Error("Phone number cannot be empty")
            return
        }

        if (!isValidPhone(phone)) {
            _uiState.value = LoginUiState.Error("Please enter a valid Bangladeshi phone number")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginWithPhoneUseCase(phone)
                .onSuccess { user ->
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Login failed. Please try again."
                    )
                }
        }
    }

    /**
     * Login with email and password (kept for staff/restaurant access)
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Email and password cannot be empty")
            return
        }

        if (!isValidEmail(email)) {
            _uiState.value = LoginUiState.Error("Invalid email format")
            return
        }

        if (password.length < Constants.MIN_PASSWORD_LENGTH) {
            _uiState.value = LoginUiState.Error("Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginWithCredentialsUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Login failed. Please try again."
                    )
                }
        }
    }

    /**
     * Login with phone number and OTP
     */
    fun loginWithPhone(phone: String, otp: String) {
        if (phone.isBlank() || otp.isBlank()) {
            _uiState.value = LoginUiState.Error("Phone number and OTP cannot be empty")
            return
        }

        if (!isValidPhone(phone)) {
            _uiState.value = LoginUiState.Error("Invalid phone number format")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            loginWithOtpUseCase(phone, otp)
                .onSuccess { user ->
                    _uiState.value = LoginUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Phone login failed. Please try again."
                    )
                }
        }
    }

    /**
     * Send OTP to phone number
     */
    fun sendOtp(phone: String) {
        if (phone.isBlank()) {
            _uiState.value = LoginUiState.Error("Phone number cannot be empty")
            return
        }

        if (!isValidPhone(phone)) {
            _uiState.value = LoginUiState.Error("Invalid phone number format")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            sendOtpUseCase(phone)
                .onSuccess {
                    _uiState.value = LoginUiState.OtpSent
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Failed to send OTP. Please try again."
                    )
                }
        }
    }

    /**
     * Check if user is already authenticated
     */
    fun checkAuthStatus() {
        viewModelScope.launch {
            if (checkAuthStatusUseCase.isAuthenticated()) {
                checkAuthStatusUseCase.getCurrentUser().collect { user ->
                    user?.let {
                        _uiState.value = LoginUiState.Success(it)
                    }
                }
            }
        }
    }

    /**
     * Reset state to idle
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Constants.BD_PHONE_PATTERN.toRegex())
    }
}

/**
 * UI state for Login screen
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object OtpSent : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
