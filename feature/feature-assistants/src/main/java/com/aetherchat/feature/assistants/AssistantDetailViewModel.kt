package com.aetherchat.feature.assistants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.local.AssistantEntity
import com.aetherchat.core.data.local.ModelEntity
import com.aetherchat.core.data.local.ProviderEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AssistantDetailViewModel(
    private val database: AetherChatDatabase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantDetailUiState())
    val uiState: StateFlow<AssistantDetailUiState> = _uiState.asStateFlow()

    private var currentAssistantId: String = ""

    fun loadAssistant(id: String) {
        currentAssistantId = id
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val assistant = database.assistantDao().getById(id)
            if (assistant != null) {
                _uiState.update {
                    it.copy(
                        assistant = Assistant(
                            id = assistant.id,
                            name = assistant.name,
                            iconEmoji = assistant.iconEmoji,
                            systemPrompt = assistant.systemPrompt,
                            providerId = assistant.providerId,
                            modelId = assistant.modelId,
                            temperature = assistant.temperature,
                            createdAt = assistant.createdAt,
                            updatedAt = assistant.updatedAt,
                        ),
                        name = assistant.name,
                        iconEmoji = assistant.iconEmoji,
                        systemPrompt = assistant.systemPrompt,
                        providerId = assistant.providerId,
                        modelId = assistant.modelId,
                        temperature = assistant.temperature,
                        isLoading = false,
                    )
                }
                loadProviders()
                val providerId = assistant.providerId
                if (providerId != null) {
                    loadModels(providerId)
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "助手不存在") }
            }
        }
    }

    private suspend fun loadProviders() {
        val providers = database.providerDao().getEnabled().first()
        _uiState.update {
            it.copy(
                availableProviders = providers.map { entity ->
                    ProviderOption(entity.id, entity.name)
                },
            )
        }
        val currentProviderId = _uiState.value.providerId
        if (currentProviderId != null) {
            val provider = providers.find { it.id == currentProviderId }
            _uiState.update { it.copy(selectedProviderName = provider?.name) }
        }
    }

    private suspend fun loadModels(providerId: String) {
        val models = database.modelDao().getEnabledByProviderId(providerId).first()
        _uiState.update {
            it.copy(
                availableModels = models.map { entity ->
                    ModelOption(entity.id, entity.displayName, entity.contextWindow)
                },
            )
        }
        val currentModelId = _uiState.value.modelId
        if (currentModelId != null) {
            val model = models.find { it.id == currentModelId }
            _uiState.update { it.copy(selectedModelName = model?.displayName) }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateIconEmoji(emoji: String) {
        _uiState.update { it.copy(iconEmoji = emoji) }
    }

    fun updateSystemPrompt(prompt: String) {
        _uiState.update { it.copy(systemPrompt = prompt) }
    }

    fun updateTemperature(temp: Float) {
        _uiState.update { it.copy(temperature = temp) }
    }

    fun showProviderPicker() {
        _uiState.update { it.copy(showProviderPicker = true) }
    }

    fun hideProviderPicker() {
        _uiState.update { it.copy(showProviderPicker = false) }
    }

    fun selectProvider(id: String?, name: String?) {
        _uiState.update {
            it.copy(
                providerId = id,
                selectedProviderName = name,
                showProviderPicker = false,
                modelId = null,
                selectedModelName = null,
                availableModels = emptyList(),
            )
        }
        if (id != null) {
            viewModelScope.launch {
                loadModels(id)
            }
        }
    }

    fun showModelPicker() {
        _uiState.update { it.copy(showModelPicker = true) }
    }

    fun hideModelPicker() {
        _uiState.update { it.copy(showModelPicker = false) }
    }

    fun selectModel(id: String?, name: String?) {
        _uiState.update {
            it.copy(
                modelId = id,
                selectedModelName = name,
                showModelPicker = false,
            )
        }
    }

    fun saveAssistant() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            database.assistantDao().update(
                AssistantEntity(
                    id = currentAssistantId,
                    name = state.name,
                    iconEmoji = state.iconEmoji,
                    systemPrompt = state.systemPrompt,
                    providerId = state.providerId,
                    modelId = state.modelId,
                    temperature = state.temperature,
                    createdAt = state.assistant?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    fun deleteAssistant() {
        viewModelScope.launch {
            val entity = database.assistantDao().getById(currentAssistantId)
            if (entity != null) {
                database.assistantDao().delete(entity)
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class AssistantDetailUiState(
    val assistant: Assistant? = null,
    val isLoading: Boolean = false,
    val name: String = "",
    val iconEmoji: String = "🤖",
    val systemPrompt: String = "",
    val providerId: String? = null,
    val modelId: String? = null,
    val temperature: Float = 0.7f,
    val selectedProviderName: String? = null,
    val selectedModelName: String? = null,
    val availableProviders: List<ProviderOption> = emptyList(),
    val availableModels: List<ModelOption> = emptyList(),
    val showProviderPicker: Boolean = false,
    val showModelPicker: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)