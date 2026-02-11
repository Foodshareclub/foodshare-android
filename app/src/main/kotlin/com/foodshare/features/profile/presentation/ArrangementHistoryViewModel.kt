package com.foodshare.features.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

/**
 * Data class representing a single arrangement history entry.
 */
@Serializable
data class ArrangementHistoryItem(
    val id: String,
    @SerialName("listing_title") val listingTitle: String,
    @SerialName("counterparty_name") val counterpartyName: String,
    val status: String, // "pending", "accepted", "completed", "cancelled"
    @SerialName("created_at") val createdAt: String,
    @SerialName("completed_at") val completedAt: String? = null
)

/**
 * ViewModel for the Arrangement History screen.
 *
 * Loads and manages the list of past arrangements for the current user.
 * Fetches data from the Supabase "arrangements" table, joining with
 * profiles and listings for display names.
 *
 * SYNC: Mirrors Swift ArrangementHistoryViewModel
 */
@HiltViewModel
class ArrangementHistoryViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    /**
     * UI state for the Arrangement History screen.
     */
    data class UiState(
        val arrangements: List<ArrangementHistoryItem> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null
    ) {
        val isEmpty: Boolean
            get() = arrangements.isEmpty() && !isLoading && error == null
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadArrangements()
    }

    /**
     * Load arrangement history from the Supabase "arrangements" table.
     *
     * Queries arrangements where the current user is either the requester or the owner,
     * ordered by creation date descending (most recent first).
     */
    fun loadArrangements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Please sign in to view your arrangements"
                        )
                    }
                    return@launch
                }

                val arrangements = supabaseClient.from("arrangements")
                    .select {
                        filter {
                            or {
                                eq("requester_id", userId)
                                eq("owner_id", userId)
                            }
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<ArrangementHistoryDto>()

                val historyItems = arrangements.map { dto ->
                    val isRequester = dto.requesterId == userId
                    ArrangementHistoryItem(
                        id = dto.id,
                        listingTitle = dto.listingTitle ?: "Unknown Listing",
                        counterpartyName = if (isRequester) {
                            dto.ownerName ?: "Unknown User"
                        } else {
                            dto.requesterName ?: "Unknown User"
                        },
                        status = dto.status,
                        createdAt = dto.createdAt ?: "",
                        completedAt = dto.completedAt
                    )
                }

                _uiState.update {
                    it.copy(
                        arrangements = historyItems,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load arrangements"
                    )
                }
            }
        }
    }

    /**
     * Refresh the arrangement list by reloading from the server.
     */
    fun refresh() {
        loadArrangements()
    }

    /**
     * Clear the current error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * Internal DTO for decoding arrangement rows from Supabase.
 */
@Serializable
private data class ArrangementHistoryDto(
    val id: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("owner_id") val ownerId: String,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("listing_title") val listingTitle: String? = null,
    @SerialName("requester_name") val requesterName: String? = null,
    @SerialName("owner_name") val ownerName: String? = null
)
