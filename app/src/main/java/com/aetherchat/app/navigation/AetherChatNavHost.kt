package com.aetherchat.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.aetherchat.core.ui.theme.AetherChatTheme

@Composable
fun AetherChatNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = OnboardingRoute,
    ) {
        composable<OnboardingRoute> {
            PlaceholderScreen("Onboarding")
        }
        composable<ConversationsRoute> {
            PlaceholderScreen("Conversations")
        }
        composable<ChatRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ChatRoute>()
            PlaceholderScreen("Chat: ${route.conversationId}")
        }
        composable<ProvidersRoute> {
            PlaceholderScreen("Providers")
        }
        composable<AddProviderRoute> {
            PlaceholderScreen("Add Provider")
        }
        composable<ProviderDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ProviderDetailRoute>()
            PlaceholderScreen("Provider: ${route.providerId}")
        }
        composable<SettingsRoute> {
            PlaceholderScreen("Settings")
        }
        composable<AssistantsRoute> {
            PlaceholderScreen("Assistants")
        }
    }
}

@Composable
private fun PlaceholderScreen(name: String) {
    androidx.compose.material3.Surface {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            androidx.compose.material3.Text(text = name)
        }
    }
}
