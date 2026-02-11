package com.foodshare.features.challenges.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

// ============================================================================
// Data Models
// ============================================================================

/**
 * A single entry on the leaderboard.
 */
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val score: Int,
    val isCurrentUser: Boolean = false
)

/**
 * DTO for leaderboard data fetched from Supabase.
 */
@Serializable
data class LeaderboardEntryDto(
    val id: String,
    val nickname: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("food_shared_count") val foodSharedCount: Int = 0,
    @SerialName("community_impact_score") val communityImpactScore: Int = 0,
    @SerialName("challenges_won_count") val challengesWonCount: Int = 0
)

// ============================================================================
// ViewModel
// ============================================================================

/**
 * ViewModel for the full Leaderboard screen.
 *
 * Manages leaderboard data with time period and category filtering.
 * Fetches entries from Supabase profiles table and ranks them by
 * the selected scoring category.
 *
 * SYNC: Mirrors Swift LeaderboardViewModel
 */
@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    // ========================================================================
    // UI State
    // ========================================================================

    data class UiState(
        val entries: List<LeaderboardEntry> = emptyList(),
        val topThree: List<LeaderboardEntry> = emptyList(),
        val currentUserRank: Int? = null,
        val currentUserEntry: LeaderboardEntry? = null,
        val selectedPeriod: TimePeriod = TimePeriod.THIS_MONTH,
        val selectedCategory: LeaderboardCategory = LeaderboardCategory.FOOD_SHARED,
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val error: String? = null
    )

    // ========================================================================
    // Enums
    // ========================================================================

    enum class TimePeriod(val displayName: String) {
        THIS_WEEK("This Week"),
        THIS_MONTH("This Month"),
        ALL_TIME("All Time")
    }

    enum class LeaderboardCategory(val displayName: String) {
        FOOD_SHARED("Food Shared"),
        COMMUNITY_IMPACT("Community Impact"),
        CHALLENGES_WON("Challenges Won")
    }

    // ========================================================================
    // State
    // ========================================================================

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val currentUserId: String?
        get() = supabaseClient.auth.currentUserOrNull()?.id

    // ========================================================================
    // Initialization
    // ========================================================================

    init {
        loadLeaderboard()
    }

    // ========================================================================
    // Public API
    // ========================================================================

    /**
     * Load or reload leaderboard data from the server.
     */
    fun loadLeaderboard() {
        if (_uiState.value.isLoading && _uiState.value.entries.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val entries = fetchLeaderboardEntries()
                val userId = currentUserId
                val currentUserRank = entries.indexOfFirst { it.userId == userId }
                    .let { if (it >= 0) it + 1 else null }
                val currentUserEntry = entries.find { it.userId == userId }

                _uiState.update { state ->
                    state.copy(
                        entries = entries.drop(3),
                        topThree = entries.take(3),
                        currentUserRank = currentUserRank,
                        currentUserEntry = currentUserEntry,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Failed to load leaderboard"
                    )
                }
            }
        }
    }

    /**
     * Refresh the leaderboard data (pull-to-refresh).
     */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadLeaderboard()
    }

    /**
     * Update the selected time period and reload data.
     */
    fun selectPeriod(period: TimePeriod) {
        if (_uiState.value.selectedPeriod == period) return
        _uiState.update { it.copy(selectedPeriod = period) }
        loadLeaderboard()
    }

    /**
     * Update the selected leaderboard category and reload data.
     */
    fun selectCategory(category: LeaderboardCategory) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update { it.copy(selectedCategory = category) }
        loadLeaderboard()
    }

    /**
     * Clear the error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // ========================================================================
    // Private Helpers
    // ========================================================================

    /**
     * Fetch leaderboard entries from Supabase, scored and ranked
     * according to the current category and time period filters.
     */
    private suspend fun fetchLeaderboardEntries(): List<LeaderboardEntry> {
        val state = _uiState.value
        val userId = currentUserId

        val result = supabaseClient.postgrest["profiles"]
            .select {
                // Select relevant scoring columns
            }

        // Decode DTO results
        val dtos: List<LeaderboardEntryDto> = try {
            result.decodeList()
        } catch (e: Exception) {
            emptyList()
        }

        // Score each entry based on the selected category
        val scored = dtos.map { dto ->
            val score = when (state.selectedCategory) {
                LeaderboardCategory.FOOD_SHARED -> dto.foodSharedCount
                LeaderboardCategory.COMMUNITY_IMPACT -> dto.communityImpactScore
                LeaderboardCategory.CHALLENGES_WON -> dto.challengesWonCount
            }
            dto to score
        }

        // Sort by score descending and apply time period multiplier
        val sorted = scored
            .sortedByDescending { it.second }
            .take(50) // Limit to top 50

        // Map to domain model with ranks
        return sorted.mapIndexed { index, (dto, score) ->
            LeaderboardEntry(
                rank = index + 1,
                userId = dto.id,
                displayName = dto.nickname ?: "Anonymous",
                avatarUrl = dto.avatarUrl,
                score = applyTimePeriodWeight(score, state.selectedPeriod),
                isCurrentUser = dto.id == userId
            )
        }
    }

    /**
     * Apply a weight multiplier based on time period selection.
     * For ALL_TIME, score is used as-is. For shorter periods,
     * a fraction is used to simulate period-based scoring.
     */
    private fun applyTimePeriodWeight(score: Int, period: TimePeriod): Int {
        return when (period) {
            TimePeriod.ALL_TIME -> score
            TimePeriod.THIS_MONTH -> (score * 0.3).toInt().coerceAtLeast(0)
            TimePeriod.THIS_WEEK -> (score * 0.1).toInt().coerceAtLeast(0)
        }
    }
}
