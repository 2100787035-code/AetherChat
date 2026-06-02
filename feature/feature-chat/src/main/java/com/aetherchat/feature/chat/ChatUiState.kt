package com.aetherchat.feature.chat

import com.aetherchat.domain.model.ContentBlock
import com.aetherchat.domain.model.Message
import com.aetherchat.domain.model.MessageStatus

data class ChatUiState(
    val conversationId: String = "",
    val title: String = "",
    val modelId: String = "",
    val providerId: String = "",
    val messages: List<MessageItem> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val streamingText: String = "",
    val errorMessage: String? = null,
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
