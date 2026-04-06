package com.khabarexpress.buyer.presentation.profile.addresses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Address
import com.khabarexpress.buyer.domain.usecase.profile.DeleteAddressUseCase
import com.khabarexpress.buyer.domain.usecase.profile.GetAddressesUseCase
import com.khabarexpress.buyer.domain.usecase.profile.SetDefaultAddressUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val getAddressesUseCase: GetAddressesUseCase,
    private val deleteAddressUseCase: DeleteAddressUseCase,
    private val setDefaultAddressUseCase: SetDefaultAddressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddressUiState>(AddressUiState.Loading)
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    private val _addressActionState = MutableStateFlow<AddressActionState>(AddressActionState.Idle)
    val addressActionState: StateFlow<AddressActionState> = _addressActionState.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        viewModelScope.launch {
            _uiState.value = AddressUiState.Loading
            getAddressesUseCase()
                .catch { error ->
                    _uiState.value = AddressUiState.Error(
                        error.message ?: "Failed to load addresses"
                    )
                }
                .collect { addresses ->
                    _uiState.value = if (addresses.isEmpty()) {
                        AddressUiState.Empty
                    } else {
                        AddressUiState.Success(addresses)
                    }
                }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            _addressActionState.value = AddressActionState.Loading
            deleteAddressUseCase(addressId)
                .onSuccess {
                    _addressActionState.value = AddressActionState.Success("Address deleted")
                }
                .onFailure { error ->
                    _addressActionState.value = AddressActionState.Error(
                        error.message ?: "Failed to delete address"
                    )
                }
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            _addressActionState.value = AddressActionState.Loading
            setDefaultAddressUseCase(addressId)
                .onSuccess {
                    _addressActionState.value = AddressActionState.Success("Default address updated")
                }
                .onFailure { error ->
                    _addressActionState.value = AddressActionState.Error(
                        error.message ?: "Failed to set default address"
                    )
                }
        }
    }

    fun resetActionState() {
        _addressActionState.value = AddressActionState.Idle
    }

    fun retry() {
        loadAddresses()
    }
}

sealed class AddressUiState {
    object Loading : AddressUiState()
    object Empty : AddressUiState()
    data class Success(val addresses: List<Address>) : AddressUiState()
    data class Error(val message: String) : AddressUiState()
}

sealed class AddressActionState {
    object Idle : AddressActionState()
    object Loading : AddressActionState()
    data class Success(val message: String) : AddressActionState()
    data class Error(val message: String) : AddressActionState()
}
