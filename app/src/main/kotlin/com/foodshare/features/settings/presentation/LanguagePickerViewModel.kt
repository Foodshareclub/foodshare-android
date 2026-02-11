package com.foodshare.features.settings.presentation

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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
private val Context.languageDataStore by preferencesDataStore(name = "language_settings")

/**
 * Preference keys for language settings
 */
private object LanguageKeys {
    val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
}

/**
 * ViewModel for Language Picker screen
 */
@HiltViewModel
class LanguagePickerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguagePickerUiState())
    val uiState: StateFlow<LanguagePickerUiState> = _uiState.asStateFlow()

    init {
        loadSelectedLanguage()
    }

    private fun loadSelectedLanguage() {
        viewModelScope.launch {
            val prefs = context.languageDataStore.data.first()
            val languageCode = prefs[LanguageKeys.SELECTED_LANGUAGE] ?: "en"

            _uiState.update {
                it.copy(
                    selectedLanguage = languageCode,
                    isLoading = false
                )
            }
        }
    }

    fun selectLanguage(languageCode: String) {
        _uiState.update { it.copy(selectedLanguage = languageCode) }
        saveLanguage(languageCode)
    }

    private fun saveLanguage(languageCode: String) {
        viewModelScope.launch {
            context.languageDataStore.edit { prefs ->
                prefs[LanguageKeys.SELECTED_LANGUAGE] = languageCode
            }
        }
    }
}

/**
 * UI state for Language Picker screen
 */
data class LanguagePickerUiState(
    val selectedLanguage: String = "en",
    val languages: List<Language> = supportedLanguages,
    val isLoading: Boolean = true
)

/**
 * Language model
 */
data class Language(
    val code: String,
    val name: String,
    val nativeName: String
)

/**
 * List of supported languages (21 languages)
 */
val supportedLanguages = listOf(
    Language("en", "English", "English"),
    Language("es", "Spanish", "Español"),
    Language("fr", "French", "Français"),
    Language("de", "German", "Deutsch"),
    Language("it", "Italian", "Italiano"),
    Language("pt", "Portuguese", "Português"),
    Language("ru", "Russian", "Русский"),
    Language("zh", "Chinese", "中文"),
    Language("ja", "Japanese", "日本語"),
    Language("ko", "Korean", "한국어"),
    Language("ar", "Arabic", "العربية"),
    Language("hi", "Hindi", "हिन्दी"),
    Language("tr", "Turkish", "Türkçe"),
    Language("pl", "Polish", "Polski"),
    Language("nl", "Dutch", "Nederlands"),
    Language("sv", "Swedish", "Svenska"),
    Language("da", "Danish", "Dansk"),
    Language("fi", "Finnish", "Suomi"),
    Language("no", "Norwegian", "Norsk"),
    Language("cs", "Czech", "Čeština"),
    Language("th", "Thai", "ไทย")
)
