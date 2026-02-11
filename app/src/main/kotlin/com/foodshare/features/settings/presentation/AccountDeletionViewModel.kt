package com.foodshare.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

/**
 * ViewModel for Account Deletion screen
 *
 * Manages the account deletion flow including password verification,
 * confirmation, and the RPC call to request account deletion.
 */
@HiltViewModel
class AccountDeletionViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    data class UiState(
        val password: String = "",
        val isConfirmed: Boolean = false,
        val isDeleting: Boolean = false,
        val error: String? = null,
        val isDeleted: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, error = null) }
    }

    fun toggleConfirmation(confirmed: Boolean) {
        _uiState.update { it.copy(isConfirmed = confirmed) }
    }

    fun deleteAccount(onDeleted: () -> Unit) {
        val state = _uiState.value

        if (state.password.isBlank()) {
            _uiState.update { it.copy(error = "Password is required to confirm deletion") }
            return
        }

        if (!state.isConfirmed) {
            _uiState.update { it.copy(error = "Please confirm that you understand this action is permanent") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }

            try {
                // Re-authenticate with password to verify identity
                val user = supabaseClient.auth.currentUserOrNull()
                val email = user?.email
                    ?: throw IllegalStateException("No email found for current user")

                supabaseClient.auth.signInWith(Email) {
                    this.email = email
                    this.password = state.password
                }

                // Call the RPC function to request account deletion
                supabaseClient.postgrest.rpc(
                    function = "request_account_deletion",
                    parameters = buildJsonObject {
                        put("reason", "user_requested")
                    }
                )

                // Sign out the user
                supabaseClient.auth.signOut()

                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        isDeleted = true
                    )
                }

                onDeleted()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        error = when {
                            e.message?.contains("Invalid login", ignoreCase = true) == true ->
                                "Incorrect password. Please try again."
                            e.message?.contains("invalid_grant", ignoreCase = true) == true ->
                                "Incorrect password. Please try again."
                            else -> "Failed to delete account: ${e.message}"
                        }
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
