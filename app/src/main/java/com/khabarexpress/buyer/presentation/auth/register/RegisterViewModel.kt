package com.khabarexpress.buyer.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import com.khabarexpress.buyer.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Register screen
 * Handles user registration with validation
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Register new user
     */
    fun register(name: String, email: String, phone: String, password: String, confirmPassword: String) {
        // Validate input
        val validationError = validateInput(name, email, phone, password, confirmPassword)
        if (validationError != null) {
            _uiState.value = RegisterUiState.Error(validationError)
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            authRepository.register(name, email, phone, password)
                .onSuccess { user ->
                    _uiState.value = RegisterUiState.Success(user)
                }
                .onFailure { error ->
                    _uiState.value = RegisterUiState.Error(
                        error.message ?: "Registration failed. Please try again."
                    )
                }
        }
    }

    /**
     * Send OTP for phone verification during registration
     */
    fun sendOtp(phone: String) {
        if (phone.isBlank()) {
            _uiState.value = RegisterUiState.Error("Phone number cannot be empty")
            return
        }

        if (!isValidPhone(phone)) {
            _uiState.value = RegisterUiState.Error("Invalid phone number format")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            authRepository.sendOtp(phone)
                .onSuccess {
                    _uiState.value = RegisterUiState.OtpSent
                }
                .onFailure { error ->
                    _uiState.value = RegisterUiState.Error(
                        error.message ?: "Failed to send OTP. Please try again."
                    )
                }
        }
    }

    /**
     * Verify OTP
     */
    fun verifyOtp(phone: String, otp: String) {
        if (otp.isBlank()) {
            _uiState.value = RegisterUiState.Error("OTP cannot be empty")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            authRepository.verifyOtp(phone, otp)
                .onSuccess { isValid ->
                    if (isValid) {
                        _uiState.value = RegisterUiState.OtpVerified
                    } else {
                        _uiState.value = RegisterUiState.Error("Invalid OTP. Please try again.")
                    }
                }
                .onFailure { error ->
                    _uiState.value = RegisterUiState.Error(
                        error.message ?: "OTP verification failed. Please try again."
                    )
                }
        }
    }

    /**
     * Validate registration input
     */
    fun validateInput(
        name: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < Constants.MIN_NAME_LENGTH -> "Name must be at least ${Constants.MIN_NAME_LENGTH} characters"
            email.isBlank() -> "Email cannot be empty"
            !isValidEmail(email) -> "Invalid email format"
            phone.isBlank() -> "Phone number cannot be empty"
            !isValidPhone(phone) -> "Invalid phone number format (use 01XXXXXXXXX)"
            password.isBlank() -> "Password cannot be empty"
            password.length < Constants.MIN_PASSWORD_LENGTH -> "Password must be at least ${Constants.MIN_PASSWORD_LENGTH} characters"
            !isValidPassword(password) -> "Password must contain at least one letter and one number"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    /**
     * Reset state to idle
     */
    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Constants.BD_PHONE_PATTERN.toRegex())
    }

    private fun isValidPassword(password: String): Boolean {
        // Password must contain at least one letter and one number
        return password.any { it.isLetter() } && password.any { it.isDigit() }
    }
}

/**
 * UI state for Register screen
 */
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object OtpSent : RegisterUiState()
    object OtpVerified : RegisterUiState()
    data class Success(val user: User) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
