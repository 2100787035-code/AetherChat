package com.aetherchat.feature.assistants

import com.aetherchat.core.data.local.AetherChatDatabase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureAssistantsModule = module {
    viewModel { AssistantsViewModel(database = get()) }
    viewModel { CreateAssistantViewModel(database = get()) }
    viewModel { AssistantDetailViewModel(database = get()) }
}