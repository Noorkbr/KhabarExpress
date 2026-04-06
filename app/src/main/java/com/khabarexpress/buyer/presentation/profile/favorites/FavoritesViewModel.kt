package com.khabarexpress.buyer.presentation.profile.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.usecase.restaurant.GetFavoriteRestaurantsUseCase
import com.khabarexpress.buyer.domain.usecase.restaurant.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoriteRestaurantsUseCase: GetFavoriteRestaurantsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading
            getFavoriteRestaurantsUseCase()
                .catch { error ->
                    _uiState.value = FavoritesUiState.Error(
                        error.message ?: "Failed to load favorites"
                    )
                }
                .collect { favorites ->
                    _uiState.value = if (favorites.isEmpty()) {
                        FavoritesUiState.Empty
                    } else {
                        FavoritesUiState.Success(favorites)
                    }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            getFavoriteRestaurantsUseCase()
                .catch { error ->
                    _uiState.value = FavoritesUiState.Error(
                        error.message ?: "Failed to refresh favorites"
                    )
                }
                .collect { favorites ->
                    _isRefreshing.value = false
                    _uiState.value = if (favorites.isEmpty()) {
                        FavoritesUiState.Empty
                    } else {
                        FavoritesUiState.Success(favorites)
                    }
                }
        }
    }

    fun toggleFavorite(restaurantId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(restaurantId)
                .onFailure { error ->
                    _uiState.value = FavoritesUiState.Error(
                        error.message ?: "Failed to update favorite"
                    )
                }
        }
    }

    fun retry() {
        loadFavorites()
    }
}

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    object Empty : FavoritesUiState()
    data class Success(val favorites: List<Restaurant>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}
