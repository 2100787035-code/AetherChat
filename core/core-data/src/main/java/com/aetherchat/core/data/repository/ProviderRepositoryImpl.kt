package com.aetherchat.core.data.repository

import com.aetherchat.core.crypto.KeystoreEncryptor
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.local.ModelEntity
import com.aetherchat.core.data.local.ProviderEntity
import com.aetherchat.domain.model.ConnectionInfo
import com.aetherchat.domain.model.ModelInfo
import com.aetherchat.domain.model.ModelTestResult
import com.aetherchat.domain.model.Provider
import com.aetherchat.domain.model.ProviderConfig
import com.aetherchat.domain.model.ProviderRepository
import com.aetherchat.domain.model.ProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ProviderRepositoryImpl(
    private val database: AetherChatDatabase,
    private val encryptor: KeystoreEncryptor,
) : ProviderRepository {

    private val providerDao = database.providerDao()
    private val modelDao = database.modelDao()

    override suspend fun addProvider(config: ProviderConfig): Result<Provider> = runCatching {
        val id = UUID.randomUUID().toString()
        val encryptedKey = encryptor.encrypt(config.apiKey)
        val provider = ProviderEntity(
            id = id,
            name = config.name,
            type = config.type,
            baseUrl = config.baseUrl,
            apiKeyEncrypted = encryptedKey,
        )
        providerDao.insert(provider)
        provider.toDomain()
    }

    override suspend fun updateProvider(provider: Provider): Result<Unit> = runCatching {
        val existing = providerDao.getById(provider.id) ?: throw NoSuchElementException()
        providerDao.update(
            existing.copy(
                name = provider.name,
                baseUrl = provider.baseUrl,
                isEnabled = provider.isEnabled,
                sortOrder = provider.sortOrder,
            )
        )
    }

    override suspend fun deleteProvider(id: String): Result<Unit> = runCatching {
        val entity = providerDao.getById(id) ?: throw NoSuchElementException()
        providerDao.delete(entity)
    }

    override suspend fun setProviderEnabled(id: String, enabled: Boolean): Result<Unit> = runCatching {
        val entity = providerDao.getById(id) ?: throw NoSuchElementException()
        providerDao.update(entity.copy(isEnabled = enabled))
    }

    override suspend fun testProviderConnection(id: String): Result<ConnectionInfo> {
        return Result.failure(NotImplementedError("Use LLMProvider.testConnection()"))
    }

    override suspend fun fetchModels(providerId: String): Result<List<ModelInfo>> {
        return Result.failure(NotImplementedError("Use LLMProvider.listModels()"))
    }

    override suspend fun setModelEnabled(providerId: String, modelId: String, enabled: Boolean): Result<Unit> = runCatching {
        modelDao.setEnabled(modelId, providerId, enabled)
    }

    override suspend fun testModelConnection(providerId: String, modelId: String): Result<ModelTestResult> {
        return Result.failure(NotImplementedError("Use LLMProvider.testModel()"))
    }

    override suspend fun addCustomModel(providerId: String, model: ModelInfo): Result<Unit> = runCatching {
        val entity = ModelEntity(
            id = model.id,
            providerId = providerId,
            displayName = model.displayName,
            contextWindow = model.contextWindow,
            supportVision = model.supportVision,
            supportFunctionCall = model.supportFunctionCall,
            isEnabled = model.isEnabled,
            isCustom = true,
            lastTestedAt = null,
            lastTestResult = null,
        )
        modelDao.insert(entity)
    }

    override suspend fun deleteModel(providerId: String, modelId: String): Result<Unit> = runCatching {
        modelDao.deleteById(modelId, providerId)
    }

    override fun getEnabledModels(providerId: String): Flow<List<ModelInfo>> {
        return modelDao.getEnabledByProviderId(providerId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllEnabledModels(): Flow<Map<String, List<ModelInfo>>> {
        return modelDao.getByProviderId("").map { emptyMap() }
    }

    private fun ProviderEntity.toDomain() = Provider(
        id = id,
        name = name,
        type = type,
        baseUrl = baseUrl,
        apiKeyEncrypted = apiKeyEncrypted,
        isEnabled = isEnabled,
        sortOrder = sortOrder,
    )

    private fun ModelEntity.toDomain() = ModelInfo(
        id = id,
        providerId = providerId,
        displayName = displayName,
        contextWindow = contextWindow,
        supportVision = supportVision,
        supportFunctionCall = supportFunctionCall,
        isEnabled = isEnabled,
        isCustom = isCustom,
        lastTestedAt = lastTestedAt,
        lastTestResult = lastTestResult,
    )
}
