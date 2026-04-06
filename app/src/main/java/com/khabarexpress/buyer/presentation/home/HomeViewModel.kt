package com.khabarexpress.buyer.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.usecase.restaurant.FilterByCategoryUseCase
import com.khabarexpress.buyer.domain.usecase.restaurant.GetNearbyRestaurantsUseCase
import com.khabarexpress.buyer.domain.usecase.restaurant.GetRestaurantsUseCase
import com.khabarexpress.buyer.domain.usecase.restaurant.SearchRestaurantsUseCase
import com.khabarexpress.buyer.domain.usecase.restaurant.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen
 * Manages restaurant list, search, filtering, and favorites
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    private val getNearbyRestaurantsUseCase: GetNearbyRestaurantsUseCase,
    private val filterByCategoryUseCase: FilterByCategoryUseCase,
    private val searchRestaurantsUseCase: SearchRestaurantsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadRestaurants()
    }

    /**
     * Load all restaurants
     */
    fun loadRestaurants() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            getRestaurantsUseCase()
                .catch { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Failed to load restaurants"
                    )
                }
                .collect { restaurants ->
                    if (restaurants.isEmpty()) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        val categories = extractCategories(restaurants)
                        _uiState.value = HomeUiState.Success(
                            restaurants = restaurants,
                            categories = categories,
                            filteredRestaurants = restaurants
                        )
                    }
                }
        }
    }

    /**
     * Search restaurants by query
     */
    fun searchRestaurants(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            loadRestaurants()
            return
        }

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            searchRestaurantsUseCase(query)
                .catch { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Search failed"
                    )
                }
                .collect { restaurants ->
                    if (restaurants.isEmpty()) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        val categories = extractCategories(restaurants)
                        _uiState.value = HomeUiState.Success(
                            restaurants = restaurants,
                            categories = categories,
                            filteredRestaurants = restaurants
                        )
                    }
                }
        }
    }

    /**
     * Filter restaurants by category
     */
    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
        
        if (category == null) {
            loadRestaurants()
            return
        }

        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            filterByCategoryUseCase(category)
                .catch { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Filter failed"
                    )
                }
                .collect { restaurants ->
                    val currentState = _uiState.value
                    if (currentState is HomeUiState.Success) {
                        _uiState.value = currentState.copy(
                            filteredRestaurants = restaurants
                        )
                    } else {
                        val categories = extractCategories(restaurants)
                        _uiState.value = HomeUiState.Success(
                            restaurants = restaurants,
                            categories = categories,
                            filteredRestaurants = restaurants
                        )
                    }
                }
        }
    }

    /**
     * Toggle favorite status for a restaurant
     */
    fun toggleFavorite(restaurantId: String) {
        viewModelScope.launch {
            toggleFavoriteUseCase(restaurantId)
                .onSuccess { isFavorite ->
                    // Update UI state with new favorite status
                    val currentState = _uiState.value
                    if (currentState is HomeUiState.Success) {
                        // Trigger reload to refresh favorite status
                        loadRestaurants()
                    }
                }
                .onFailure { error ->
                    // Show error but don't change main state
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Failed to update favorite"
                    )
                }
        }
    }

    /**
     * Load nearby restaurants based on user location
     */
    fun loadNearbyRestaurants(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            getNearbyRestaurantsUseCase(latitude, longitude)
                .onSuccess { restaurants ->
                    if (restaurants.isEmpty()) {
                        _uiState.value = HomeUiState.Empty
                    } else {
                        val categories = extractCategories(restaurants)
                        _uiState.value = HomeUiState.Success(
                            restaurants = restaurants,
                            categories = categories,
                            filteredRestaurants = restaurants
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Failed to load nearby restaurants"
                    )
                }
        }
    }

    /**
     * Retry loading restaurants
     */
    fun retry() {
        loadRestaurants()
    }

    /**
     * Extract unique categories from restaurants
     */
    private fun extractCategories(restaurants: List<Restaurant>): List<String> {
        return restaurants
            .flatMap { it.cuisine }
            .distinct()
            .sorted()
    }
}

/**
 * UI state for Home screen
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    object Empty : HomeUiState()
    data class Success(
        val restaurants: List<Restaurant>,
        val categories: List<String>,
        val filteredRestaurants: List<Restaurant>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
