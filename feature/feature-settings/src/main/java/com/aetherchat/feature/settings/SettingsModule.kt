package com.aetherchat.feature.settings

import com.aetherchat.core.data.export.DataExporter
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureSettingsModule = module {
    single { DataExporter() }

    viewModel {
        SettingsViewModel(
            providerRepository = get(),
            database = get(),
            encryptor = get(),
            dataExporter = get(),
            prefs = get(),
        )
    }
}
