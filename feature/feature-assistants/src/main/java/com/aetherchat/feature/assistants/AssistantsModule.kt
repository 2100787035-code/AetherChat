package com.aetherchat.feature.assistants

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureAssistantsModule = module {
    viewModel { AssistantsViewModel() }
    viewModel { CreateAssistantViewModel() }
}
