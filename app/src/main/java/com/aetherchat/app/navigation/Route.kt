package com.aetherchat.app.navigation

import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

@Serializable
data object ConversationsRoute

@Serializable
data class ChatRoute(val conversationId: String)

@Serializable
data object ProvidersRoute

@Serializable
data object AddProviderRoute

@Serializable
data class ProviderDetailRoute(val providerId: String)

@Serializable
data object SettingsRoute

@Serializable
data object AssistantsRoute

@Serializable
data object CreateAssistantRoute
