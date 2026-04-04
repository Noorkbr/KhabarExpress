package com.khabarexpress.seller.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.seller.domain.model.MenuItem
import com.khabarexpress.seller.domain.repository.MenuRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val isLoading: Boolean = false,
    val menuItems: List<MenuItem> = emptyList(),
    val error: String? = null
)

sealed class MenuEvent {
    data class ShowError(val message: String) : MenuEvent()
    data class ShowSuccess(val message: String) : MenuEvent()
}

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState

    private val _events = MutableSharedFlow<MenuEvent>()
    val events: SharedFlow<MenuEvent> = _events

    init {
        loadMenuItems()
    }

    fun loadMenuItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            menuRepository.getMenuItems().fold(
                onSuccess = { items ->
                    _uiState.value = _uiState.value.copy(isLoading = false, menuItems = items)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load menu"
                    )
                }
            )
        }
    }

    fun createMenuItem(
        name: String,
        description: String?,
        category: String,
        price: Double,
        isAvailable: Boolean = true,
        isHalal: Boolean = true,
        spiceLevel: String = "None",
        prepTime: Int = 15
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            menuRepository.createMenuItem(name, description, category, price, isAvailable, isHalal, spiceLevel, prepTime).fold(
                onSuccess = {
                    _events.emit(MenuEvent.ShowSuccess("Menu item created"))
                    loadMenuItems()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _events.emit(MenuEvent.ShowError(e.message ?: "Failed to create item"))
                }
            )
        }
    }

    fun updateMenuItem(
        id: String,
        name: String? = null,
        description: String? = null,
        category: String? = null,
        price: Double? = null,
        isAvailable: Boolean? = null
    ) {
        viewModelScope.launch {
            menuRepository.updateMenuItem(id, name, description, category, price, isAvailable).fold(
                onSuccess = {
                    _events.emit(MenuEvent.ShowSuccess("Menu item updated"))
                    loadMenuItems()
                },
                onFailure = { e ->
                    _events.emit(MenuEvent.ShowError(e.message ?: "Failed to update item"))
                }
            )
        }
    }

    fun toggleAvailability(id: String) {
        viewModelScope.launch {
            menuRepository.toggleAvailability(id).fold(
                onSuccess = {
                    _events.emit(MenuEvent.ShowSuccess("Availability updated"))
                    loadMenuItems()
                },
                onFailure = { e ->
                    _events.emit(MenuEvent.ShowError(e.message ?: "Failed to toggle availability"))
                }
            )
        }
    }

    fun deleteMenuItem(id: String) {
        viewModelScope.launch {
            menuRepository.deleteMenuItem(id).fold(
                onSuccess = {
                    _events.emit(MenuEvent.ShowSuccess("Menu item deleted"))
                    loadMenuItems()
                },
                onFailure = { e ->
                    _events.emit(MenuEvent.ShowError(e.message ?: "Failed to delete item"))
                }
            )
        }
    }
}
