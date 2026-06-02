package com.aetherchat.feature.memory

import kotlinx.serialization.Serializable

@Serializable
data class MemoryEntry(
    val id: String,
    val content: String,
    val source: MemorySource,
    val conversationId: String? = null,
    val timestamp: Long,
    val relevanceScore: Float? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
enum class MemorySource {
    CONVERSATION_SUMMARY,
    USER_FACT,
    DOCUMENT_CHUNK,
    MANUAL_ENTRY,
}

@Serializable
data class KnowledgeBase(
    val id: String,
    val name: String,
    val description: String = "",
    val documentCount: Int = 0,
    val totalChunks: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
data class DocumentChunk(
    val id: String,
    val knowledgeBaseId: String,
    val documentName: String,
    val content: String,
    val chunkIndex: Int,
    val embedding: List<Float>? = null,
)

data class MemoryUiState(
    val memories: List<MemoryEntry> = emptyList(),
    val knowledgeBases: List<KnowledgeBase> = emptyList(),
    val selectedKnowledgeBase: KnowledgeBase? = null,
    val searchQuery: String = "",
    val searchResults: List<MemoryEntry> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class CreateKnowledgeBaseUiState(
    val name: String = "",
    val description: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
