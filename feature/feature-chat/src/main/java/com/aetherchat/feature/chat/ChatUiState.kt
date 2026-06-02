package com.aetherchat.feature.chat

import com.aetherchat.domain.model.ContentBlock
import com.aetherchat.domain.model.MessageStatus

data class ChatUiState(
    val conversationId: String = "",
    val title: String = "",
    val providerId: String = "",
    val modelId: String = "",
    val availableProviders: List<ProviderOption> = emptyList(),
    val availableModels: List<ModelOption> = emptyList(),
    val showProviderPicker: Boolean = false,
    val showModelPicker: Boolean = false,
    val messages: List<MessageItem> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val streamingText: String = "",
    val errorMessage: String? = null,
    val webSearchEnabled: Boolean = false,
    val sttEnabled: Boolean = false,
    val sttIsListening: Boolean = false,
)

data class ProviderOption(
    val id: String,
    val name: String,
    val type: String,
)

data class ModelOption(
    val id: String,
    val displayName: String,
    val contextWindow: Int?,
)

data class MessageItem(
    val id: String,
    val role: MessageRole,
    val content: String,
    val contentBlocks: List<ContentBlock> = emptyList(),
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val status: MessageStatus = MessageStatus.COMPLETED,
    val isStreaming: Boolean = false,
)

enum class MessageRole { USER, ASSISTANT }
