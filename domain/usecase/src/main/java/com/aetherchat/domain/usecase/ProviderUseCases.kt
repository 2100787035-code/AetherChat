package com.aetherchat.domain.usecase

import com.aetherchat.domain.model.ConnectionInfo
import com.aetherchat.domain.model.ModelInfo
import com.aetherchat.domain.model.ModelTestResult
import com.aetherchat.domain.model.Provider
import com.aetherchat.domain.model.ProviderConfig
import com.aetherchat.domain.model.ProviderRepository
import kotlinx.coroutines.flow.Flow

class AddProviderUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(config: ProviderConfig): Result<Provider> = repository.addProvider(config)
}

class UpdateProviderUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(provider: Provider): Result<Unit> = repository.updateProvider(provider)
}

class DeleteProviderUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteProvider(id)
}

class SetProviderEnabledUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(id: String, enabled: Boolean): Result<Unit> = repository.setProviderEnabled(id, enabled)
}

class TestProviderConnectionUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(id: String): Result<ConnectionInfo> = repository.testProviderConnection(id)
}

class FetchModelsUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(providerId: String): Result<List<ModelInfo>> = repository.fetchModels(providerId)
}

class SetModelEnabledUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(providerId: String, modelId: String, enabled: Boolean): Result<Unit> =
        repository.setModelEnabled(providerId, modelId, enabled)
}

class TestModelConnectionUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(providerId: String, modelId: String): Result<ModelTestResult> =
        repository.testModelConnection(providerId, modelId)
}

class AddCustomModelUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(providerId: String, model: ModelInfo): Result<Unit> =
        repository.addCustomModel(providerId, model)
}

class DeleteModelUseCase(private val repository: ProviderRepository) {
    suspend operator fun invoke(providerId: String, modelId: String): Result<Unit> =
        repository.deleteModel(providerId, modelId)
}

class GetEnabledModelsUseCase(private val repository: ProviderRepository) {
    operator fun invoke(providerId: String): Flow<List<ModelInfo>> = repository.getEnabledModels(providerId)
}

class GetAllEnabledModelsUseCase(private val repository: ProviderRepository) {
    operator fun invoke(): Flow<Map<String, List<ModelInfo>>> = repository.getAllEnabledModels()
}
