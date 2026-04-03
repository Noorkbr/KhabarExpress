package com.khabarexpress.buyer.presentation.restaurant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.CartItem
import com.khabarexpress.buyer.domain.model.MenuItem
import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.CartRepository
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Restaurant Details screen
 * Manages restaurant details, menu, and cart operations
 */
@HiltViewModel
class RestaurantViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RestaurantDetailUiState>(RestaurantDetailUiState.Loading)
    val uiState: StateFlow<RestaurantDetailUiState> = _uiState.asStateFlow()

    private val _addToCartState = MutableStateFlow<AddToCartState>(AddToCartState.Idle)
    val addToCartState: StateFlow<AddToCartState> = _addToCartState.asStateFlow()

    /**
     * Load restaurant details with menu
     */
    fun loadRestaurantDetails(restaurantId: String) {
        viewModelScope.launch {
            _uiState.value = RestaurantDetailUiState.Loading
            restaurantRepository.getRestaurantById(restaurantId)
                .onSuccess { restaurant ->
                    _uiState.value = RestaurantDetailUiState.Success(
                        restaurant = restaurant,
                        selectedCategory = restaurant.categories.firstOrNull()?.id
                    )
                }
                .onFailure { error ->
                    _uiState.value = RestaurantDetailUiState.Error(
                        error.message ?: "Failed to load restaurant details"
                    )
                }
        }
    }

    /**
     * Select a menu category
     */
    fun selectCategory(categoryId: String) {
        val currentState = _uiState.value
        if (currentState is RestaurantDetailUiState.Success) {
            _uiState.value = currentState.copy(selectedCategory = categoryId)
        }
    }

    /**
     * Add item to cart
     */
    fun addToCart(cartItem: CartItem) {
        viewModelScope.launch {
            _addToCartState.value = AddToCartState.Loading
            cartRepository.addItem(cartItem)
                .onSuccess {
                    _addToCartState.value = AddToCartState.Success
                }
                .onFailure { error ->
                    _addToCartState.value = AddToCartState.Error(
                        error.message ?: "Failed to add item to cart"
                    )
                }
        }
    }

    /**
     * Check if restaurant is favorite
     */
    fun checkFavoriteStatus(restaurantId: String) {
        viewModelScope.launch {
            val isFavorite = restaurantRepository.isFavorite(restaurantId)
            val currentState = _uiState.value
            if (currentState is RestaurantDetailUiState.Success) {
                _uiState.value = currentState.copy(isFavorite = isFavorite)
            }
        }
    }

    /**
     * Toggle favorite status
     */
    fun toggleFavorite(restaurantId: String) {
        viewModelScope.launch {
            restaurantRepository.toggleFavorite(restaurantId)
                .onSuccess { isFavorite ->
                    val currentState = _uiState.value
                    if (currentState is RestaurantDetailUiState.Success) {
                        _uiState.value = currentState.copy(isFavorite = isFavorite)
                    }
                }
                .onFailure { error ->
                    val currentState = _uiState.value
                    if (currentState is RestaurantDetailUiState.Success) {
                        _uiState.value = RestaurantDetailUiState.Error(
                            error.message ?: "Failed to update favorite"
                        )
                    }
                }
        }
    }

    /**
     * Reset add to cart state
     */
    fun resetAddToCartState() {
        _addToCartState.value = AddToCartState.Idle
    }

    /**
     * Retry loading restaurant details
     */
    fun retry(restaurantId: String) {
        loadRestaurantDetails(restaurantId)
    }
}

/**
 * UI state for Restaurant Detail screen
 */
sealed class RestaurantDetailUiState {
    object Loading : RestaurantDetailUiState()
    data class Success(
        val restaurant: Restaurant,
        val selectedCategory: String? = null,
        val isFavorite: Boolean = false
    ) : RestaurantDetailUiState()
    data class Error(val message: String) : RestaurantDetailUiState()
}

/**
 * State for add to cart operation
 */
sealed class AddToCartState {
    object Idle : AddToCartState()
    object Loading : AddToCartState()
    object Success : AddToCartState()
    data class Error(val message: String) : AddToCartState()
}
