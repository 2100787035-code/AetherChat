package com.aetherchat.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aetherchat.app.onboarding.OnboardingScreen
import com.aetherchat.feature.assistants.AssistantsScreen
import com.aetherchat.feature.assistants.AssistantsViewModel
import com.aetherchat.feature.assistants.CreateAssistantScreen
import com.aetherchat.feature.assistants.CreateAssistantViewModel
import com.aetherchat.feature.chat.ChatScreen
import com.aetherchat.feature.chat.ChatViewModel
import com.aetherchat.feature.conversations.ConversationsDrawer
import com.aetherchat.feature.conversations.ConversationsViewModel
import com.aetherchat.feature.providers.AddProviderScreen
import com.aetherchat.feature.providers.AddProviderViewModel
import com.aetherchat.feature.providers.ProviderDetailScreen
import com.aetherchat.feature.providers.ProviderDetailViewModel
import com.aetherchat.feature.providers.ProvidersScreen
import com.aetherchat.feature.providers.ProvidersViewModel
import com.aetherchat.feature.settings.SettingsScreen
import com.aetherchat.feature.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AetherChatNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = OnboardingRoute,
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onNavigateToConversations = {
                    navController.navigate(ConversationsRoute) {
                        popUpTo<OnboardingRoute> { inclusive = true }
                    }
                },
                onNavigateToAddProvider = {
                    navController.navigate(AddProviderRoute)
                },
            )
        }
        composable<ConversationsRoute> {
            val viewModel: ConversationsViewModel = koinViewModel()
            ConversationsDrawer(
                viewModel = viewModel,
                onConversationClick = { id -> navController.navigate(ChatRoute(id)) },
                onNewConversation = {
                    val id = viewModel.createNewConversation()
                    navController.navigate(ChatRoute(id))
                },
                onNavigateToSettings = { navController.navigate(SettingsRoute) },
                onNavigateToProviders = { navController.navigate(ProvidersRoute) },
                onNavigateToAssistants = { navController.navigate(AssistantsRoute) },
            )
        }
        composable<ChatRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ChatRoute>()
            val viewModel: ChatViewModel = koinViewModel()
            ChatScreen(
                viewModel = viewModel,
                conversationId = route.conversationId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<ProvidersRoute> {
            val viewModel: ProvidersViewModel = koinViewModel()
            ProvidersScreen(
                viewModel = viewModel,
                onAddProvider = { navController.navigate(AddProviderRoute) },
                onProviderClick = { id -> navController.navigate(ProviderDetailRoute(id)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<AddProviderRoute> {
            val viewModel: AddProviderViewModel = koinViewModel()
            AddProviderScreen(
                viewModel = viewModel,
                onSaved = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable<ProviderDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ProviderDetailRoute>()
            val viewModel: ProviderDetailViewModel = koinViewModel()
            ProviderDetailScreen(
                viewModel = viewModel,
                providerId = route.providerId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<SettingsRoute> {
            val viewModel: SettingsViewModel = koinViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
        composable<AssistantsRoute> {
            val viewModel: AssistantsViewModel = koinViewModel()
            AssistantsScreen(
                viewModel = viewModel,
                onNavigateToCreate = { navController.navigate(CreateAssistantRoute) },
                onNavigateToDetail = { },
                onBack = { navController.popBackStack() },
            )
        }
        composable<CreateAssistantRoute> {
            val viewModel: CreateAssistantViewModel = koinViewModel()
            CreateAssistantScreen(
                viewModel = viewModel,
                onCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
