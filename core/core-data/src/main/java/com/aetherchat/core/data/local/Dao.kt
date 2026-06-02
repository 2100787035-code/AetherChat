package com.aetherchat.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: ProviderEntity)

    @Update
    suspend fun update(provider: ProviderEntity)

    @Delete
    suspend fun delete(provider: ProviderEntity)

    @Query("SELECT * FROM providers ORDER BY sortOrder ASC")
    fun getAll(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getById(id: String): ProviderEntity?

    @Query("SELECT * FROM providers WHERE isEnabled = 1 ORDER BY sortOrder ASC")
    fun getEnabled(): Flow<List<ProviderEntity>>
}

@Dao
interface ModelDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: ModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(models: List<ModelEntity>)

    @Update
    suspend fun update(model: ModelEntity)

    @Delete
    suspend fun delete(model: ModelEntity)

    @Query("SELECT * FROM models WHERE providerId = :providerId")
    fun getByProviderId(providerId: String): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE providerId = :providerId AND isEnabled = 1")
    fun getEnabledByProviderId(providerId: String): Flow<List<ModelEntity>>

    @Query("SELECT * FROM models WHERE id = :id AND providerId = :providerId")
    suspend fun getById(id: String, providerId: String): ModelEntity?

    @Query("DELETE FROM models WHERE id = :id AND providerId = :providerId")
    suspend fun deleteById(id: String, providerId: String)

    @Query("UPDATE models SET isEnabled = :enabled WHERE id = :id AND providerId = :providerId")
    suspend fun setEnabled(id: String, providerId: String, enabled: Boolean)
}

@Dao
interface ConversationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(conversation: ConversationEntity)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Delete
    suspend fun delete(conversation: ConversationEntity)

    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, updatedAt DESC")
    fun getAll(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getById(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE title LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ConversationEntity>>
}

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Update
    suspend fun update(message: MessageEntity)

    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getByConversationId(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): MessageEntity?
}

@Dao
interface AssistantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assistant: AssistantEntity)

    @Update
    suspend fun update(assistant: AssistantEntity)

    @Delete
    suspend fun delete(assistant: AssistantEntity)

    @Query("SELECT * FROM assistants ORDER BY createdAt DESC")
    fun getAll(): Flow<List<AssistantEntity>>

    @Query("SELECT * FROM assistants WHERE id = :id")
    suspend fun getById(id: String): AssistantEntity?
}
