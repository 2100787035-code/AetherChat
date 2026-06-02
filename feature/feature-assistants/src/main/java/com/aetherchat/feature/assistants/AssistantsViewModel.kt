package com.aetherchat.feature.assistants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.local.AssistantEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AssistantsViewModel(
    private val database: AetherChatDatabase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantsUiState())
    val uiState: StateFlow<AssistantsUiState> = _uiState.asStateFlow()

    init {
        loadAssistants()
    }

    private fun loadAssistants() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            database.assistantDao().getAll().collect { entities ->
                _uiState.update {
                    it.copy(
                        assistants = entities.map { entity ->
                            Assistant(
                                id = entity.id,
                                name = entity.name,
                                iconEmoji = entity.iconEmoji,
                                systemPrompt = entity.systemPrompt,
                                providerId = entity.providerId,
                                modelId = entity.modelId,
                                temperature = entity.temperature,
                                createdAt = entity.createdAt,
                                updatedAt = entity.updatedAt,
                            )
                        },
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun deleteAssistant(id: String) {
        viewModelScope.launch {
            val entity = database.assistantDao().getById(id) ?: return@launch
            database.assistantDao().delete(entity)
        }
    }
}

class CreateAssistantViewModel(
    private val database: AetherChatDatabase,
) : ViewModel() {

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

    fun updateProviderId(id: String?) {
        _uiState.update { it.copy(providerId = id) }
    }

    fun updateModelId(id: String?) {
        _uiState.update { it.copy(modelId = id) }
    }

    fun createAssistant() {
        val state = _uiState.value
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            database.assistantDao().insert(
                AssistantEntity(
                    id = id,
                    name = state.name.ifBlank { "新助手" },
                    iconEmoji = state.iconEmoji,
                    systemPrompt = state.systemPrompt,
                    providerId = state.providerId,
                    modelId = state.modelId,
                    temperature = state.temperature.toFloatOrNull() ?: 0.7f,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            _uiState.update { it.copy(isSaving = false) }
        }
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
