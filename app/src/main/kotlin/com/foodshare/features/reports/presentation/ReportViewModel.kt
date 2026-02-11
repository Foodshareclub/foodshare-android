package com.foodshare.features.reports.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.features.reports.domain.model.CreateReportInput
import com.foodshare.features.reports.domain.model.ReportReason
import com.foodshare.features.reports.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportUiState(
    val postId: Int = -1,
    val postName: String = "",
    val selectedReason: ReportReason? = null,
    val description: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val hasAlreadyReported: Boolean = false,
    val isCheckingExisting: Boolean = true,
    val error: String? = null
) {
    val canSubmit: Boolean
        get() = selectedReason != null && !isSubmitting && !hasAlreadyReported

    val descriptionCharCount: Int
        get() = description.length

    companion object {
        const val MAX_DESCRIPTION_LENGTH = 500
    }
}

@HiltViewModel
class ReportViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ReportRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val postId: Int = savedStateHandle.get<Int>("postId") ?: -1
    private val postName: String = savedStateHandle.get<String>("postName") ?: ""

    private val _uiState = MutableStateFlow(
        ReportUiState(postId = postId, postName = postName)
    )
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        checkIfAlreadyReported()
    }

    private fun checkIfAlreadyReported() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingExisting = true) }
            repository.hasUserReportedPost(postId, userId)
                .onSuccess { hasReported ->
                    _uiState.update {
                        it.copy(hasAlreadyReported = hasReported, isCheckingExisting = false)
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isCheckingExisting = false) }
                }
        }
    }

    fun selectReason(reason: ReportReason) {
        _uiState.update { it.copy(selectedReason = reason, error = null) }
    }

    fun updateDescription(text: String) {
        if (text.length <= ReportUiState.MAX_DESCRIPTION_LENGTH) {
            _uiState.update { it.copy(description = text, error = null) }
        }
    }

    fun submitReport() {
        val state = _uiState.value
        val reason = state.selectedReason ?: return
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val input = CreateReportInput(
                postId = state.postId,
                reporterId = userId,
                reason = reason.name.lowercase(),
                description = state.description.ifBlank { null }
            )

            repository.submitReport(input)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to submit report"
                        )
                    }
                }
        }
    }

    fun reset() {
        _uiState.update {
            ReportUiState(postId = postId, postName = postName)
        }
        checkIfAlreadyReported()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
