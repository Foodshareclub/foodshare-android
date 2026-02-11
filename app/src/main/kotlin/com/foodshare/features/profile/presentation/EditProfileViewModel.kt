package com.foodshare.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.core.moderation.ModerationBridge
import com.foodshare.core.moderation.ModerationContentType
import com.foodshare.core.validation.ValidationBridge
import com.foodshare.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Edit Profile screen
 *
 * Manages profile editing with Swift validation and moderation.
 *
 * SYNC: Mirrors Swift EditProfileViewModel
 */
@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            profileRepository.currentProfile.collect { profile ->
                if (profile != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            userId = profile.id,
                            nickname = profile.nickname ?: "",
                            bio = profile.bio ?: "",
                            location = profile.location ?: "",
                            avatarUrl = profile.avatarUrl,
                            error = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to load profile"
                        )
                    }
                }
            }
        }
    }

    /**
     * Update nickname with Swift validation.
     */
    fun updateNickname(nickname: String) {
        // Validate using Swift (matches iOS)
        val validationError = ValidationBridge.validateNickname(nickname)

        _uiState.update {
            it.copy(
                nickname = nickname,
                nicknameError = validationError
            )
        }
    }

    /**
     * Update bio with Swift validation.
     */
    fun updateBio(bio: String) {
        // Validate using Swift (matches iOS)
        val validationError = ValidationBridge.validateBio(bio)

        _uiState.update {
            it.copy(
                bio = bio,
                bioError = validationError
            )
        }
    }

    /**
     * Update location (optional field, no validation).
     */
    fun updateLocation(location: String) {
        _uiState.update {
            it.copy(location = location)
        }
    }

    /**
     * Upload avatar image.
     */
    fun pickAvatar(imageBytes: ByteArray, mimeType: String, userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingAvatar = true, avatarUploadProgress = 0f) }

            profileRepository.uploadAvatar(userId, imageBytes, mimeType)
                .onSuccess { url ->
                    _uiState.update {
                        it.copy(
                            avatarUrl = url,
                            isUploadingAvatar = false,
                            avatarUploadProgress = 1f,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isUploadingAvatar = false,
                            avatarUploadProgress = 0f,
                            error = error.message ?: "Failed to upload avatar"
                        )
                    }
                }
        }
    }

    /**
     * Save profile with Swift sanitization and moderation.
     */
    fun save() {
        val state = _uiState.value

        // Validate all fields before saving
        if (state.nicknameError != null || state.bioError != null) {
            return
        }

        // Run Swift-based content moderation check on profile content
        val moderationResult = ModerationBridge.checkBeforeSubmission(
            title = state.nickname.takeIf { it.isNotBlank() },
            description = state.bio.takeIf { it.isNotBlank() },
            contentType = ModerationContentType.PROFILE
        )

        // Block save if moderation fails
        if (!moderationResult.canSubmit) {
            val issueDescriptions = moderationResult.issues.joinToString("\n") { it.description }
            _uiState.update {
                it.copy(
                    error = "Profile contains inappropriate content: $issueDescriptions"
                )
            }
            return
        }

        // Use sanitized content from moderation result (or fallback to ValidationBridge)
        val sanitizedNickname = moderationResult.sanitizedTitle
            ?: ValidationBridge.sanitizeDisplayName(state.nickname)
        val sanitizedBio = moderationResult.sanitizedDescription
            ?: ValidationBridge.sanitizeBio(state.bio)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            profileRepository.updateProfile(
                nickname = sanitizedNickname.ifBlank { null },
                bio = sanitizedBio.ifBlank { null },
                location = state.location.ifBlank { null },
                avatarUrl = state.avatarUrl
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = null,
                            saveSuccess = true
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message ?: "Failed to save profile"
                        )
                    }
                }
        }
    }

    /**
     * Cancel editing and go back.
     */
    fun cancel() {
        // State will be cleared when navigating back
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI state for the Edit Profile screen
 */
data class EditProfileUiState(
    val userId: String? = null,
    val nickname: String = "",
    val bio: String = "",
    val location: String = "",
    val avatarUrl: String? = null,
    val nicknameError: String? = null,
    val bioError: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingAvatar: Boolean = false,
    val avatarUploadProgress: Float = 0f,
    val error: String? = null,
    val saveSuccess: Boolean = false
) {
    val isValid: Boolean
        get() = nicknameError == null && bioError == null

    val hasChanges: Boolean
        get() = nickname.isNotBlank() || bio.isNotBlank() || location.isNotBlank()

    val nicknameCharCount: String
        get() = "${nickname.length}/50"

    val bioCharCount: String
        get() = "${bio.length}/500"
}
