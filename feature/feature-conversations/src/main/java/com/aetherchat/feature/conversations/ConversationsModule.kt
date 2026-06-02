package com.aetherchat.feature.conversations

import com.aetherchat.core.data.local.AetherChatDatabase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val featureConversationsModule = module {
    viewModel { ConversationsViewModel(get<AetherChatDatabase>()) }
}
