package com.aetherchat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val parentId: String? = null,
    val role: Role,
    val content: List<ContentBlock> = emptyList(),
    val modelId: String? = null,
    val providerId: String? = null,
    val inputTokens: Int? = null,
    val outputTokens: Int? = null,
    val createdAt: Long,
    val status: MessageStatus,
)

@Serializable
enum class Role {
    USER, ASSISTANT, SYSTEM, TOOL
}

@Serializable
enum class MessageStatus {
    PENDING, STREAMING, COMPLETED, ERROR
}
