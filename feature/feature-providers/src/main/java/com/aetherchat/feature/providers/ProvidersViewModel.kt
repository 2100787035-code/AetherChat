package com.aetherchat.feature.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.core.crypto.KeystoreEncryptor
import com.aetherchat.domain.model.ConnectionInfo
import com.aetherchat.domain.model.ModelInfo
import com.aetherchat.domain.model.PROVIDER_PRESETS
import com.aetherchat.domain.model.Provider
import com.aetherchat.domain.model.ProviderConfig
import com.aetherchat.domain.model.ProviderRepository
import com.aetherchat.domain.model.ProviderType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProvidersViewModel(
    private val providerRepository: ProviderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProvidersUiState())
    val uiState: StateFlow<ProvidersUiState> = _uiState.asStateFlow()

    init {
        loadProviders()
    }

    private fun loadProviders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                providerRepository.getAllProviders().collect { providers ->
                    val items = providers.map { provider ->
                        val preset = PROVIDER_PRESETS.find { it.type == provider.type }
                        ProviderItem(
                            provider = provider,
                            iconEmoji = preset?.iconEmoji ?: "⚙️",
                        )
                    }
                    _uiState.update { it.copy(providers = items, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun setProviderEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch {
            providerRepository.setProviderEnabled(id, enabled)
        }
    }

    fun deleteProvider(id: String) {
        viewModelScope.launch {
            providerRepository.deleteProvider(id)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class AddProviderViewModel(
    private val providerRepository: ProviderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddProviderUiState())
    val uiState: StateFlow<AddProviderUiState> = _uiState.asStateFlow()

    fun selectType(type: ProviderType) {
        val preset = PROVIDER_PRESETS.find { it.type == type }
        _uiState.update {
            it.copy(
                selectedType = type,
                baseUrl = preset?.defaultBaseUrl ?: "",
            )
        }
    }

    fun updateApiKey(key: String) {
        _uiState.update { it.copy(apiKey = key) }
    }

    fun updateBaseUrl(url: String) {
        _uiState.update { it.copy(baseUrl = url) }
    }

    fun testConnection() {
        val state = _uiState.value
        val type = state.selectedType ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, testResult = null) }
            val config = ProviderConfig(
                type = type,
                name = PROVIDER_PRESETS.find { it.type == type }?.displayName ?: "Custom",
                baseUrl = state.baseUrl,
                apiKey = state.apiKey,
            )
            val addResult = providerRepository.addProvider(config)
            if (addResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isTesting = false,
                        testResult = TestConnectionResult(
                            success = false,
                            errorMessage = addResult.exceptionOrNull()?.message,
                        ),
                    )
                }
                return@launch
            }
            val provider = addResult.getOrThrow()
            val testResult = providerRepository.testProviderConnection(provider.id)
            _uiState.update {
                it.copy(
                    isTesting = false,
                    testResult = testResult.getOrNull()?.let { info ->
                        TestConnectionResult(
                            success = true,
                            latencyMs = info.latencyMs,
                            modelCount = info.availableModelCount,
                        )
                    } ?: TestConnectionResult(
                        success = false,
                        errorMessage = testResult.exceptionOrNull()?.message,
                    ),
                    savedProviderId = provider.id,
                )
            }
            providerRepository.deleteProvider(provider.id)
        }
    }

    fun saveProvider() {
        val state = _uiState.value
        val type = state.selectedType ?: return
        val preset = PROVIDER_PRESETS.find { it.type == type }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = providerRepository.addProvider(
                ProviderConfig(
                    type = type,
                    name = preset?.displayName ?: "Custom",
                    baseUrl = state.baseUrl,
                    apiKey = state.apiKey,
                )
            )
            if (result.isSuccess) {
                val providerId = result.getOrThrow().id
                providerRepository.fetchModels(providerId)
                _uiState.update { it.copy(isSaving = false, savedProviderId = providerId) }
            } else {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.exceptionOrNull()?.message,
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class ProviderDetailViewModel(
    private val providerRepository: ProviderRepository,
    private val encryptor: KeystoreEncryptor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderDetailUiState())
    val uiState: StateFlow<ProviderDetailUiState> = _uiState.asStateFlow()

    private val _modelsState = MutableStateFlow(ModelsTabUiState())
    val modelsState: StateFlow<ModelsTabUiState> = _modelsState.asStateFlow()

    private val _addModelState = MutableStateFlow(AddModelUiState())
    val addModelState: StateFlow<AddModelUiState> = _addModelState.asStateFlow()

    private var currentProviderId: String = ""

    fun init(providerId: String) {
        currentProviderId = providerId
        loadProvider()
        loadModels()
    }

    private fun loadProvider() {
        viewModelScope.launch {
            val provider = providerRepository.getProviderById(currentProviderId)
            if (provider != null) {
                val preset = PROVIDER_PRESETS.find { it.type == provider.type }
                _uiState.update {
                    it.copy(
                        provider = provider,
                        iconEmoji = preset?.iconEmoji ?: "⚙️",
                    )
                }
            }
        }
    }

    private fun loadModels() {
        viewModelScope.launch {
            providerRepository.getEnabledModels(currentProviderId).collect { models ->
                _modelsState.update {
                    it.copy(autoModels = models.map { ModelItem(model = it) })
                }
            }
        }
    }

    fun selectTab(tab: DetailTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleApiKeyVisibility() {
        _uiState.update { it.copy(apiKeyVisible = !_uiState.value.apiKeyVisible) }
    }

    fun getDecryptedApiKey(): String {
        val encrypted = _uiState.value.provider?.apiKeyEncrypted ?: return ""
        return try {
            encryptor.decrypt(encrypted)
        } catch (_: Exception) {
            ""
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }
            val result = providerRepository.testProviderConnection(currentProviderId)
            _uiState.update {
                it.copy(
                    isTestingConnection = false,
                    connectionTestResult = result.getOrNull()?.let { info ->
                        TestConnectionResult(
                            success = true,
                            latencyMs = info.latencyMs,
                            modelCount = info.availableModelCount,
                        )
                    } ?: TestConnectionResult(
                        success = false,
                        errorMessage = result.exceptionOrNull()?.message,
                    ),
                )
            }
        }
    }

    fun fetchModels() {
        viewModelScope.launch {
            _modelsState.update { it.copy(isFetching = true) }
            val result = providerRepository.fetchModels(currentProviderId)
            if (result.isFailure) {
                _modelsState.update {
                    it.copy(
                        isFetching = false,
                        errorMessage = result.exceptionOrNull()?.message,
                    )
                }
            } else {
                _modelsState.update {
                    it.copy(
                        isFetching = false,
                        lastFetchTime = System.currentTimeMillis(),
                    )
                }
            }
        }
    }

    fun setModelEnabled(modelId: String, enabled: Boolean) {
        viewModelScope.launch {
            providerRepository.setModelEnabled(currentProviderId, modelId, enabled)
        }
    }

    fun testModel(modelId: String) {
        viewModelScope.launch {
            val currentModels = _modelsState.value.autoModels.toMutableList()
            val index = currentModels.indexOfFirst { it.model.id == modelId }
            if (index >= 0) {
                currentModels[index] = currentModels[index].copy(isTesting = true)
                _modelsState.update { it.copy(autoModels = currentModels) }
            }
            val result = providerRepository.testModelConnection(currentProviderId, modelId)
            if (index >= 0) {
                val updatedModels = _modelsState.value.autoModels.toMutableList()
                updatedModels[index] = updatedModels[index].copy(
                    isTesting = false,
                    testResult = result.getOrNull()?.let { testResult ->
                        ModelTestDisplay(
                            success = testResult.success,
                            latencyMs = testResult.latencyMs,
                            errorMessage = testResult.errorMessage,
                        )
                    } ?: ModelTestDisplay(
                        success = false,
                        errorMessage = result.exceptionOrNull()?.message,
                    ),
                )
                _modelsState.update { it.copy(autoModels = updatedModels) }
            }
        }
    }

    fun updateAddModelState(state: AddModelUiState) {
        _addModelState.update { state }
    }

    fun addCustomModel() {
        val state = _addModelState.value
        if (state.modelId.isBlank()) return
        viewModelScope.launch {
            _addModelState.update { it.copy(isSaving = true) }
            val result = providerRepository.addCustomModel(
                currentProviderId,
                ModelInfo(
                    id = state.modelId,
                    providerId = currentProviderId,
                    displayName = state.displayName.ifBlank { state.modelId },
                    contextWindow = state.contextWindow.toIntOrNull(),
                    supportVision = state.supportVision,
                    supportFunctionCall = state.supportFunctionCall,
                    isCustom = true,
                ),
            )
            _addModelState.update {
                it.copy(
                    isSaving = false,
                    errorMessage = if (result.isFailure) result.exceptionOrNull()?.message else null,
                )
            }
            if (result.isSuccess) {
                _addModelState.update { AddModelUiState() }
            }
        }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            providerRepository.deleteModel(currentProviderId, modelId)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
        _modelsState.update { it.copy(errorMessage = null) }
    }
}
