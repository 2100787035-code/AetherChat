package com.aetherchat.core.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aetherchat.domain.model.ContentBlock
import com.aetherchat.domain.model.MessageStatus
import com.aetherchat.domain.model.ProviderType
import com.aetherchat.domain.model.Role
import com.aetherchat.domain.model.TestResult

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: ProviderType,
    val baseUrl: String,
    val apiKeyEncrypted: String,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
)

@Entity(tableName = "models")
data class ModelEntity(
    @PrimaryKey val id: String,
    val providerId: String,
    val displayName: String,
    val contextWindow: Int?,
    val supportVision: Boolean = false,
    val supportFunctionCall: Boolean = false,
    val isEnabled: Boolean = true,
    val isCustom: Boolean = false,
    val lastTestedAt: Long?,
    val lastTestResult: TestResult?,
)

@Entity(
    tableName = "conversations",
    indices = [Index("updatedAt")]
)
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val assistantId: String?,
    val providerId: String,
    val modelId: String,
    val systemPrompt: String?,
    val tags: List<String>,
    val isPinned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

@Entity(
    tableName = "messages",
    foreignKeys = [ForeignKey(
        entity = ConversationEntity.class,
        parentColumns = ["id"],
        childColumns = ["conversationId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("conversationId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val parentId: String?,
    val role: Role,
    val content: List<ContentBlock>,
    val modelId: String?,
    val providerId: String?,
    val inputTokens: Int?,
    val outputTokens: Int?,
    val createdAt: Long,
    val status: MessageStatus,
)

@Entity(tableName = "assistants")
data class AssistantEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconEmoji: String = "🤖",
    val systemPrompt: String = "",
    val providerId: String?,
    val modelId: String?,
    val temperature: Float = 0.7f,
    val createdAt: Long,
    val updatedAt: Long,
)
