package com.aetherchat.core.data.export

import com.aetherchat.domain.model.Conversation
import com.aetherchat.domain.model.Message
import com.aetherchat.domain.model.Provider
import com.aetherchat.domain.model.ModelInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ExportData(
    val version: Int = 1,
    val exportedAt: Long,
    val providers: List<ProviderExport> = emptyList(),
    val conversations: List<ConversationExport> = emptyList(),
)

@Serializable
data class ProviderExport(
    val id: String,
    val name: String,
    val type: String,
    val baseUrl: String,
    val isEnabled: Boolean,
    val models: List<ModelExport> = emptyList(),
)

@Serializable
data class ModelExport(
    val id: String,
    val displayName: String,
    val isEnabled: Boolean,
    val isCustom: Boolean,
)

@Serializable
data class ConversationExport(
    val id: String,
    val title: String,
    val providerId: String,
    val modelId: String,
    val systemPrompt: String?,
    val tags: List<String>,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val messages: List<MessageExport> = emptyList(),
)

@Serializable
data class MessageExport(
    val id: String,
    val role: String,
    val content: String,
    val createdAt: Long,
)

class DataExporter {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun exportToJson(
        providers: List<Provider>,
        models: Map<String, List<ModelInfo>>,
        conversations: List<Conversation>,
        messages: Map<String, List<Message>>,
    ): String {
        val exportData = ExportData(
            exportedAt = System.currentTimeMillis(),
            providers = providers.map { provider ->
                ProviderExport(
                    id = provider.id,
                    name = provider.name,
                    type = provider.type.name,
                    baseUrl = provider.baseUrl,
                    isEnabled = provider.isEnabled,
                    models = (models[provider.id] ?: emptyList()).map { model ->
                        ModelExport(
                            id = model.id,
                            displayName = model.displayName,
                            isEnabled = model.isEnabled,
                            isCustom = model.isCustom,
                        )
                    },
                )
            },
            conversations = conversations.map { conversation ->
                ConversationExport(
                    id = conversation.id,
                    title = conversation.title,
                    providerId = conversation.providerId,
                    modelId = conversation.modelId,
                    systemPrompt = conversation.systemPrompt,
                    tags = conversation.tags,
                    isPinned = conversation.isPinned,
                    createdAt = conversation.createdAt,
                    updatedAt = conversation.updatedAt,
                    messages = (messages[conversation.id] ?: emptyList()).map { msg ->
                        MessageExport(
                            id = msg.id,
                            role = msg.role.name,
                            content = msg.content.filterIsInstance<com.aetherchat.domain.model.ContentBlock.Text>()
                                .joinToString("\n") { it.text },
                            createdAt = msg.createdAt,
                        )
                    },
                )
            },
        )
        return json.encodeToString(ExportData.serializer(), exportData)
    }

    fun importFromJson(jsonString: String): Result<ExportData> = runCatching {
        json.decodeFromString(ExportData.serializer(), jsonString)
    }
}
