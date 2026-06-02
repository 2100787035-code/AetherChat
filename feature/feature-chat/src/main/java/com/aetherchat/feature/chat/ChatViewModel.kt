package com.aetherchat.feature.chat

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.core.crypto.KeystoreEncryptor
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.local.ConversationEntity
import com.aetherchat.core.data.local.MessageEntity
import com.aetherchat.core.network.provider.OpenAICompatProvider
import com.aetherchat.domain.model.ChatMessage
import com.aetherchat.domain.model.ChatRequest
import com.aetherchat.domain.model.ChatStreamEvent
import com.aetherchat.domain.model.ContentBlock
import com.aetherchat.domain.model.MessageStatus
import com.aetherchat.domain.model.Role
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val database: AetherChatDatabase,
    private val encryptor: KeystoreEncryptor,
    private val prefs: SharedPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null

    fun loadConversation(conversationId: String) {
        streamingJob?.cancel()
        _uiState.update { ChatUiState() }

        viewModelScope.launch {
            loadProviders()

            if (conversationId.isNotBlank()) {
                val conversation = database.conversationDao().getById(conversationId)
                if (conversation != null) {
                    _uiState.update {
                        it.copy(
                            conversationId = conversation.id,
                            title = conversation.title,
                            providerId = conversation.providerId,
                            modelId = conversation.modelId,
                        )
                    }
                    loadModels(conversation.providerId)
                    loadMessages(conversationId)
                }
            } else {
                val defaultProviderId = prefs.getString("default_provider_id", "") ?: ""
                val defaultModelId = prefs.getString("default_model_id", "") ?: ""
                val webSearchOn = prefs.getBoolean("web_search_enabled", false)
                val sttOn = prefs.getBoolean("stt_enabled", false)

                val providerId = if (defaultProviderId.isNotEmpty()) {
                    val exists = _uiState.value.availableProviders.any { it.id == defaultProviderId }
                    if (exists) defaultProviderId else ""
                } else ""

                if (providerId.isNotEmpty()) {
                    _uiState.update { it.copy(providerId = providerId) }
                    loadModels(providerId)
                    val modelId = if (defaultModelId.isNotEmpty()) {
                        val modelExists = _uiState.value.availableModels.any { it.id == defaultModelId }
                        if (modelExists) defaultModelId else ""
                    } else ""
                    if (modelId.isNotEmpty()) {
                        _uiState.update { it.copy(modelId = modelId) }
                    } else {
                        val firstModel = _uiState.value.availableModels.firstOrNull()
                        if (firstModel != null) {
                            _uiState.update { it.copy(modelId = firstModel.id) }
                        }
                    }
                } else {
                    val firstProvider = _uiState.value.availableProviders.firstOrNull()
                    if (firstProvider != null) {
                        _uiState.update { it.copy(providerId = firstProvider.id) }
                        loadModels(firstProvider.id)
                        val firstModel = _uiState.value.availableModels.firstOrNull()
                        if (firstModel != null) {
                            _uiState.update { it.copy(modelId = firstModel.id) }
                        }
                    }
                }

                _uiState.update {
                    it.copy(
                        webSearchEnabled = webSearchOn,
                        sttEnabled = sttOn,
                    )
                }
            }
        }
    }

    private suspend fun loadProviders() {
        val providers = database.providerDao().getEnabled().first()
        _uiState.update {
            it.copy(
                availableProviders = providers.map { entity ->
                    ProviderOption(
                        id = entity.id,
                        name = entity.name,
                        type = entity.type.name,
                    )
                }
            )
        }
    }

    private suspend fun loadModels(providerId: String) {
        val models = database.modelDao().getEnabledByProviderId(providerId).first()
        _uiState.update {
            it.copy(
                availableModels = models.map { entity ->
                    ModelOption(
                        id = entity.id,
                        displayName = entity.displayName,
                        contextWindow = entity.contextWindow,
                    )
                }
            )
        }
    }

    private suspend fun loadMessages(conversationId: String) {
        val messages = database.messageDao().getByConversationId(conversationId).first()
        _uiState.update {
            it.copy(
                messages = messages.map { entity ->
                    MessageItem(
                        id = entity.id,
                        role = when (entity.role) {
                            Role.USER -> MessageRole.USER
                            else -> MessageRole.ASSISTANT
                        },
                        content = entity.content.filterIsInstance<ContentBlock.Text>()
                            .joinToString("\n") { it.text },
                        contentBlocks = entity.content,
                        inputTokens = entity.inputTokens,
                        outputTokens = entity.outputTokens,
                        status = entity.status,
                    )
                }
            )
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isGenerating) return

        val userMessageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val userMessage = MessageItem(
            id = userMessageId,
            role = MessageRole.USER,
            content = text,
            contentBlocks = listOf(ContentBlock.Text(text)),
            status = MessageStatus.COMPLETED,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isGenerating = true,
                streamingText = "",
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            val conversationId = ensureConversation(text, now)

            database.messageDao().insert(
                MessageEntity(
                    id = userMessageId,
                    conversationId = conversationId,
                    parentId = null,
                    role = Role.USER,
                    content = listOf(ContentBlock.Text(text)),
                    modelId = null,
                    providerId = null,
                    inputTokens = null,
                    outputTokens = null,
                    createdAt = now,
                    status = MessageStatus.COMPLETED,
                )
            )

            streamResponse(conversationId)
        }
    }

    private suspend fun ensureConversation(firstMessage: String, now: Long): String {
        val currentId = _uiState.value.conversationId
        if (currentId.isNotBlank()) return currentId

        val id = UUID.randomUUID().toString()
        val state = _uiState.value
        val providerId = state.providerId.ifEmpty {
            state.availableProviders.firstOrNull()?.id ?: ""
        }
        val modelId = state.modelId.ifEmpty {
            state.availableModels.firstOrNull()?.id ?: ""
        }
        val title = firstMessage.take(50)

        database.conversationDao().insert(
            ConversationEntity(
                id = id,
                title = title,
                assistantId = null,
                providerId = providerId,
                modelId = modelId,
                systemPrompt = null,
                tags = emptyList(),
                isPinned = false,
                createdAt = now,
                updatedAt = now,
            )
        )

        _uiState.update {
            it.copy(
                conversationId = id,
                title = title,
                providerId = providerId,
                modelId = modelId,
            )
        }

        return id
    }

    private suspend fun streamResponse(conversationId: String) {
        val state = _uiState.value
        val providerId = state.providerId
        val modelId = state.modelId

        if (providerId.isBlank() || modelId.isBlank()) {
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    errorMessage = "请先选择提供商和模型",
                )
            }
            return
        }

        val providerEntity = database.providerDao().getById(providerId)
        if (providerEntity == null) {
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    errorMessage = "未找到提供商",
                )
            }
            return
        }

        val apiKey = try {
            encryptor.decrypt(providerEntity.apiKeyEncrypted)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    errorMessage = "API密钥解密失败",
                )
            }
            return
        }

        val llmProvider = OpenAICompatProvider(
            id = providerEntity.id,
            displayName = providerEntity.name,
            baseUrl = providerEntity.baseUrl,
            apiKey = apiKey,
        )

        val assistantId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val assistantPlaceholder = MessageItem(
            id = assistantId,
            role = MessageRole.ASSISTANT,
            content = "",
            isStreaming = true,
            status = MessageStatus.STREAMING,
        )
        _uiState.update {
            it.copy(messages = it.messages + assistantPlaceholder)
        }

        database.messageDao().insert(
            MessageEntity(
                id = assistantId,
                conversationId = conversationId,
                parentId = null,
                role = Role.ASSISTANT,
                content = emptyList(),
                modelId = modelId,
                providerId = providerId,
                inputTokens = null,
                outputTokens = null,
                createdAt = now,
                status = MessageStatus.STREAMING,
            )
        )

        val chatMessages = _uiState.value.messages
            .filter { it.status != MessageStatus.ERROR && it.id != assistantId }
            .map { item ->
                ChatMessage(
                    role = when (item.role) {
                        MessageRole.USER -> Role.USER
                        MessageRole.ASSISTANT -> Role.ASSISTANT
                    },
                    content = item.content,
                )
            }

        val request = ChatRequest(
            providerId = providerId,
            modelId = modelId,
            messages = chatMessages,
            temperature = 0.7f,
            stream = true,
        )

        streamingJob = viewModelScope.launch {
            val fullResponse = StringBuilder()
            var inputTokens: Int? = null
            var outputTokens: Int? = null

            try {
                llmProvider.chatStream(request).collect { event ->
                    when (event) {
                        is ChatStreamEvent.Token -> {
                            fullResponse.append(event.text)
                            _uiState.update {
                                it.copy(streamingText = fullResponse.toString())
                            }
                        }
                        is ChatStreamEvent.Usage -> {
                            inputTokens = event.inputTokens
                            outputTokens = event.outputTokens
                        }
                        is ChatStreamEvent.Error -> {
                            _uiState.update {
                                it.copy(errorMessage = event.errorMessage)
                            }
                        }
                        is ChatStreamEvent.Done -> {}
                        is ChatStreamEvent.ToolCallStart -> {}
                        is ChatStreamEvent.ToolCallDelta -> {}
                        is ChatStreamEvent.ToolCallEnd -> {}
                    }
                }

                val finalContent = fullResponse.toString()
                _uiState.update { s ->
                    s.copy(
                        isGenerating = false,
                        streamingText = "",
                        messages = s.messages.map { msg ->
                            if (msg.id == assistantId) msg.copy(
                                content = finalContent,
                                contentBlocks = listOf(ContentBlock.Text(finalContent)),
                                isStreaming = false,
                                status = MessageStatus.COMPLETED,
                                inputTokens = inputTokens,
                                outputTokens = outputTokens,
                            ) else msg
                        },
                    )
                }

                database.messageDao().insert(
                    MessageEntity(
                        id = assistantId,
                        conversationId = conversationId,
                        parentId = null,
                        role = Role.ASSISTANT,
                        content = listOf(ContentBlock.Text(finalContent)),
                        modelId = modelId,
                        providerId = providerId,
                        inputTokens = inputTokens,
                        outputTokens = outputTokens,
                        createdAt = now,
                        status = MessageStatus.COMPLETED,
                    )
                )

                updateConversationTimestamp(conversationId)
            } catch (e: Exception) {
                val partialContent = fullResponse.toString()
                _uiState.update { s ->
                    s.copy(
                        isGenerating = false,
                        streamingText = "",
                        errorMessage = e.message,
                        messages = s.messages.map { msg ->
                            if (msg.id == assistantId) msg.copy(
                                content = partialContent,
                                contentBlocks = if (partialContent.isNotEmpty()) listOf(ContentBlock.Text(partialContent)) else emptyList(),
                                isStreaming = false,
                                status = MessageStatus.ERROR,
                            ) else msg
                        },
                    )
                }

                database.messageDao().insert(
                    MessageEntity(
                        id = assistantId,
                        conversationId = conversationId,
                        parentId = null,
                        role = Role.ASSISTANT,
                        content = if (partialContent.isNotEmpty()) listOf(ContentBlock.Text(partialContent)) else emptyList(),
                        modelId = modelId,
                        providerId = providerId,
                        inputTokens = null,
                        outputTokens = null,
                        createdAt = now,
                        status = MessageStatus.ERROR,
                    )
                )
            }
        }
    }

    private suspend fun updateConversationTimestamp(conversationId: String) {
        val conversation = database.conversationDao().getById(conversationId) ?: return
        database.conversationDao().update(
            conversation.copy(updatedAt = System.currentTimeMillis())
        )
    }

    fun stopGeneration() {
        streamingJob?.cancel()
        streamingJob = null
        val streamingText = _uiState.value.streamingText
        _uiState.update { state ->
            state.copy(
                isGenerating = false,
                streamingText = "",
                messages = state.messages.map { msg ->
                    if (msg.isStreaming) msg.copy(
                        content = streamingText.ifBlank { msg.content },
                        contentBlocks = if (streamingText.isNotBlank()) listOf(ContentBlock.Text(streamingText)) else msg.contentBlocks,
                        isStreaming = false,
                        status = MessageStatus.COMPLETED,
                    ) else msg
                },
            )
        }
    }

    fun selectProvider(providerId: String) {
        _uiState.update {
            it.copy(
                providerId = providerId,
                modelId = "",
                availableModels = emptyList(),
                showProviderPicker = false,
            )
        }

        viewModelScope.launch {
            loadModels(providerId)
            val firstModel = _uiState.value.availableModels.firstOrNull()
            if (firstModel != null) {
                _uiState.update { it.copy(modelId = firstModel.id) }
            }

            val convId = _uiState.value.conversationId
            if (convId.isNotBlank()) {
                val conversation = database.conversationDao().getById(convId)
                if (conversation != null) {
                    database.conversationDao().update(
                        conversation.copy(
                            providerId = providerId,
                            modelId = _uiState.value.modelId,
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }
            }
        }
    }

    fun selectModel(modelId: String) {
        _uiState.update {
            it.copy(
                modelId = modelId,
                showModelPicker = false,
            )
        }

        val convId = _uiState.value.conversationId
        if (convId.isNotBlank()) {
            viewModelScope.launch {
                val conversation = database.conversationDao().getById(convId)
                if (conversation != null) {
                    database.conversationDao().update(
                        conversation.copy(
                            modelId = modelId,
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }
            }
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun toggleWebSearch() {
        val newValue = !_uiState.value.webSearchEnabled
        _uiState.update { it.copy(webSearchEnabled = newValue) }
        prefs.edit().putBoolean("web_search_enabled", newValue).apply()
    }

    fun toggleStt() {
        val newValue = !_uiState.value.sttEnabled
        _uiState.update { it.copy(sttEnabled = newValue) }
        prefs.edit().putBoolean("stt_enabled", newValue).apply()
    }

    fun showProviderPicker() {
        _uiState.update { it.copy(showProviderPicker = true) }
    }

    fun hideProviderPicker() {
        _uiState.update { it.copy(showProviderPicker = false) }
    }

    fun showModelPicker() {
        _uiState.update { it.copy(showModelPicker = true) }
    }

    fun hideModelPicker() {
        _uiState.update { it.copy(showModelPicker = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }
}
