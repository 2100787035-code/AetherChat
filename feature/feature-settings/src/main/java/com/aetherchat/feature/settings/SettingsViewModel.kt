package com.aetherchat.feature.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.core.crypto.KeystoreEncryptor
import com.aetherchat.core.data.export.DataExporter
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.sync.SyncManager
import com.aetherchat.core.data.sync.WebDavConfig
import com.aetherchat.core.data.sync.WebDavService
import com.aetherchat.domain.model.Conversation
import com.aetherchat.domain.model.Message
import com.aetherchat.domain.model.ModelInfo
import com.aetherchat.domain.model.Provider
import com.aetherchat.domain.model.ProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val providerRepository: ProviderRepository,
    private val database: AetherChatDatabase,
    private val encryptor: KeystoreEncryptor,
    private val dataExporter: DataExporter,
    private val prefs: SharedPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadProviders()
    }

    private fun loadSettings() {
        _uiState.update { state ->
            state.copy(
                defaultProviderId = prefs.getString("default_provider_id", "") ?: "",
                defaultModelId = prefs.getString("default_model_id", "") ?: "",
                webSearchEnabled = prefs.getBoolean("web_search_enabled", false),
                webSearchEngine = prefs.getString("web_search_engine", "searxng") ?: "searxng",
                searxngUrl = prefs.getString("searxng_url", "") ?: "",
                sttEnabled = prefs.getBoolean("stt_enabled", false),
                sttProvider = prefs.getString("stt_provider", "openai_whisper") ?: "openai_whisper",
                sttApiKey = decryptOrNull(prefs.getString("stt_api_key", null)),
                sttBaseUrl = prefs.getString("stt_base_url", "https://api.openai.com/v1") ?: "https://api.openai.com/v1",
                sttModel = prefs.getString("stt_model", "whisper-1") ?: "whisper-1",
                ttsEnabled = prefs.getBoolean("tts_enabled", false),
                ttsProvider = prefs.getString("tts_provider", "openai_tts") ?: "openai_tts",
                ttsApiKey = decryptOrNull(prefs.getString("tts_api_key", null)),
                ttsBaseUrl = prefs.getString("tts_base_url", "https://api.openai.com/v1") ?: "https://api.openai.com/v1",
                ttsVoice = prefs.getString("tts_voice", "alloy") ?: "alloy",
                ttsSpeed = prefs.getFloat("tts_speed", 1.0f),
                webdavEnabled = prefs.getBoolean("webdav_enabled", false),
                webdavServerUrl = prefs.getString("webdav_server_url", "") ?: "",
                webdavUsername = prefs.getString("webdav_username", "") ?: "",
                webdavPassword = decryptOrNull(prefs.getString("webdav_password", null)),
                webdavRemotePath = prefs.getString("webdav_remote_path", "/AetherChat/") ?: "/AetherChat/",
                webdavAutoSync = prefs.getBoolean("webdav_auto_sync", false),
                webdavSyncIntervalMinutes = prefs.getInt("webdav_sync_interval_minutes", 30),
                themeMode = prefs.getString("theme_mode", "system") ?: "system",
                dynamicColorEnabled = prefs.getBoolean("dynamic_color_enabled", true),
                fontSize = prefs.getInt("font_size", 16),
                sendOnEnter = prefs.getBoolean("send_on_enter", true),
                showTokenCount = prefs.getBoolean("show_token_count", true),
                streamResponse = prefs.getBoolean("stream_response", true),
            )
        }
    }

    private fun loadProviders() {
        viewModelScope.launch {
            try {
                val entities = database.providerDao().getAll().first()
                val providers = entities.map { entity ->
                    ProviderInfo(
                        id = entity.id,
                        name = entity.name,
                        type = entity.type.name,
                        isEnabled = entity.isEnabled,
                        modelCount = 0,
                    )
                }
                _uiState.update { it.copy(availableProviders = providers) }
                val currentProviderId = _uiState.value.defaultProviderId
                if (currentProviderId.isNotEmpty()) {
                    loadModelsForProvider(currentProviderId)
                }
            } catch (_: Exception) {
            }
        }
    }

    private suspend fun loadModelsForProvider(providerId: String) {
        try {
            val models = database.modelDao().getByProviderId(providerId).first().map { entity ->
                ModelInfo(
                    id = entity.id,
                    providerId = entity.providerId,
                    displayName = entity.displayName,
                    contextWindow = entity.contextWindow,
                    supportVision = entity.supportVision,
                    supportFunctionCall = entity.supportFunctionCall,
                    isEnabled = entity.isEnabled,
                    isCustom = entity.isCustom,
                    lastTestedAt = entity.lastTestedAt,
                    lastTestResult = entity.lastTestResult,
                )
            }
            _uiState.update { it.copy(availableModels = models) }
        } catch (_: Exception) {
        }
    }

    private fun decryptOrNull(encrypted: String?): String {
        if (encrypted.isNullOrEmpty()) return ""
        return try {
            encryptor.decrypt(encrypted)
        } catch (_: Exception) {
            ""
        }
    }

    private fun encryptOrEmpty(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        return try {
            encryptor.encrypt(plaintext)
        } catch (_: Exception) {
            ""
        }
    }

    private fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    private fun putEncrypted(key: String, value: String) {
        prefs.edit().putString(key, encryptOrEmpty(value)).apply()
    }

    fun updateDefaultProvider(id: String) {
        putString("default_provider_id", id)
        _uiState.update { it.copy(defaultProviderId = id, defaultModelId = "", availableModels = emptyList()) }
        viewModelScope.launch { loadModelsForProvider(id) }
    }

    fun updateDefaultModel(id: String) {
        putString("default_model_id", id)
        _uiState.update { it.copy(defaultModelId = id) }
    }

    fun updateWebSearchEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("web_search_enabled", enabled).apply()
        _uiState.update { it.copy(webSearchEnabled = enabled) }
    }

    fun updateWebSearchEngine(engine: String) {
        putString("web_search_engine", engine)
        _uiState.update { it.copy(webSearchEngine = engine) }
    }

    fun updateSearxngUrl(url: String) {
        putString("searxng_url", url)
        _uiState.update { it.copy(searxngUrl = url) }
    }

    fun updateSttEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("stt_enabled", enabled).apply()
        _uiState.update { it.copy(sttEnabled = enabled) }
    }

    fun updateSttProvider(provider: String) {
        putString("stt_provider", provider)
        _uiState.update { it.copy(sttProvider = provider) }
    }

    fun updateSttApiKey(key: String) {
        putEncrypted("stt_api_key", key)
        _uiState.update { it.copy(sttApiKey = key) }
    }

    fun updateSttBaseUrl(url: String) {
        putString("stt_base_url", url)
        _uiState.update { it.copy(sttBaseUrl = url) }
    }

    fun updateSttModel(model: String) {
        putString("stt_model", model)
        _uiState.update { it.copy(sttModel = model) }
    }

    fun updateTtsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("tts_enabled", enabled).apply()
        _uiState.update { it.copy(ttsEnabled = enabled) }
    }

    fun updateTtsProvider(provider: String) {
        putString("tts_provider", provider)
        _uiState.update { it.copy(ttsProvider = provider) }
    }

    fun updateTtsApiKey(key: String) {
        putEncrypted("tts_api_key", key)
        _uiState.update { it.copy(ttsApiKey = key) }
    }

    fun updateTtsBaseUrl(url: String) {
        putString("tts_base_url", url)
        _uiState.update { it.copy(ttsBaseUrl = url) }
    }

    fun updateTtsVoice(voice: String) {
        putString("tts_voice", voice)
        _uiState.update { it.copy(ttsVoice = voice) }
    }

    fun updateTtsSpeed(speed: Float) {
        prefs.edit().putFloat("tts_speed", speed).apply()
        _uiState.update { it.copy(ttsSpeed = speed) }
    }

    fun updateWebdavEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("webdav_enabled", enabled).apply()
        _uiState.update { it.copy(webdavEnabled = enabled) }
    }

    fun updateWebdavServerUrl(url: String) {
        putString("webdav_server_url", url)
        _uiState.update { it.copy(webdavServerUrl = url) }
    }

    fun updateWebdavUsername(username: String) {
        putString("webdav_username", username)
        _uiState.update { it.copy(webdavUsername = username) }
    }

    fun updateWebdavPassword(password: String) {
        putEncrypted("webdav_password", password)
        _uiState.update { it.copy(webdavPassword = password) }
    }

    fun updateWebdavRemotePath(path: String) {
        putString("webdav_remote_path", path)
        _uiState.update { it.copy(webdavRemotePath = path) }
    }

    fun updateWebdavAutoSync(enabled: Boolean) {
        prefs.edit().putBoolean("webdav_auto_sync", enabled).apply()
        _uiState.update { it.copy(webdavAutoSync = enabled) }
    }

    fun updateWebdavSyncInterval(minutes: Int) {
        prefs.edit().putInt("webdav_sync_interval_minutes", minutes).apply()
        _uiState.update { it.copy(webdavSyncIntervalMinutes = minutes) }
    }

    fun updateThemeMode(mode: String) {
        putString("theme_mode", mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun updateDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean("dynamic_color_enabled", enabled).apply()
        _uiState.update { it.copy(dynamicColorEnabled = enabled) }
    }

    fun updateFontSize(size: Int) {
        prefs.edit().putInt("font_size", size).apply()
        _uiState.update { it.copy(fontSize = size) }
    }

    fun updateSendOnEnter(enabled: Boolean) {
        prefs.edit().putBoolean("send_on_enter", enabled).apply()
        _uiState.update { it.copy(sendOnEnter = enabled) }
    }

    fun updateShowTokenCount(enabled: Boolean) {
        prefs.edit().putBoolean("show_token_count", enabled).apply()
        _uiState.update { it.copy(showTokenCount = enabled) }
    }

    fun updateStreamResponse(enabled: Boolean) {
        prefs.edit().putBoolean("stream_response", enabled).apply()
        _uiState.update { it.copy(streamResponse = enabled) }
    }

    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, errorMessage = null) }
            try {
                val syncManager = createSyncManager()
                val providers = collectProviders()
                val modelsMap = collectModelsMap(providers)
                val conversations = collectConversations()
                val messagesMap = collectMessagesMap(conversations)
                val result = syncManager.syncToRemote(providers, modelsMap, conversations, messagesMap)
                if (result.isSuccess) {
                    _uiState.update { it.copy(isSyncing = false, successMessage = "同步成功") }
                } else {
                    _uiState.update {
                        it.copy(
                            isSyncing = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "同步失败",
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSyncing = false, errorMessage = e.message ?: "同步失败") }
            }
        }
    }

    fun testWebdavConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val syncManager = createSyncManager()
                val result = syncManager.testConnection()
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, successMessage = "连接成功") }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.exceptionOrNull()?.message ?: "连接失败",
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "连接失败") }
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val providers = collectProviders()
                val modelsMap = collectModelsMap(providers)
                val conversations = collectConversations()
                val messagesMap = collectMessagesMap(conversations)
                dataExporter.exportToJson(providers, modelsMap, conversations, messagesMap)
                _uiState.update { it.copy(isLoading = false, successMessage = "导出成功") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "导出失败") }
            }
        }
    }

    private fun createSyncManager(): SyncManager {
        val state = _uiState.value
        val config = WebDavConfig(
            serverUrl = state.webdavServerUrl,
            username = state.webdavUsername,
            password = state.webdavPassword,
            remotePath = state.webdavRemotePath,
        )
        return SyncManager(WebDavService(config), dataExporter)
    }

    private suspend fun collectProviders(): List<Provider> {
        return database.providerDao().getAll().first().map { entity ->
            Provider(
                id = entity.id,
                name = entity.name,
                type = entity.type,
                baseUrl = entity.baseUrl,
                apiKeyEncrypted = entity.apiKeyEncrypted,
                isEnabled = entity.isEnabled,
                sortOrder = entity.sortOrder,
            )
        }
    }

    private suspend fun collectModelsMap(providers: List<Provider>): Map<String, List<ModelInfo>> {
        val map = mutableMapOf<String, List<ModelInfo>>()
        for (provider in providers) {
            val models = database.modelDao().getByProviderId(provider.id).first().map { entity ->
                ModelInfo(
                    id = entity.id,
                    providerId = entity.providerId,
                    displayName = entity.displayName,
                    contextWindow = entity.contextWindow,
                    supportVision = entity.supportVision,
                    supportFunctionCall = entity.supportFunctionCall,
                    isEnabled = entity.isEnabled,
                    isCustom = entity.isCustom,
                    lastTestedAt = entity.lastTestedAt,
                    lastTestResult = entity.lastTestResult,
                )
            }
            map[provider.id] = models
        }
        return map
    }

    private suspend fun collectConversations(): List<Conversation> {
        return database.conversationDao().getAll().first().map { entity ->
            Conversation(
                id = entity.id,
                title = entity.title,
                assistantId = entity.assistantId,
                providerId = entity.providerId,
                modelId = entity.modelId,
                systemPrompt = entity.systemPrompt,
                tags = entity.tags,
                isPinned = entity.isPinned,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
        }
    }

    private suspend fun collectMessagesMap(conversations: List<Conversation>): Map<String, List<Message>> {
        val map = mutableMapOf<String, List<Message>>()
        for (conv in conversations) {
            val msgs = database.messageDao().getByConversationId(conv.id).first().map { entity ->
                Message(
                    id = entity.id,
                    conversationId = entity.conversationId,
                    parentId = entity.parentId,
                    role = entity.role,
                    content = entity.content,
                    modelId = entity.modelId,
                    providerId = entity.providerId,
                    inputTokens = entity.inputTokens,
                    outputTokens = entity.outputTokens,
                    createdAt = entity.createdAt,
                    status = entity.status,
                )
            }
            map[conv.id] = msgs
        }
        return map
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
