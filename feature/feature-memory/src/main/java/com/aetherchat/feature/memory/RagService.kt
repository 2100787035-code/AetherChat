package com.aetherchat.feature.memory

import com.aetherchat.domain.model.ChatMessage
import com.aetherchat.domain.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RagService {
    suspend fun indexDocument(knowledgeBaseId: String, content: String, documentName: String): Result<List<DocumentChunk>>
    suspend fun query(knowledgeBaseId: String, query: String, topK: Int = 5): Result<List<DocumentChunk>>
    suspend fun buildContext(query: String, knowledgeBaseIds: List<String>, topK: Int = 5): Result<String>
    suspend fun extractMemories(conversationId: String, messages: List<ChatMessage>): Result<List<MemoryEntry>>
}

class SimpleRagService : RagService {

    override suspend fun indexDocument(knowledgeBaseId: String, content: String, documentName: String): Result<List<DocumentChunk>> =
        withContext(Dispatchers.Default) {
            runCatching {
                val chunkSize = 500
                val chunks = content.chunked(chunkSize).mapIndexed { index, chunk ->
                    DocumentChunk(
                        id = java.util.UUID.randomUUID().toString(),
                        knowledgeBaseId = knowledgeBaseId,
                        documentName = documentName,
                        content = chunk,
                        chunkIndex = index,
                    )
                }
                chunks
            }
        }

    override suspend fun query(knowledgeBaseId: String, query: String, topK: Int): Result<List<DocumentChunk>> =
        withContext(Dispatchers.Default) {
            runCatching {
                emptyList<DocumentChunk>()
            }
        }

    override suspend fun buildContext(query: String, knowledgeBaseIds: List<String>, topK: Int): Result<String> =
        withContext(Dispatchers.Default) {
            runCatching {
                val allChunks = mutableListOf<DocumentChunk>()
                for (kbId in knowledgeBaseIds) {
                    val result = query(kbId, query, topK)
                    result.getOrNull()?.let { allChunks.addAll(it) }
                }
                if (allChunks.isEmpty()) "" else allChunks.joinToString("\n\n") { it.content }
            }
        }

    override suspend fun extractMemories(conversationId: String, messages: List<ChatMessage>): Result<List<MemoryEntry>> =
        withContext(Dispatchers.Default) {
            runCatching {
                val userMessages = messages.filter { it.role == Role.USER }
                userMessages.mapNotNull { msg ->
                    if (msg.content.length > 20) {
                        MemoryEntry(
                            id = java.util.UUID.randomUUID().toString(),
                            content = msg.content.take(200),
                            source = MemorySource.CONVERSATION_SUMMARY,
                            conversationId = conversationId,
                            timestamp = System.currentTimeMillis(),
                        )
                    } else null
                }
            }
        }
}
