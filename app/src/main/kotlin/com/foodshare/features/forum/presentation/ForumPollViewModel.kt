package com.foodshare.features.forum.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.core.errors.ErrorBridge
import com.foodshare.core.validation.ValidationBridge
import com.foodshare.features.forum.domain.model.*
import com.foodshare.features.forum.domain.repository.ForumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Forum Poll feature - handles poll display, voting, and creation.
 *
 * SYNC: This mirrors Swift ForumPollViewModel
 */
@HiltViewModel
class ForumPollViewModel @Inject constructor(
    private val forumRepository: ForumRepository
) : ViewModel() {

    /**
     * UI state for forum poll interactions.
     */
    data class UiState(
        // Poll display
        val poll: ForumPoll? = null,
        val isLoading: Boolean = false,
        val isVoting: Boolean = false,
        val error: String? = null,
        val selectedOptionIds: Set<String> = emptySet(),
        // Create poll form
        val createQuestion: String = "",
        val createOptions: List<String> = listOf("", ""),
        val createPollType: PollType = PollType.SINGLE,
        val createEndDate: String? = null,
        val createIsAnonymous: Boolean = false,
        val createShowResultsBeforeVote: Boolean = false,
        val isCreating: Boolean = false,
        val createError: String? = null,
        val createSuccess: Boolean = false
    ) {
        /** Whether the create form is valid based on current input. */
        val isCreateFormValid: Boolean
            get() {
                if (createQuestion.isBlank()) return false
                val nonEmptyOptions = createOptions.filter { it.isNotBlank() }
                return nonEmptyOptions.size >= ValidationBridge.MIN_POLL_OPTIONS
            }

        /** Validation error for the question field (null if valid or empty). */
        val questionError: String?
            get() {
                if (createQuestion.isBlank()) return null
                val result = ValidationBridge.validatePollQuestion(createQuestion)
                return result.firstError
            }

        /** Validation error for the options (null if valid or not enough input yet). */
        val optionsError: String?
            get() {
                val nonEmptyOptions = createOptions.filter { it.isNotBlank() }
                if (nonEmptyOptions.size < 2) return null
                val result = ValidationBridge.validatePollOptions(nonEmptyOptions)
                return result.firstError
            }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // MARK: - Poll Display

    /**
     * Load poll data for a given forum post.
     */
    fun loadPoll(forumId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            forumRepository.getPoll(forumId)
                .onSuccess { poll ->
                    _uiState.update {
                        it.copy(
                            poll = poll,
                            isLoading = false,
                            // Pre-select user's existing votes if any
                            selectedOptionIds = poll.userVotes?.toSet() ?: emptySet()
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = ErrorBridge.mapForumError(error)
                        )
                    }
                }
        }
    }

    /**
     * Toggle selection of a poll option.
     * For single-choice polls, only one option can be selected at a time.
     * For multiple-choice polls, options are toggled independently.
     */
    fun selectOption(optionId: String) {
        _uiState.update { state ->
            val poll = state.poll ?: return@update state
            if (!poll.canVote) return@update state

            val newSelection = when (poll.pollType) {
                PollType.SINGLE -> {
                    if (state.selectedOptionIds.contains(optionId)) {
                        emptySet()
                    } else {
                        setOf(optionId)
                    }
                }
                PollType.MULTIPLE -> {
                    if (state.selectedOptionIds.contains(optionId)) {
                        state.selectedOptionIds - optionId
                    } else {
                        state.selectedOptionIds + optionId
                    }
                }
            }

            state.copy(selectedOptionIds = newSelection)
        }
    }

    /**
     * Submit the user's vote for the currently selected options.
     */
    fun castVote() {
        val state = _uiState.value
        val poll = state.poll ?: return
        if (state.selectedOptionIds.isEmpty() || state.isVoting) return
        if (!poll.canVote) return

        viewModelScope.launch {
            _uiState.update { it.copy(isVoting = true, error = null) }

            forumRepository.castVote(
                pollId = poll.id,
                optionIds = state.selectedOptionIds.toList()
            )
                .onSuccess { updatedPoll ->
                    _uiState.update {
                        it.copy(
                            poll = updatedPoll,
                            isVoting = false,
                            selectedOptionIds = updatedPoll.userVotes?.toSet() ?: it.selectedOptionIds
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isVoting = false,
                            error = ErrorBridge.mapForumError(error)
                        )
                    }
                }
        }
    }

    // MARK: - Create Poll Form

    /**
     * Update the poll question text.
     */
    fun updateCreateQuestion(question: String) {
        _uiState.update { it.copy(createQuestion = question) }
    }

    /**
     * Add a new blank option to the create form.
     * Limited to MAX_POLL_OPTIONS.
     */
    fun addOption() {
        _uiState.update { state ->
            if (state.createOptions.size >= ValidationBridge.MAX_POLL_OPTIONS) return@update state
            state.copy(createOptions = state.createOptions + "")
        }
    }

    /**
     * Remove an option at the given index.
     * Maintains minimum of MIN_POLL_OPTIONS options.
     */
    fun removeOption(index: Int) {
        _uiState.update { state ->
            if (state.createOptions.size <= ValidationBridge.MIN_POLL_OPTIONS) return@update state
            if (index !in state.createOptions.indices) return@update state
            state.copy(createOptions = state.createOptions.toMutableList().apply { removeAt(index) })
        }
    }

    /**
     * Update the text of an option at the given index.
     */
    fun updateOption(index: Int, text: String) {
        _uiState.update { state ->
            if (index !in state.createOptions.indices) return@update state
            state.copy(
                createOptions = state.createOptions.toMutableList().apply { set(index, text) }
            )
        }
    }

    /**
     * Update the poll type (single or multiple choice).
     */
    fun updateCreatePollType(pollType: PollType) {
        _uiState.update { it.copy(createPollType = pollType) }
    }

    /**
     * Update the poll end date (ISO 8601 string or null for no end date).
     */
    fun updateCreateEndDate(endDate: String?) {
        _uiState.update { it.copy(createEndDate = endDate) }
    }

    /**
     * Toggle whether the poll is anonymous.
     */
    fun toggleCreateIsAnonymous() {
        _uiState.update { it.copy(createIsAnonymous = !it.createIsAnonymous) }
    }

    /**
     * Toggle whether results are shown before voting.
     */
    fun toggleCreateShowResultsBeforeVote() {
        _uiState.update { it.copy(createShowResultsBeforeVote = !it.createShowResultsBeforeVote) }
    }

    /**
     * Validate and create a new poll for the given forum post.
     */
    fun createPoll(forumId: Int) {
        val state = _uiState.value
        if (state.isCreating) return

        // Validate question
        val questionResult = ValidationBridge.validatePollQuestion(state.createQuestion)
        if (!questionResult.isValid) {
            _uiState.update { it.copy(createError = questionResult.firstError) }
            return
        }

        // Filter and validate options
        val nonEmptyOptions = state.createOptions.map { it.trim() }.filter { it.isNotEmpty() }
        val optionsResult = ValidationBridge.validatePollOptions(nonEmptyOptions)
        if (!optionsResult.isValid) {
            _uiState.update { it.copy(createError = optionsResult.firstError) }
            return
        }

        val request = CreatePollRequest(
            forumId = forumId,
            question = ValidationBridge.sanitizeForumTitle(state.createQuestion),
            pollType = state.createPollType,
            options = nonEmptyOptions.map { ValidationBridge.sanitizeText(it) },
            endsAt = state.createEndDate,
            isAnonymous = state.createIsAnonymous,
            showResultsBeforeVote = state.createShowResultsBeforeVote
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }

            forumRepository.createPoll(request)
                .onSuccess { poll ->
                    _uiState.update {
                        it.copy(
                            poll = poll,
                            isCreating = false,
                            createSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            createError = ErrorBridge.mapForumError(error)
                        )
                    }
                }
        }
    }

    /**
     * Reset the create poll form to its initial state.
     */
    fun resetCreateForm() {
        _uiState.update {
            it.copy(
                createQuestion = "",
                createOptions = listOf("", ""),
                createPollType = PollType.SINGLE,
                createEndDate = null,
                createIsAnonymous = false,
                createShowResultsBeforeVote = false,
                isCreating = false,
                createError = null,
                createSuccess = false
            )
        }
    }

    /**
     * Clear the display error.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear the create form error.
     */
    fun clearCreateError() {
        _uiState.update { it.copy(createError = null) }
    }
}
