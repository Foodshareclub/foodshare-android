package com.foodshare.features.settings.presentation

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Serializable settings backup model
 */
@Serializable
data class SettingsBackup(
    val version: Int = 1,
    val exportedAt: String = "",
    val theme: String = "system",
    val language: String = "en",
    val textScale: Float = 1.0f,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    val boldText: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val pushEnabled: Boolean = true,
    val emailNotifications: Boolean = false,
    val profileVisible: Boolean = true,
    val showLocation: Boolean = true,
    val allowMessages: Boolean = true
)

/**
 * ViewModel for Settings Backup screen
 *
 * Handles exporting and importing user settings as JSON files,
 * using DataStore for reading/writing preferences and
 * Android's Storage Access Framework for file operations.
 */
@HiltViewModel
class SettingsBackupViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class UiState(
        val lastBackupTimestamp: String? = null,
        val isExporting: Boolean = false,
        val isImporting: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )

    companion object {
        private val LAST_BACKUP_KEY = stringPreferencesKey("last_backup_timestamp")
        private val THEME_KEY = stringPreferencesKey("app_theme")
        private val LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        private val PUSH_ENABLED_KEY = booleanPreferencesKey("push_enabled")
        private val EMAIL_NOTIFICATIONS_KEY = booleanPreferencesKey("email_notifications")
        private val PROFILE_VISIBLE_KEY = booleanPreferencesKey("profile_visible")
        private val SHOW_LOCATION_KEY = booleanPreferencesKey("show_location")
        private val ALLOW_MESSAGES_KEY = booleanPreferencesKey("allow_messages")

        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadLastBackupTimestamp()
    }

    private fun loadLastBackupTimestamp() {
        viewModelScope.launch {
            try {
                val preferences = dataStore.data.first()
                val timestamp = preferences[LAST_BACKUP_KEY]
                _uiState.update { it.copy(lastBackupTimestamp = timestamp) }
            } catch (_: Exception) {
                // Ignore, no last backup timestamp
            }
        }
    }

    fun exportSettings(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, error = null, successMessage = null) }

            try {
                val preferences = dataStore.data.first()

                val now = Instant.now()
                val formatter = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                val timestamp = formatter.format(now)

                val backup = SettingsBackup(
                    version = 1,
                    exportedAt = timestamp,
                    theme = preferences[THEME_KEY] ?: "system",
                    language = preferences[LANGUAGE_KEY] ?: "en",
                    textScale = preferences[AccessibilitySettingsViewModel.TEXT_SCALE_KEY] ?: 1.0f,
                    reduceMotion = preferences[AccessibilitySettingsViewModel.REDUCE_MOTION_KEY] ?: false,
                    highContrast = preferences[AccessibilitySettingsViewModel.HIGH_CONTRAST_KEY] ?: false,
                    boldText = preferences[AccessibilitySettingsViewModel.BOLD_TEXT_KEY] ?: false,
                    notificationsEnabled = preferences[NOTIFICATIONS_ENABLED_KEY] ?: true,
                    pushEnabled = preferences[PUSH_ENABLED_KEY] ?: true,
                    emailNotifications = preferences[EMAIL_NOTIFICATIONS_KEY] ?: false,
                    profileVisible = preferences[PROFILE_VISIBLE_KEY] ?: true,
                    showLocation = preferences[SHOW_LOCATION_KEY] ?: true,
                    allowMessages = preferences[ALLOW_MESSAGES_KEY] ?: true
                )

                val jsonString = json.encodeToString(backup)

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException("Could not open output stream")

                // Save the backup timestamp
                dataStore.edit { prefs ->
                    prefs[LAST_BACKUP_KEY] = timestamp
                }

                _uiState.update {
                    it.copy(
                        isExporting = false,
                        lastBackupTimestamp = timestamp,
                        successMessage = "Settings exported successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        error = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun importSettings(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, error = null, successMessage = null) }

            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().readText()
                } ?: throw IllegalStateException("Could not open input stream")

                val backup = json.decodeFromString<SettingsBackup>(jsonString)

                // Validate backup version
                if (backup.version > 1) {
                    throw IllegalStateException("Unsupported backup version: ${backup.version}")
                }

                // Apply settings to DataStore
                dataStore.edit { preferences ->
                    preferences[THEME_KEY] = backup.theme
                    preferences[LANGUAGE_KEY] = backup.language
                    preferences[AccessibilitySettingsViewModel.TEXT_SCALE_KEY] = backup.textScale
                    preferences[AccessibilitySettingsViewModel.REDUCE_MOTION_KEY] = backup.reduceMotion
                    preferences[AccessibilitySettingsViewModel.HIGH_CONTRAST_KEY] = backup.highContrast
                    preferences[AccessibilitySettingsViewModel.BOLD_TEXT_KEY] = backup.boldText
                    preferences[NOTIFICATIONS_ENABLED_KEY] = backup.notificationsEnabled
                    preferences[PUSH_ENABLED_KEY] = backup.pushEnabled
                    preferences[EMAIL_NOTIFICATIONS_KEY] = backup.emailNotifications
                    preferences[PROFILE_VISIBLE_KEY] = backup.profileVisible
                    preferences[SHOW_LOCATION_KEY] = backup.showLocation
                    preferences[ALLOW_MESSAGES_KEY] = backup.allowMessages
                }

                _uiState.update {
                    it.copy(
                        isImporting = false,
                        successMessage = "Settings imported successfully from backup created on ${backup.exportedAt}"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        error = "Import failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissMessage() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
