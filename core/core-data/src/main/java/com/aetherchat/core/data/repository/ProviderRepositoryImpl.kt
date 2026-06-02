package com.aetherchat.core.data.repository

import com.aetherchat.core.crypto.KeystoreEncryptor
import com.aetherchat.core.data.local.AetherChatDatabase
import com.aetherchat.core.data.local.ModelEntity
import com.aetherchat.core.data.local.ProviderEntity
import com.aetherchat.core.network.provider.OpenAICompatProvider
import com.aetherchat.domain.model.ConnectionInfo
import com.aetherchat.domain.model.ModelInfo
import com.aetherchat.domain.model.ModelTestResult
import com.aetherchat.domain.model.Provider
import com.aetherchat.domain.model.ProviderConfig
import com.aetherchat.domain.model.ProviderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ProviderRepositoryImpl(
    private val database: AetherChatDatabase,
    private val encryptor: KeystoreEncryptor,
) : ProviderRepository {

    private val providerDao = database.providerDao()
    private val modelDao = database.modelDao()

    override fun getAllProviders(): Flow<List<Provider>> {
        return providerDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getProviderById(id: String): Provider? {
        return providerDao.getById(id)?.toDomain()
    }

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

    override suspend fun testProviderConnection(id: String): Result<ConnectionInfo> = runCatching {
        val entity = providerDao.getById(id) ?: throw NoSuchElementException("Provider not found")
        val apiKey = encryptor.decrypt(entity.apiKeyEncrypted)
        val llmProvider = OpenAICompatProvider(
            id = entity.id,
            displayName = entity.name,
            baseUrl = entity.baseUrl,
            apiKey = apiKey,
        )
        llmProvider.testConnection().getOrThrow()
    }

    override suspend fun fetchModels(providerId: String): Result<List<ModelInfo>> = runCatching {
        val entity = providerDao.getById(providerId) ?: throw NoSuchElementException("Provider not found")
        val apiKey = encryptor.decrypt(entity.apiKeyEncrypted)
        val llmProvider = OpenAICompatProvider(
            id = entity.id,
            displayName = entity.name,
            baseUrl = entity.baseUrl,
            apiKey = apiKey,
        )
        val models = llmProvider.listModels().getOrThrow()
        for (model in models) {
            val existing = modelDao.getById(model.id, providerId)
            if (existing != null) {
                modelDao.update(existing.copy(
                    displayName = model.displayName,
                    contextWindow = model.contextWindow ?: existing.contextWindow,
                    supportVision = model.supportVision || existing.supportVision,
                    supportFunctionCall = model.supportFunctionCall || existing.supportFunctionCall,
                ))
            } else {
                modelDao.insert(ModelEntity(
                    id = model.id,
                    providerId = providerId,
                    displayName = model.displayName,
                    contextWindow = model.contextWindow,
                    supportVision = model.supportVision,
                    supportFunctionCall = model.supportFunctionCall,
                    isEnabled = true,
                    isCustom = false,
                    lastTestedAt = null,
                    lastTestResult = null,
                ))
            }
        }
        models
    }

    override suspend fun setModelEnabled(providerId: String, modelId: String, enabled: Boolean): Result<Unit> = runCatching {
        modelDao.setEnabled(modelId, providerId, enabled)
    }

    override suspend fun testModelConnection(providerId: String, modelId: String): Result<ModelTestResult> = runCatching {
        val entity = providerDao.getById(providerId) ?: throw NoSuchElementException("Provider not found")
        val apiKey = encryptor.decrypt(entity.apiKeyEncrypted)
        val llmProvider = OpenAICompatProvider(
            id = entity.id,
            displayName = entity.name,
            baseUrl = entity.baseUrl,
            apiKey = apiKey,
        )
        llmProvider.testModel(modelId).getOrThrow()
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
        return providerDao.getAll().map { providers ->
            emptyMap()
        }
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
