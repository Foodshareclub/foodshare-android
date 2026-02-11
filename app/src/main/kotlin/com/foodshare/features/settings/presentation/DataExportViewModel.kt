package com.foodshare.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.core.security.ExportStatus
import com.foodshare.core.security.GDPRExportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Data Export screen
 */
@HiltViewModel
class DataExportViewModel @Inject constructor(
    private val gdprExportService: GDPRExportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataExportUiState())
    val uiState: StateFlow<DataExportUiState> = _uiState.asStateFlow()

    fun requestExport() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRequesting = true, error = null) }

            val result = gdprExportService.requestExport()

            result.onSuccess { exportId ->
                _uiState.update {
                    it.copy(
                        exportId = exportId,
                        status = "pending",
                        isRequesting = false
                    )
                }

                // Start polling for status
                pollExportStatus(exportId)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isRequesting = false,
                        error = error.message
                    )
                }
            }
        }
    }

    private fun pollExportStatus(exportId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true) }

            val result = gdprExportService.pollExportStatus(exportId)

            result.onSuccess { status ->
                _uiState.update {
                    it.copy(
                        status = status.status,
                        downloadUrl = status.downloadUrl,
                        expiresAt = status.expiresAt,
                        isPolling = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isPolling = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun checkStatus(exportId: String) {
        viewModelScope.launch {
            val result = gdprExportService.checkExportStatus(exportId)

            result.onSuccess { status ->
                _uiState.update {
                    it.copy(
                        exportId = exportId,
                        status = status.status,
                        downloadUrl = status.downloadUrl,
                        expiresAt = status.expiresAt
                    )
                }
            }
        }
    }
}

/**
 * UI state for Data Export screen
 */
data class DataExportUiState(
    val exportId: String? = null,
    val status: String? = null,
    val downloadUrl: String? = null,
    val expiresAt: String? = null,
    val isRequesting: Boolean = false,
    val isPolling: Boolean = false,
    val error: String? = null
)
