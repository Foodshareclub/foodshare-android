package com.foodshare.features.fridges.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.features.fridges.domain.model.CommunityFridge
import com.foodshare.features.fridges.domain.model.FridgeReport
import com.foodshare.features.fridges.domain.model.StockLevel
import com.foodshare.features.fridges.domain.repository.FridgeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FridgeDetailUiState(
    val fridge: CommunityFridge? = null,
    val reports: List<FridgeReport> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val reportSuccess: Boolean = false,
    val isReporting: Boolean = false
)

@HiltViewModel
class CommunityFridgeDetailViewModel @Inject constructor(
    private val fridgeRepository: FridgeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val fridgeId: Int = checkNotNull(savedStateHandle.get<String>("fridgeId")?.toIntOrNull())

    private val _uiState = MutableStateFlow(FridgeDetailUiState())
    val uiState: StateFlow<FridgeDetailUiState> = _uiState.asStateFlow()

    init {
        loadFridge()
        loadReports()
    }

    fun loadFridge() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            fridgeRepository.getFridgeDetail(fridgeId)
                .onSuccess { fridge ->
                    _uiState.update { it.copy(fridge = fridge, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            fridgeRepository.getFridgeReports(fridgeId)
                .onSuccess { reports ->
                    _uiState.update { it.copy(reports = reports) }
                }
                .onFailure { e ->
                    // Silent failure for reports
                }
        }
    }

    fun reportStock(stockLevel: StockLevel, notes: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isReporting = true, reportSuccess = false) }

            fridgeRepository.reportStock(fridgeId, stockLevel, notes)
                .onSuccess {
                    _uiState.update { it.copy(isReporting = false, reportSuccess = true) }
                    loadFridge() // Reload to get updated stock level
                    loadReports() // Reload to show new report
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isReporting = false, error = e.message) }
                }
        }
    }

    fun clearReportSuccess() {
        _uiState.update { it.copy(reportSuccess = false) }
    }
}
