package com.aetherchat.feature.providers

import com.aetherchat.core.crypto.KeystoreEncryptor
import com.aetherchat.domain.model.ProviderRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureProvidersModule = module {
    viewModel { ProvidersViewModel(providerRepository = get()) }
    viewModel { AddProviderViewModel(providerRepository = get()) }
    viewModel { ProviderDetailViewModel(providerRepository = get(), encryptor = get()) }
}
