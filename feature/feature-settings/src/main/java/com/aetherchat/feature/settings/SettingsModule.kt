package com.aetherchat.feature.settings

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureSettingsModule = module {
    viewModel { SettingsViewModel() }
}
