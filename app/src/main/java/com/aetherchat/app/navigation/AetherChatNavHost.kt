package com.aetherchat.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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
            OnboardingPlaceholder(navController)
        }
        composable<ConversationsRoute> {
            ConversationsPlaceholder(navController)
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
                onNavigateToAdd = { navController.navigate(AddProviderRoute) },
                onNavigateToDetail = { id -> navController.navigate(ProviderDetailRoute(id)) },
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
            PlaceholderScreen("Assistants")
        }
    }
}

@Composable
private fun OnboardingPlaceholder(navController: NavHostController) {
    androidx.compose.material3.Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            androidx.compose.material3.Text(
                "欢迎使用 AetherChat",
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Text(
                "一个入口，所有 AI",
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(32.dp))
            androidx.compose.material3.Button(
                onClick = { navController.navigate(ConversationsRoute) },
            ) {
                androidx.compose.material3.Text("→ 开始")
            }
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.TextButton(
                onClick = { navController.navigate(ConversationsRoute) },
            ) {
                androidx.compose.material3.Text("跳过")
            }
        }
    }
}

@Composable
private fun ConversationsPlaceholder(navController: NavHostController) {
    val viewModel: ConversationsViewModel = koinViewModel()
    ConversationsDrawer(
        viewModel = viewModel,
        onConversationClick = { id -> navController.navigate(ChatRoute(id)) },
        onNewConversation = {
            val id = viewModel.createNewConversation()
            navController.navigate(ChatRoute(id))
        },
        onNavigateToSettings = { navController.navigate(SettingsRoute) },
        onNavigateToAbout = { },
    )
}

@Composable
private fun PlaceholderScreen(name: String) {
    androidx.compose.material3.Surface {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Text(text = name)
        }
    }
}
