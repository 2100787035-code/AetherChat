package com.aetherchat.domain.model

import kotlinx.coroutines.flow.Flow

interface LLMProvider {
    val id: String
    val displayName: String

    fun chatStream(request: ChatRequest): Flow<ChatStreamEvent>
    suspend fun chat(request: ChatRequest): ChatResponse
    suspend fun listModels(): Result<List<ModelInfo>>
    suspend fun testConnection(): Result<ConnectionInfo>
    suspend fun testModel(modelId: String): Result<ModelTestResult>
}

@kotlinx.serialization.Serializable
data class ChatResponse(
    val message: ChatMessage,
    val inputTokens: Int?,
    val outputTokens: Int?,
)

data class ConnectionInfo(val latencyMs: Long, val availableModelCount: Int)

data class ModelTestResult(
    val modelId: String,
    val success: Boolean,
    val latencyMs: Long?,
    val errorMessage: String?,
)
