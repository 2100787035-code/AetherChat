package com.aetherchat.core.network.api

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val temperature: Float = 0.7f,
    val max_tokens: Int? = null,
    val stream: Boolean = true,
    val tools: List<ToolDto>? = null,
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String,
)

@Serializable
data class ToolDto(
    val type: String = "function",
    val function: ToolFunctionDto,
)

@Serializable
data class ToolFunctionDto(
    val name: String,
    val description: String,
    val parameters: String,
)

@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val choices: List<ChoiceDto> = emptyList(),
    val usage: UsageDto? = null,
)

@Serializable
data class ChoiceDto(
    val index: Int = 0,
    val message: MessageDto? = null,
    val delta: DeltaDto? = null,
    val finish_reason: String? = null,
)

@Serializable
data class MessageDto(
    val role: String? = null,
    val content: String? = null,
    val tool_calls: List<ToolCallDto>? = null,
)

@Serializable
data class DeltaDto(
    val role: String? = null,
    val content: String? = null,
    val tool_calls: List<ToolCallDeltaDto>? = null,
)

@Serializable
data class ToolCallDto(
    val id: String,
    val type: String = "function",
    val function: ToolCallFunctionDto,
)

@Serializable
data class ToolCallFunctionDto(
    val name: String,
    val arguments: String,
)

@Serializable
data class ToolCallDeltaDto(
    val index: Int = 0,
    val id: String? = null,
    val type: String? = null,
    val function: ToolCallFunctionDeltaDto? = null,
)

@Serializable
data class ToolCallFunctionDeltaDto(
    val name: String? = null,
    val arguments: String? = null,
)

@Serializable
data class UsageDto(
    val prompt_tokens: Int? = null,
    val completion_tokens: Int? = null,
    val total_tokens: Int? = null,
)

@Serializable
data class ModelsResponse(
    val data: List<ModelDto> = emptyList(),
)

@Serializable
data class ModelDto(
    val id: String,
    val owned_by: String? = null,
)
