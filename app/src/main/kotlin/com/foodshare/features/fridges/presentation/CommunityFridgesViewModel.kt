package com.foodshare.features.fridges.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.features.fridges.domain.model.CommunityFridge
import com.foodshare.features.fridges.domain.model.FridgeStatus
import com.foodshare.features.fridges.domain.repository.FridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode {
    MAP, LIST
}

enum class FridgeFilter {
    ALL, ACTIVE, LOW_STOCK
}

data class CommunityFridgesUiState(
    val fridges: List<CommunityFridge> = emptyList(),
    val filteredFridges: List<CommunityFridge> = emptyList(),
    val selectedFilter: FridgeFilter = FridgeFilter.ALL,
    val viewMode: ViewMode = ViewMode.LIST,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userLatitude: Double? = null,
    val userLongitude: Double? = null
)

@HiltViewModel
class CommunityFridgesViewModel @Inject constructor(
    private val fridgeRepository: FridgeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommunityFridgesUiState())
    val uiState: StateFlow<CommunityFridgesUiState> = _uiState.asStateFlow()

    init {
        // Default location (can be updated by LocationService)
        loadFridges(37.7749, -122.4194, 10.0)
    }

    fun loadFridges(latitude: Double, longitude: Double, radiusKm: Double = 10.0) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, userLatitude = latitude, userLongitude = longitude) }

            fridgeRepository.getNearbyFridges(latitude, longitude, radiusKm)
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { fridges ->
                    _uiState.update {
                        it.copy(
                            fridges = fridges,
                            filteredFridges = applyFilter(fridges, it.selectedFilter),
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    fun filterByStatus(filter: FridgeFilter) {
        _uiState.update {
            it.copy(
                selectedFilter = filter,
                filteredFridges = applyFilter(it.fridges, filter)
            )
        }
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(
                viewMode = if (it.viewMode == ViewMode.MAP) ViewMode.LIST else ViewMode.MAP
            )
        }
    }

    private fun applyFilter(fridges: List<CommunityFridge>, filter: FridgeFilter): List<CommunityFridge> {
        return when (filter) {
            FridgeFilter.ALL -> fridges
            FridgeFilter.ACTIVE -> fridges.filter { it.status == FridgeStatus.ACTIVE }
            FridgeFilter.LOW_STOCK -> fridges.filter {
                it.stockLevel == com.foodshare.features.fridges.domain.model.StockLevel.LOW ||
                it.stockLevel == com.foodshare.features.fridges.domain.model.StockLevel.EMPTY
            }
        }
    }
}
