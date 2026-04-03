package com.khabarexpress.buyer.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.model.Order
import com.khabarexpress.buyer.domain.model.User
import com.khabarexpress.buyer.domain.repository.AuthRepository
import com.khabarexpress.buyer.domain.repository.OrderRepository
import com.khabarexpress.buyer.domain.repository.UserRepository
import com.khabarexpress.buyer.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Profile screen
 * Manages user profile, addresses, orders, and logout
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState.asStateFlow()

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Load user profile with addresses and recent orders
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            
            try {
                // Collect user profile
                userRepository.getUserProfile()
                    .catch { error ->
                        _uiState.value = ProfileUiState.Error(
                            error.message ?: "Failed to load profile"
                        )
                    }
                    .collect { user ->
                        if (user == null) {
                            _uiState.value = ProfileUiState.Error("User not found")
                        } else {
                            // Collect addresses
                            userRepository.getUserAddresses()
                                .catch { error ->
                                    // Show profile even if addresses fail
                                    _uiState.value = ProfileUiState.Success(
                                        user = user,
                                        addresses = emptyList(),
                                        recentOrders = emptyList()
                                    )
                                }
                                .collect { addresses ->
                                    // Collect recent orders
                                    orderRepository.getUserOrders()
                                        .catch { error ->
                                            // Show profile even if orders fail
                                            _uiState.value = ProfileUiState.Success(
                                                user = user,
                                                addresses = addresses,
                                                recentOrders = emptyList()
                                            )
                                        }
                                        .collect { orders ->
                                            _uiState.value = ProfileUiState.Success(
                                                user = user,
                                                addresses = addresses,
                                                recentOrders = orders.take(5) // Show only recent 5 orders
                                            )
                                        }
                                }
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    e.message ?: "Failed to load profile"
                )
            }
        }
    }

    /**
     * Update user profile
     */
    fun updateProfile(name: String?, phone: String?, profileImageUrl: String?) {
        // Validate input
        if (name != null && name.isBlank()) {
            _updateProfileState.value = UpdateProfileState.Error("Name cannot be empty")
            return
        }

        if (phone != null && !isValidPhone(phone)) {
            _updateProfileState.value = UpdateProfileState.Error("Invalid phone number format")
            return
        }

        viewModelScope.launch {
            _updateProfileState.value = UpdateProfileState.Loading
            userRepository.updateProfile(name, phone, profileImageUrl)
                .onSuccess { updatedUser ->
                    _updateProfileState.value = UpdateProfileState.Success
                    // Reload profile to show updated data
                    loadProfile()
                }
                .onFailure { error ->
                    _updateProfileState.value = UpdateProfileState.Error(
                        error.message ?: "Failed to update profile"
                    )
                }
        }
    }

    /**
     * Upload profile image
     */
    fun uploadProfileImage(imagePath: String) {
        viewModelScope.launch {
            _updateProfileState.value = UpdateProfileState.Loading
            userRepository.uploadProfileImage(imagePath)
                .onSuccess { imageUrl ->
                    // Update profile with new image URL
                    updateProfile(null, null, imageUrl)
                }
                .onFailure { error ->
                    _updateProfileState.value = UpdateProfileState.Error(
                        error.message ?: "Failed to upload image"
                    )
                }
        }
    }

    /**
     * Load user addresses
     */
    fun loadAddresses() {
        viewModelScope.launch {
            userRepository.getUserAddresses()
                .catch { error ->
                    _uiState.value = ProfileUiState.Error(
                        error.message ?: "Failed to load addresses"
                    )
                }
                .collect { addresses ->
                    val currentState = _uiState.value
                    if (currentState is ProfileUiState.Success) {
                        _uiState.value = currentState.copy(addresses = addresses)
                    }
                }
        }
    }

    /**
     * Add new address
     */
    fun addAddress(address: Address) {
        viewModelScope.launch {
            userRepository.addAddress(address)
                .onSuccess {
                    loadAddresses()
                }
                .onFailure { error ->
                    _updateProfileState.value = UpdateProfileState.Error(
                        error.message ?: "Failed to add address"
                    )
                }
        }
    }

    /**
     * Delete address
     */
    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            userRepository.deleteAddress(addressId)
                .onSuccess {
                    loadAddresses()
                }
                .onFailure { error ->
                    _updateProfileState.value = UpdateProfileState.Error(
                        error.message ?: "Failed to delete address"
                    )
                }
        }
    }

    /**
     * Set default address
     */
    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            userRepository.setDefaultAddress(addressId)
                .onSuccess {
                    loadAddresses()
                }
                .onFailure { error ->
                    _updateProfileState.value = UpdateProfileState.Error(
                        error.message ?: "Failed to set default address"
                    )
                }
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading
            authRepository.logout()
                .onSuccess {
                    _logoutState.value = LogoutState.Success
                }
                .onFailure { error ->
                    _logoutState.value = LogoutState.Error(
                        error.message ?: "Failed to logout"
                    )
                }
        }
    }

    /**
     * Reset update profile state
     */
    fun resetUpdateProfileState() {
        _updateProfileState.value = UpdateProfileState.Idle
    }

    /**
     * Reset logout state
     */
    fun resetLogoutState() {
        _logoutState.value = LogoutState.Idle
    }

    /**
     * Retry loading profile
     */
    fun retry() {
        loadProfile()
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Constants.BD_PHONE_PATTERN.toRegex())
    }
}

/**
 * UI state for Profile screen
 */
sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val user: User,
        val addresses: List<Address>,
        val recentOrders: List<Order>
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

/**
 * State for update profile operation
 */
sealed class UpdateProfileState {
    object Idle : UpdateProfileState()
    object Loading : UpdateProfileState()
    object Success : UpdateProfileState()
    data class Error(val message: String) : UpdateProfileState()
}

/**
 * State for logout operation
 */
sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}
