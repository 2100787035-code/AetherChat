package com.aetherchat.feature.tts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.domain.model.AudioFormat
import com.aetherchat.domain.model.TTSConfig
import com.aetherchat.domain.model.TTSProvider
import com.aetherchat.domain.model.VoiceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TtsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TtsUiState())
    val uiState: StateFlow<TtsUiState> = _uiState.asStateFlow()

    fun selectService(service: TtsServiceType) {
        _uiState.update { it.copy(selectedService = service, voices = emptyList()) }
        loadVoices()
    }

    fun selectVoice(voice: String) {
        _uiState.update { it.copy(selectedVoice = voice) }
    }

    fun updateSpeed(speed: Float) {
        _uiState.update { it.copy(speed = speed) }
    }

    fun playText(text: String) {
        if (text.isBlank()) return
        _uiState.update { it.copy(currentText = text, isPlaying = true, isPaused = false) }
    }

    fun pausePlayback() {
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumePlayback() {
        _uiState.update { it.copy(isPaused = false) }
    }

    fun stopPlayback() {
        _uiState.update { it.copy(isPlaying = false, isPaused = false, currentText = "") }
    }

    private fun loadVoices() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val defaultVoices = listOf(
                VoiceInfo("alloy", "Alloy", "en", null),
                VoiceInfo("echo", "Echo", "en", null),
                VoiceInfo("fable", "Fable", "en", null),
                VoiceInfo("onyx", "Onyx", "en", null),
                VoiceInfo("nova", "Nova", "en", null),
                VoiceInfo("shimmer", "Shimmer", "en", null),
            )
            _uiState.update { it.copy(voices = defaultVoices, isLoading = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
