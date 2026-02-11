package com.foodshare.features.settings.presentation

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// DataStore extension
private val Context.notificationsDataStore by preferencesDataStore(name = "notifications_settings")

/**
 * Preference keys for notification settings
 */
private object NotificationKeys {
    val PUSH_ENABLED = booleanPreferencesKey("push_enabled")
    val EMAIL_ENABLED = booleanPreferencesKey("email_enabled")
    val MESSAGES_ENABLED = booleanPreferencesKey("messages_enabled")
    val NEW_LISTINGS_ENABLED = booleanPreferencesKey("new_listings_enabled")
    val REVIEWS_ENABLED = booleanPreferencesKey("reviews_enabled")
    val CHALLENGES_ENABLED = booleanPreferencesKey("challenges_enabled")
    val FORUM_ENABLED = booleanPreferencesKey("forum_enabled")
}

/**
 * ViewModel for Notifications Settings screen
 */
@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val prefs = context.notificationsDataStore.data.first()

            _uiState.update {
                it.copy(
                    pushEnabled = prefs[NotificationKeys.PUSH_ENABLED] ?: true,
                    emailEnabled = prefs[NotificationKeys.EMAIL_ENABLED] ?: true,
                    messagesEnabled = prefs[NotificationKeys.MESSAGES_ENABLED] ?: true,
                    newListingsEnabled = prefs[NotificationKeys.NEW_LISTINGS_ENABLED] ?: true,
                    reviewsEnabled = prefs[NotificationKeys.REVIEWS_ENABLED] ?: true,
                    challengesEnabled = prefs[NotificationKeys.CHALLENGES_ENABLED] ?: true,
                    forumEnabled = prefs[NotificationKeys.FORUM_ENABLED] ?: true,
                    isLoading = false
                )
            }
        }
    }

    fun togglePush(enabled: Boolean) {
        _uiState.update { it.copy(pushEnabled = enabled) }
        savePreference(NotificationKeys.PUSH_ENABLED, enabled)
    }

    fun toggleEmail(enabled: Boolean) {
        _uiState.update { it.copy(emailEnabled = enabled) }
        savePreference(NotificationKeys.EMAIL_ENABLED, enabled)
    }

    fun toggleMessages(enabled: Boolean) {
        _uiState.update { it.copy(messagesEnabled = enabled) }
        savePreference(NotificationKeys.MESSAGES_ENABLED, enabled)
    }

    fun toggleNewListings(enabled: Boolean) {
        _uiState.update { it.copy(newListingsEnabled = enabled) }
        savePreference(NotificationKeys.NEW_LISTINGS_ENABLED, enabled)
    }

    fun toggleReviews(enabled: Boolean) {
        _uiState.update { it.copy(reviewsEnabled = enabled) }
        savePreference(NotificationKeys.REVIEWS_ENABLED, enabled)
    }

    fun toggleChallenges(enabled: Boolean) {
        _uiState.update { it.copy(challengesEnabled = enabled) }
        savePreference(NotificationKeys.CHALLENGES_ENABLED, enabled)
    }

    fun toggleForum(enabled: Boolean) {
        _uiState.update { it.copy(forumEnabled = enabled) }
        savePreference(NotificationKeys.FORUM_ENABLED, enabled)
    }

    private fun savePreference(
        key: androidx.datastore.preferences.core.Preferences.Key<Boolean>,
        value: Boolean
    ) {
        viewModelScope.launch {
            context.notificationsDataStore.edit { prefs ->
                prefs[key] = value
            }
        }
    }
}

/**
 * UI state for Notifications Settings
 */
data class NotificationsUiState(
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val messagesEnabled: Boolean = true,
    val newListingsEnabled: Boolean = true,
    val reviewsEnabled: Boolean = true,
    val challengesEnabled: Boolean = true,
    val forumEnabled: Boolean = true,
    val isLoading: Boolean = true
)
