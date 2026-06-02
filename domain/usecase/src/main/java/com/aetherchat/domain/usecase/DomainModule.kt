package com.aetherchat.domain.usecase

import com.aetherchat.domain.model.ProviderRepository
import org.koin.dsl.module

val domainModule = module {
    factory { AddProviderUseCase(get()) }
    factory { UpdateProviderUseCase(get()) }
    factory { DeleteProviderUseCase(get()) }
    factory { SetProviderEnabledUseCase(get()) }
    factory { TestProviderConnectionUseCase(get()) }
    factory { FetchModelsUseCase(get()) }
    factory { SetModelEnabledUseCase(get()) }
    factory { TestModelConnectionUseCase(get()) }
    factory { AddCustomModelUseCase(get()) }
    factory { DeleteModelUseCase(get()) }
    factory { GetEnabledModelsUseCase(get()) }
    factory { GetAllEnabledModelsUseCase(get()) }
}
