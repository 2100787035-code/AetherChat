package com.aetherchat.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: String,
    val title: String,
    val assistantId: String? = null,
    val providerId: String,
    val modelId: String,
    val systemPrompt: String? = null,
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
