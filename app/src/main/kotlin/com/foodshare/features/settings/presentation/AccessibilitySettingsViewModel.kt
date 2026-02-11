package com.foodshare.features.settings.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Accessibility Settings screen
 *
 * Manages accessibility preferences persisted via DataStore,
 * including text scale, reduce motion, high contrast, and bold text.
 */
@HiltViewModel
class AccessibilitySettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    data class UiState(
        val textScale: Float = 1.0f,
        val reduceMotion: Boolean = false,
        val highContrast: Boolean = false,
        val boldText: Boolean = false,
        val isLoading: Boolean = true
    )

    companion object {
        val TEXT_SCALE_KEY = floatPreferencesKey("accessibility_text_scale")
        val REDUCE_MOTION_KEY = booleanPreferencesKey("accessibility_reduce_motion")
        val HIGH_CONTRAST_KEY = booleanPreferencesKey("accessibility_high_contrast")
        val BOLD_TEXT_KEY = booleanPreferencesKey("accessibility_bold_text")
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val preferences = dataStore.data.first()

                _uiState.update {
                    it.copy(
                        textScale = preferences[TEXT_SCALE_KEY] ?: 1.0f,
                        reduceMotion = preferences[REDUCE_MOTION_KEY] ?: false,
                        highContrast = preferences[HIGH_CONTRAST_KEY] ?: false,
                        boldText = preferences[BOLD_TEXT_KEY] ?: false,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateTextScale(scale: Float) {
        // Round to nearest 0.1
        val rounded = (Math.round(scale * 10.0) / 10.0).toFloat()
        _uiState.update { it.copy(textScale = rounded) }
        savePreference(TEXT_SCALE_KEY, rounded)
    }

    fun toggleReduceMotion(enabled: Boolean) {
        _uiState.update { it.copy(reduceMotion = enabled) }
        savePreference(REDUCE_MOTION_KEY, enabled)
    }

    fun toggleHighContrast(enabled: Boolean) {
        _uiState.update { it.copy(highContrast = enabled) }
        savePreference(HIGH_CONTRAST_KEY, enabled)
    }

    fun toggleBoldText(enabled: Boolean) {
        _uiState.update { it.copy(boldText = enabled) }
        savePreference(BOLD_TEXT_KEY, enabled)
    }

    private fun savePreference(key: Preferences.Key<Float>, value: Float) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    private fun savePreference(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }
}
