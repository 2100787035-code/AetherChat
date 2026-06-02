package com.aetherchat.feature.conversations

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureConversationsModule = module {
    viewModel { ConversationsViewModel(get()) }
}
