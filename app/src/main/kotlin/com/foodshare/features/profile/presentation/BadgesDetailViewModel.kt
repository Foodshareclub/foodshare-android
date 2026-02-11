package com.foodshare.features.profile.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foodshare.features.forum.domain.model.ForumBadge
import com.foodshare.features.forum.domain.model.ForumUserStats
import com.foodshare.features.forum.domain.model.UserBadge
import com.foodshare.features.forum.domain.repository.ForumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Badges Detail screen.
 *
 * Loads all available badges, the user's earned badges, and user stats
 * to compute badge progress for unearned badges.
 *
 * SYNC: This mirrors the iOS BadgesDetailViewModel pattern.
 */
@HiltViewModel
class BadgesDetailViewModel @Inject constructor(
    private val forumRepository: ForumRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * UI state for the Badges Detail screen.
     */
    data class UiState(
        val allBadges: List<ForumBadge> = emptyList(),
        val earnedBadges: List<UserBadge> = emptyList(),
        val userStats: ForumUserStats? = null,
        val isLoading: Boolean = true,
        val error: String? = null
    ) {
        /** Set of earned badge IDs for quick lookup. */
        val earnedBadgeIds: Set<Int>
            get() = earnedBadges.map { it.badgeId }.toSet()

        /** Badge progress map (badgeId -> progress 0.0-1.0) for unearned auto-criteria badges. */
        val badgeProgress: Map<Int, Double>
            get() {
                val stats = userStats ?: return emptyMap()
                return allBadges
                    .filter { !earnedBadgeIds.contains(it.id) && it.hasAutoCriteria }
                    .associate { badge ->
                        badge.id to (badge.criteria?.progress(stats) ?: 0.0)
                    }
            }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadBadges()
    }

    /**
     * Load all badges data: available badges, earned badges, and user stats.
     */
    private fun loadBadges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Load all data in parallel
            launch { loadAllBadges() }
            launch { loadEarnedBadges() }
            launch { loadUserStats() }
        }
    }

    private suspend fun loadAllBadges() {
        forumRepository.getBadges()
            .onSuccess { badges ->
                _uiState.update {
                    it.copy(
                        allBadges = badges,
                        isLoading = false
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load badges"
                    )
                }
            }
    }

    private suspend fun loadEarnedBadges() {
        forumRepository.getUserBadges()
            .onSuccess { badges ->
                _uiState.update { it.copy(earnedBadges = badges) }
            }
    }

    private suspend fun loadUserStats() {
        forumRepository.getUserStats()
            .onSuccess { stats ->
                _uiState.update { it.copy(userStats = stats) }
            }
    }

    /**
     * Refresh all badges data.
     */
    fun refresh() {
        loadBadges()
    }
}
