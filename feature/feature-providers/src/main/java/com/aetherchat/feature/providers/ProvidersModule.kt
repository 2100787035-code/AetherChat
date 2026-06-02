package com.aetherchat.feature.providers

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureProvidersModule = module {
    viewModel { ProvidersViewModel(get(), get()) }
    viewModel { AddProviderViewModel(get()) }
    viewModel { (providerId: String) -> ProviderDetailViewModel(get(), get()) }
}
