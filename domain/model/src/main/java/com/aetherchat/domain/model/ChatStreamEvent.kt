package com.aetherchat.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ChatStreamEvent {
    @Serializable
    data class Token(val text: String) : ChatStreamEvent()

    @Serializable
    data class ToolCallStart(val id: String, val name: String) : ChatStreamEvent()

    @Serializable
    data class ToolCallDelta(val inputDelta: String) : ChatStreamEvent()

    @Serializable
    data class ToolCallEnd(val id: String) : ChatStreamEvent()

    @Serializable
    data class Usage(val inputTokens: Int, val outputTokens: Int) : ChatStreamEvent()

    @Serializable
    data class Error(val errorMessage: String) : ChatStreamEvent()

    @Serializable
    data object Done : ChatStreamEvent()
}
