package com.aetherchat.feature.assistants

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class AssistantsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantsUiState())
    val uiState: StateFlow<AssistantsUiState> = _uiState.asStateFlow()

    fun deleteAssistant(id: String) {
        _uiState.update { state ->
            state.copy(assistants = state.assistants.filter { it.id != id })
        }
    }
}

class CreateAssistantViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateAssistantUiState())
    val uiState: StateFlow<CreateAssistantUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateIconEmoji(emoji: String) {
        _uiState.update { it.copy(iconEmoji = emoji) }
    }

    fun updateSystemPrompt(prompt: String) {
        _uiState.update { it.copy(systemPrompt = prompt) }
    }

    fun updateTemperature(temp: String) {
        _uiState.update { it.copy(temperature = temp) }
    }

    fun createAssistant(): Assistant {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        return Assistant(
            id = UUID.randomUUID().toString(),
            name = state.name.ifBlank { "新助手" },
            iconEmoji = state.iconEmoji,
            systemPrompt = state.systemPrompt,
            providerId = state.providerId,
            modelId = state.modelId,
            temperature = state.temperature.toFloatOrNull() ?: 0.7f,
            createdAt = now,
            updatedAt = now,
        )
    }

    fun loadPreset(preset: PresetAssistant) {
        _uiState.update {
            it.copy(
                name = preset.name,
                iconEmoji = preset.iconEmoji,
                systemPrompt = preset.systemPrompt,
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
