package com.khabarexpress.buyer.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khabarexpress.buyer.domain.model.MenuItem
import com.khabarexpress.buyer.domain.model.Restaurant
import com.khabarexpress.buyer.domain.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val restaurantRepository: RestaurantRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Initial)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _filters = MutableStateFlow(SearchFilters())
    val filters: StateFlow<SearchFilters> = _filters.asStateFlow()

    init {
        loadRecentSearches()
        setupSearchDebounce()
    }

    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .filter { it.isNotBlank() }
                .collect { query ->
                    performSearch(query)
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Initial
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            restaurantRepository.searchRestaurants(query)
                .catch { error ->
                    _uiState.value = SearchUiState.Error(
                        error.message ?: "Failed to search"
                    )
                }
                .collect { restaurants ->
                    val filteredRestaurants = applyFilters(restaurants)
                    _uiState.value = if (filteredRestaurants.isEmpty()) {
                        SearchUiState.NoResults
                    } else {
                        SearchUiState.Success(
                            restaurants = filteredRestaurants,
                            menuItems = extractMenuItems(filteredRestaurants, query)
                        )
                    }
                }
        }
    }

    private fun applyFilters(restaurants: List<Restaurant>): List<Restaurant> {
        val currentFilters = _filters.value
        return restaurants.filter { restaurant ->
            val matchesCuisine = currentFilters.cuisines.isEmpty() || 
                restaurant.cuisine.any { it in currentFilters.cuisines }
            val matchesRating = restaurant.rating >= currentFilters.minRating
            val matchesDeliveryTime = restaurant.deliveryTime <= currentFilters.maxDeliveryTime
            val matchesPrice = restaurant.minOrderAmount in currentFilters.priceRange
            
            matchesCuisine && matchesRating && matchesDeliveryTime && matchesPrice
        }.let { filtered ->
            when (currentFilters.sortBy) {
                SortOption.RATING -> filtered.sortedByDescending { it.rating }
                SortOption.DISTANCE -> filtered.sortedBy { it.distance }
                SortOption.DELIVERY_TIME -> filtered.sortedBy { it.deliveryTime }
            }
        }
    }

    private fun extractMenuItems(restaurants: List<Restaurant>, query: String): List<MenuItem> {
        val allItems = restaurants.flatMap { restaurant ->
            restaurant.categories.flatMap { it.items }
        }
        return allItems.filter { item ->
            item.name.contains(query, ignoreCase = true) ||
            item.description.contains(query, ignoreCase = true)
        }.take(10)
    }

    fun executeSearch() {
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            addToRecentSearches(query)
            performSearch(query)
        }
    }

    fun selectRecentSearch(query: String) {
        _searchQuery.value = query
        performSearch(query)
    }

    fun removeRecentSearch(query: String) {
        _recentSearches.value = _recentSearches.value.filter { it != query }
        saveRecentSearches()
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
        saveRecentSearches()
    }

    private fun addToRecentSearches(query: String) {
        val updated = (_recentSearches.value.filter { it != query } + query)
            .takeLast(10)
        _recentSearches.value = updated
        saveRecentSearches()
    }

    private fun loadRecentSearches() {
        // TODO: Load from local storage/preferences
        _recentSearches.value = listOf("Biryani", "Pizza", "Burger")
    }

    private fun saveRecentSearches() {
        // TODO: Save to local storage/preferences
    }

    fun updateFilters(filters: SearchFilters) {
        _filters.value = filters
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            performSearch(query)
        }
    }

    fun clearFilters() {
        _filters.value = SearchFilters()
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            performSearch(query)
        }
    }

    fun retry() {
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            performSearch(query)
        }
    }
}

sealed class SearchUiState {
    object Initial : SearchUiState()
    object Loading : SearchUiState()
    data class Success(
        val restaurants: List<Restaurant>,
        val menuItems: List<MenuItem>
    ) : SearchUiState()
    object NoResults : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

data class SearchFilters(
    val cuisines: List<String> = emptyList(),
    val minRating: Double = 0.0,
    val maxDeliveryTime: Int = 120,
    val priceRange: ClosedFloatingPointRange<Double> = 0.0..10000.0,
    val dietaryPreferences: List<DietaryPreference> = emptyList(),
    val sortBy: SortOption = SortOption.RATING
)

enum class DietaryPreference {
    VEGETARIAN, VEGAN, GLUTEN_FREE
}

enum class SortOption(val displayName: String) {
    RATING("Rating"),
    DISTANCE("Distance"),
    DELIVERY_TIME("Delivery Time")
}
