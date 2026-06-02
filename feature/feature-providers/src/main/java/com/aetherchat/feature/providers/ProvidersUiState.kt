package com.aetherchat.feature.providers

import com.aetherchat.domain.model.ModelInfo
import com.aetherchat.domain.model.Provider
import com.aetherchat.domain.model.ProviderPreset
import com.aetherchat.domain.model.ProviderType

data class ProvidersUiState(
    val providers: List<ProviderItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class ProviderItem(
    val provider: Provider,
    val enabledModelCount: Int = 0,
    val iconEmoji: String = "⚙️",
)

data class AddProviderUiState(
    val selectedType: ProviderType? = null,
    val apiKey: String = "",
    val baseUrl: String = "",
    val isTesting: Boolean = false,
    val testResult: TestConnectionResult? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

data class TestConnectionResult(
    val success: Boolean,
    val latencyMs: Long? = null,
    val modelCount: Int? = null,
    val errorMessage: String? = null,
)

data class ProviderDetailUiState(
    val provider: Provider? = null,
    val selectedTab: DetailTab = DetailTab.BASIC,
    val apiKeyVisible: Boolean = false,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: TestConnectionResult? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

enum class DetailTab { BASIC, MODELS, ADVANCED }

data class ModelsTabUiState(
    val autoModels: List<ModelItem> = emptyList(),
    val customModels: List<ModelItem> = emptyList(),
    val isFetching: Boolean = false,
    val lastFetchTime: Long? = null,
    val errorMessage: String? = null,
)

data class ModelItem(
    val model: ModelInfo,
    val isTesting: Boolean = false,
    val testResult: ModelTestDisplay? = null,
)

data class ModelTestDisplay(
    val success: Boolean,
    val latencyMs: Long? = null,
    val errorMessage: String? = null,
)

data class AddModelUiState(
    val modelId: String = "",
    val displayName: String = "",
    val contextWindow: String = "",
    val supportVision: Boolean = false,
    val supportFunctionCall: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

data class AdvancedTabUiState(
    val apiKeys: List<String> = emptyList(),
    val timeoutSeconds: String = "60",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
