package com.aetherchat.feature.chat

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureChatModule = module {
    viewModel { ChatViewModel(get()) }
}
