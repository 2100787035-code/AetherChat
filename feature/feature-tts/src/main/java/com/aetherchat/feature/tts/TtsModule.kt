package com.aetherchat.feature.tts

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureTtsModule = module {
    viewModel { TtsViewModel() }
}
