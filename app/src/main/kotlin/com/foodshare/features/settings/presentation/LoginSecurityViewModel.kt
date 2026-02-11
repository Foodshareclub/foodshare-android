package com.foodshare.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Active session data model
 */
data class ActiveSession(
    val id: String,
    val deviceName: String,
    val lastActiveAt: String,
    val isCurrent: Boolean
)

/**
 * ViewModel for Login & Security screen
 *
 * Manages password changes, MFA status, biometric preferences,
 * and active session listing/revocation.
 */
@HiltViewModel
class LoginSecurityViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    data class UiState(
        val isMfaEnabled: Boolean = false,
        val isBiometricEnabled: Boolean = false,
        val sessions: List<ActiveSession> = emptyList(),
        val isLoading: Boolean = true,
        val isChangingPassword: Boolean = false,
        val currentPassword: String = "",
        val newPassword: String = "",
        val confirmPassword: String = "",
        val passwordError: String? = null,
        val error: String? = null,
        val successMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadSecurityData()
    }

    private fun loadSecurityData() {
        viewModelScope.launch {
            try {
                val user = supabaseClient.auth.currentUserOrNull()
                if (user == null) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val hasMFA = user.factors?.isNotEmpty() == true

                // Build active sessions from current session info
                val currentSession = supabaseClient.auth.currentSessionOrNull()
                val sessions = mutableListOf<ActiveSession>()

                currentSession?.let { session ->
                    sessions.add(
                        ActiveSession(
                            id = session.accessToken.take(12),
                            deviceName = "Current Device",
                            lastActiveAt = "Now",
                            isCurrent = true
                        )
                    )
                }

                _uiState.update {
                    it.copy(
                        isMfaEnabled = hasMFA,
                        sessions = sessions,
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

    fun updateCurrentPassword(password: String) {
        _uiState.update { it.copy(currentPassword = password, passwordError = null) }
    }

    fun updateNewPassword(password: String) {
        _uiState.update { it.copy(newPassword = password, passwordError = null) }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { it.copy(confirmPassword = password, passwordError = null) }
    }

    fun changePassword() {
        val state = _uiState.value

        // Validate inputs
        if (state.currentPassword.isBlank()) {
            _uiState.update { it.copy(passwordError = "Current password is required") }
            return
        }
        if (state.newPassword.length < 8) {
            _uiState.update { it.copy(passwordError = "New password must be at least 8 characters") }
            return
        }
        if (state.newPassword != state.confirmPassword) {
            _uiState.update { it.copy(passwordError = "Passwords do not match") }
            return
        }
        if (state.currentPassword == state.newPassword) {
            _uiState.update { it.copy(passwordError = "New password must be different from current password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, passwordError = null) }

            try {
                // Re-authenticate with current password to verify identity
                val user = supabaseClient.auth.currentUserOrNull()
                val email = user?.email
                if (email != null) {
                    supabaseClient.auth.signInWith(
                        io.github.jan.supabase.auth.providers.builtin.Email
                    ) {
                        this.email = email
                        this.password = state.currentPassword
                    }
                }

                // Update to new password
                supabaseClient.auth.updateUser {
                    password = state.newPassword
                }

                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        successMessage = "Password changed successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        passwordError = "Failed to change password: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun revokeSession(sessionId: String) {
        viewModelScope.launch {
            try {
                // Remove the session from the local list
                _uiState.update { state ->
                    state.copy(
                        sessions = state.sessions.filter { it.id != sessionId }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
