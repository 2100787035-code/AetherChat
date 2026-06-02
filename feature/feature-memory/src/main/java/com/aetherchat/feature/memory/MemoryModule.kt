package com.aetherchat.feature.memory

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureMemoryModule = module {
    viewModel { MemoryViewModel() }
}
