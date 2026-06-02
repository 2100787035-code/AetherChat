package com.aetherchat.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ContentBlock {
    @Serializable
    data class Text(val text: String) : ContentBlock()

    @Serializable
    data class Image(val uri: String, val mimeType: String) : ContentBlock()

    @Serializable
    data class Document(val uri: String, val name: String, val mimeType: String) : ContentBlock()

    @Serializable
    data class ToolCall(val id: String, val name: String, val input: String) : ContentBlock()

    @Serializable
    data class ToolResult(val toolCallId: String, val content: String, val isError: Boolean) : ContentBlock()
}
