package com.aetherchat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val providerId: String,
    val modelId: String,
    val messages: List<ChatMessage>,
    val temperature: Float = 0.7f,
    val maxTokens: Int? = null,
    val stream: Boolean = true,
    val tools: List<ToolDefinition>? = null,
)

@Serializable
data class ChatMessage(
    val role: Role,
    val content: String,
)

@Serializable
data class ToolDefinition(
    val name: String,
    val description: String,
    val parameters: String,
)
