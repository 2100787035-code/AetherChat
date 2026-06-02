package com.aetherchat.domain.model

import kotlinx.coroutines.flow.Flow

interface ProviderRepository {
    fun getAllProviders(): Flow<List<Provider>>
    suspend fun getProviderById(id: String): Provider?
    suspend fun addProvider(config: ProviderConfig): Result<Provider>
    suspend fun updateProvider(provider: Provider): Result<Unit>
    suspend fun deleteProvider(id: String): Result<Unit>
    suspend fun setProviderEnabled(id: String, enabled: Boolean): Result<Unit>
    suspend fun testProviderConnection(id: String): Result<ConnectionInfo>
    suspend fun fetchModels(providerId: String): Result<List<ModelInfo>>
    suspend fun setModelEnabled(providerId: String, modelId: String, enabled: Boolean): Result<Unit>
    suspend fun testModelConnection(providerId: String, modelId: String): Result<ModelTestResult>
    suspend fun addCustomModel(providerId: String, model: ModelInfo): Result<Unit>
    suspend fun deleteModel(providerId: String, modelId: String): Result<Unit>
    fun getEnabledModels(providerId: String): Flow<List<ModelInfo>>
    fun getAllEnabledModels(): Flow<Map<String, List<ModelInfo>>>
}

data class ProviderConfig(
    val type: ProviderType,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
)
