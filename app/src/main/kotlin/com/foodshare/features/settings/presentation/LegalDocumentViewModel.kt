package com.foodshare.features.settings.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.core.security.LegalDocType
import com.foodshare.core.security.LegalDocumentService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Legal Document screen
 */
@HiltViewModel
class LegalDocumentViewModel @Inject constructor(
    private val legalDocumentService: LegalDocumentService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val documentTypeArg: String? = savedStateHandle["documentType"]

    private val _uiState = MutableStateFlow(LegalDocumentUiState())
    val uiState: StateFlow<LegalDocumentUiState> = _uiState.asStateFlow()

    init {
        loadDocument()
    }

    private fun loadDocument() {
        viewModelScope.launch {
            try {
                val docType = when (documentTypeArg) {
                    "terms" -> LegalDocType.TERMS_OF_SERVICE
                    "privacy" -> LegalDocType.PRIVACY_POLICY
                    "licenses" -> LegalDocType.OPEN_SOURCE_LICENSES
                    else -> LegalDocType.TERMS_OF_SERVICE
                }

                val content = legalDocumentService.getDocument(docType)

                _uiState.update {
                    it.copy(
                        title = docType.displayName,
                        content = content,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }
}

/**
 * UI state for Legal Document screen
 */
data class LegalDocumentUiState(
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)
