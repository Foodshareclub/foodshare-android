package com.foodshare.features.profile.presentation

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

// DataStore for email preferences
private val Context.emailPreferencesDataStore by preferencesDataStore(name = "email_preferences")

/**
 * Preference keys for per-type email settings.
 */
private object EmailPrefKeys {
    val MARKETING = booleanPreferencesKey("email_marketing")
    val PRODUCT_UPDATES = booleanPreferencesKey("email_product_updates")
    val COMMUNITY_NOTIFICATIONS = booleanPreferencesKey("email_community_notifications")
    val FOOD_ALERTS = booleanPreferencesKey("email_food_alerts")
    val WEEKLY_DIGEST = booleanPreferencesKey("email_weekly_digest")
}

/**
 * ViewModel for the Email Preferences screen.
 *
 * Manages per-type email notification toggles, persisting settings locally
 * via DataStore and syncing to the Supabase "email_preferences" table.
 *
 * SYNC: Mirrors Swift EmailPreferencesViewModel
 */
@HiltViewModel
class EmailPreferencesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    /**
     * UI state for the Email Preferences screen.
     */
    data class UiState(
        val marketingEnabled: Boolean = true,
        val productUpdatesEnabled: Boolean = true,
        val communityNotificationsEnabled: Boolean = true,
        val foodAlertsEnabled: Boolean = true,
        val weeklyDigestEnabled: Boolean = true,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    /**
     * Load saved email preferences from DataStore.
     */
    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                val prefs = context.emailPreferencesDataStore.data.first()

                _uiState.update {
                    it.copy(
                        marketingEnabled = prefs[EmailPrefKeys.MARKETING] ?: true,
                        productUpdatesEnabled = prefs[EmailPrefKeys.PRODUCT_UPDATES] ?: true,
                        communityNotificationsEnabled = prefs[EmailPrefKeys.COMMUNITY_NOTIFICATIONS] ?: true,
                        foodAlertsEnabled = prefs[EmailPrefKeys.FOOD_ALERTS] ?: true,
                        weeklyDigestEnabled = prefs[EmailPrefKeys.WEEKLY_DIGEST] ?: true,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load email preferences"
                    )
                }
            }
        }
    }

    /**
     * Toggle marketing emails preference.
     */
    fun toggleMarketing(enabled: Boolean) {
        _uiState.update { it.copy(marketingEnabled = enabled, successMessage = null) }
        savePreference(EmailPrefKeys.MARKETING, enabled)
        syncToBackend()
    }

    /**
     * Toggle product updates emails preference.
     */
    fun toggleProductUpdates(enabled: Boolean) {
        _uiState.update { it.copy(productUpdatesEnabled = enabled, successMessage = null) }
        savePreference(EmailPrefKeys.PRODUCT_UPDATES, enabled)
        syncToBackend()
    }

    /**
     * Toggle community notifications emails preference.
     */
    fun toggleCommunityNotifications(enabled: Boolean) {
        _uiState.update { it.copy(communityNotificationsEnabled = enabled, successMessage = null) }
        savePreference(EmailPrefKeys.COMMUNITY_NOTIFICATIONS, enabled)
        syncToBackend()
    }

    /**
     * Toggle food alerts emails preference.
     */
    fun toggleFoodAlerts(enabled: Boolean) {
        _uiState.update { it.copy(foodAlertsEnabled = enabled, successMessage = null) }
        savePreference(EmailPrefKeys.FOOD_ALERTS, enabled)
        syncToBackend()
    }

    /**
     * Toggle weekly digest emails preference.
     */
    fun toggleWeeklyDigest(enabled: Boolean) {
        _uiState.update { it.copy(weeklyDigestEnabled = enabled, successMessage = null) }
        savePreference(EmailPrefKeys.WEEKLY_DIGEST, enabled)
        syncToBackend()
    }

    /**
     * Save a single preference key to DataStore.
     */
    private fun savePreference(
        key: androidx.datastore.preferences.core.Preferences.Key<Boolean>,
        value: Boolean
    ) {
        viewModelScope.launch {
            context.emailPreferencesDataStore.edit { prefs ->
                prefs[key] = value
            }
        }
    }

    /**
     * Sync all current email preferences to the Supabase backend.
     *
     * Upserts into the "email_preferences" table keyed by user ID.
     */
    private fun syncToBackend() {
        viewModelScope.launch {
            val state = _uiState.value
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch

            try {
                val updateData = buildJsonObject {
                    put("user_id", userId)
                    put("marketing_enabled", state.marketingEnabled)
                    put("product_updates_enabled", state.productUpdatesEnabled)
                    put("community_notifications_enabled", state.communityNotificationsEnabled)
                    put("food_alerts_enabled", state.foodAlertsEnabled)
                    put("weekly_digest_enabled", state.weeklyDigestEnabled)
                }

                supabaseClient.from("email_preferences")
                    .upsert(updateData)

                _uiState.update {
                    it.copy(successMessage = "Preferences saved")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to sync preferences")
                }
            }
        }
    }

    /**
     * Clear the current error message.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear the success message.
     */
    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
