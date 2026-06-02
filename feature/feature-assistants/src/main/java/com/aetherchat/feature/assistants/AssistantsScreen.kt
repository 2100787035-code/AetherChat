package com.aetherchat.feature.assistants

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aetherchat.core.ui.theme.AppShape
import com.aetherchat.core.ui.theme.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantsScreen(
    viewModel: AssistantsViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("助手") },
                navigationIcon = {
                    Text(
                        "←",
                        modifier = Modifier.clickable(onClick = onBack).padding(AppSpacing.md),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        },
    ) { padding ->
        if (uiState.assistants.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🤖", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(AppSpacing.md))
                    Text("还没有助手", style = MaterialTheme.typography.titleMedium)
                    Text("点击 + 创建你的第一个助手", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(AppSpacing.md),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                items(uiState.assistants, key = { it.id }) { assistant ->
                    AssistantCard(
                        assistant = assistant,
                        onClick = { onNavigateToDetail(assistant.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AssistantCard(
    assistant: Assistant,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppShape.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = assistant.iconEmoji,
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.width(AppSpacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assistant.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                if (assistant.systemPrompt.isNotBlank()) {
                    Text(
                        text = assistant.systemPrompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
