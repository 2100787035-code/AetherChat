package com.aetherchat.feature.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        _uiState.update { it.copy(dynamicColorEnabled = enabled) }
    }

    fun updateFontSize(level: FontSizeLevel) {
        _uiState.update { it.copy(fontSizeLevel = level) }
    }

    fun updateTtsService(service: String) {
        _uiState.update { it.copy(ttsService = service) }
    }

    fun updateStreamEnabled(enabled: Boolean) {
        _uiState.update { it.copy(streamEnabled = enabled) }
    }
}
