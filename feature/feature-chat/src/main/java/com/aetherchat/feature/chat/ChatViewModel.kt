package com.aetherchat.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aetherchat.domain.model.ChatMessage
import com.aetherchat.domain.model.ChatRequest
import com.aetherchat.domain.model.ChatStreamEvent
import com.aetherchat.domain.model.ContentBlock
import com.aetherchat.domain.model.Message
import com.aetherchat.domain.model.MessageStatus
import com.aetherchat.domain.model.ProviderRepository
import com.aetherchat.domain.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(
    private val providerRepository: ProviderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var conversationId: String = ""

    fun init(conversationId: String) {
        this.conversationId = conversationId
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isGenerating) return

        val userMessage = MessageItem(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = text,
            status = MessageStatus.COMPLETED,
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isGenerating = true,
                streamingText = "",
            )
        }

        viewModelScope.launch {
            val state = _uiState.value
            val assistantId = UUID.randomUUID().toString()

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

            try {
                val fullResponse = StringBuilder()
                _uiState.update {
                    it.copy(streamingText = "")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = e.message,
                        messages = it.messages.map { msg ->
                            if (msg.id == assistantId) msg.copy(
                                isStreaming = false,
                                status = MessageStatus.ERROR,
                            ) else msg
                        },
                    )
                }
            }
        }
    }

    fun stopGeneration() {
        _uiState.update { it.copy(isGenerating = false) }
    }

    fun retryMessage(messageId: String) {
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
