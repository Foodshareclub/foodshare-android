package com.foodshare.features.feedback.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.features.feedback.domain.model.CreateFeedbackInput
import com.foodshare.features.feedback.domain.model.FeedbackType
import com.foodshare.features.feedback.domain.repository.FeedbackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FeedbackUiState(
    val name: String = "",
    val email: String = "",
    val subject: String = "",
    val message: String = "",
    val feedbackType: FeedbackType = FeedbackType.GENERAL,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val subjectError: String? = null,
    val messageError: String? = null
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() && email.isNotBlank() && subject.isNotBlank() &&
                message.isNotBlank() && !isSubmitting &&
                nameError == null && emailError == null &&
                subjectError == null && messageError == null

    companion object {
        const val MAX_SUBJECT_LENGTH = 100
        const val MAX_MESSAGE_LENGTH = 2000
        const val MIN_MESSAGE_LENGTH = 10
    }
}

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val repository: FeedbackRepository,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    init {
        prefillUserData()
    }

    private fun prefillUserData() {
        val user = supabaseClient.auth.currentUserOrNull()
        user?.email?.let { email ->
            _uiState.update { it.copy(email = email) }
        }
    }

    fun updateName(name: String) {
        val error = when {
            name.isNotBlank() && name.trim().length < 2 -> "Name must be at least 2 characters"
            else -> null
        }
        _uiState.update { it.copy(name = name, nameError = error) }
    }

    fun updateEmail(email: String) {
        val error = when {
            email.isNotBlank() && !isValidEmail(email) -> "Please enter a valid email"
            else -> null
        }
        _uiState.update { it.copy(email = email, emailError = error) }
    }

    fun updateSubject(subject: String) {
        if (subject.length <= FeedbackUiState.MAX_SUBJECT_LENGTH) {
            val error = when {
                subject.isNotBlank() && subject.trim().length < 3 -> "Subject must be at least 3 characters"
                else -> null
            }
            _uiState.update { it.copy(subject = subject, subjectError = error) }
        }
    }

    fun updateMessage(message: String) {
        if (message.length <= FeedbackUiState.MAX_MESSAGE_LENGTH) {
            val error = when {
                message.isNotBlank() && message.trim().length < FeedbackUiState.MIN_MESSAGE_LENGTH ->
                    "Message must be at least ${FeedbackUiState.MIN_MESSAGE_LENGTH} characters"
                else -> null
            }
            _uiState.update { it.copy(message = message, messageError = error) }
        }
    }

    fun updateFeedbackType(type: FeedbackType) {
        _uiState.update { it.copy(feedbackType = type) }
    }

    fun submitFeedback() {
        val state = _uiState.value
        if (!state.canSubmit) return

        val userId = supabaseClient.auth.currentUserOrNull()?.id

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val input = CreateFeedbackInput(
                name = state.name.trim(),
                email = state.email.trim(),
                subject = state.subject.trim(),
                message = state.message.trim(),
                type = state.feedbackType.name.lowercase(),
                userId = userId
            )

            repository.submitFeedback(input)
                .onSuccess {
                    _uiState.update { it.copy(isSubmitting = false, isSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            error = error.message ?: "Failed to submit feedback"
                        )
                    }
                }
        }
    }

    fun reset() {
        _uiState.value = FeedbackUiState()
        prefillUserData()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return emailPattern.matches(email.trim())
    }
}
